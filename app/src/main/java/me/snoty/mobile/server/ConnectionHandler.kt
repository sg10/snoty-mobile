package me.snoty.mobile.server

import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.PreferenceConstants
import me.snoty.mobile.processors.ServerConnection
import java.io.*
import java.net.Inet4Address
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.*


/**
 * Created by Stefan on 27.12.2017.
 */
class ConnectionHandler {
    companion object {
        private val TAG = "ConnHandler"

        private var serverAddress: String = ""

        private var requestDelegator : RequestDelegator? = null
        private var trustManager : ServerFingerprintTrustManager = ServerFingerprintTrustManager()

        var serverConnectionPossible: Boolean = false // changed on first connect

        fun updateServerPreferences(sharedPreferences: SharedPreferences) {
            Log.d(TAG, "Updating server preferences for requests")

            val serverFingerprint = "" + sharedPreferences?.getString(PreferenceConstants.SERVER_FINGERPRINT, "")
            trustManager.setStoredFingerprint(serverFingerprint)
            serverAddress = "" + sharedPreferences?.getString(PreferenceConstants.SERVER_IP, "")
        }

        fun initServerConnectionListener(serverConnectionPlugin: ServerConnection) {
            if(requestDelegator == null) {
                requestDelegator = RequestDelegator(serverConnectionPlugin)
                requestDelegator?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }

        fun send(str : String) {
            requestDelegator?.send(str)
        }

        fun isServerConnectionPossible() : Boolean {
            return serverConnectionPossible
        }

    }


    class RequestDelegator constructor(private val serverConnectionPlugin: ServerConnection)
        : AsyncTask<Void, Void, Boolean>() {

        private val requestQueue : LinkedList<String> = LinkedList()
        private val queueLock = ReentrantLock()

        private var socket : SSLSocket? = null

        override fun doInBackground(vararg x: Void): Boolean {
            if (socket != null && socket!!.isConnected) return true

            if (serverAddress == "") {
                Log.d(TAG, "no connection data set")
                return false
            }

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, arrayOf(ConnectionHandler.trustManager), SecureRandom())
            val ssf: SSLSocketFactory = sslContext.socketFactory

            while(true) {
                try {
                    if(socket == null || socket?.isConnected != true || socket?.isClosed == true) {
                        Log.d(TAG, "establishing connection to '$serverAddress' ...")
                        val addr = Inet4Address.getByName(serverAddress)
                        val newSocket = ssf.createSocket(addr, 9096) as SSLSocket

                        newSocket.startHandshake() // throws SSLHandshakeException

                        serverConnectionPossible = true

                        Log.d(TAG, "socket created")

                        this.socket = newSocket
                    }

                    if(socket?.isClosed == false) {
                        sendQueuedRequests()
                        //receive()
                    }

                } catch (ioex: IOException) {
                    if(ioex is SSLHandshakeException) {
                        ConnectionHandler.serverConnectionPossible = false
                        ConnectionHandler.requestDelegator = null
                        Log.d(TAG, "no server connection possible (${ioex.message})")
                        return false
                    }
                    else if(ioex.message?.contains("ECONNREFUSED") == false) {
                        Log.e(TAG, "Caught IO Exception", ioex)
                        Log.e(TAG, ioex.message)
                    }
                }

                Thread.sleep(300)
            }

            return false
        }

        fun send(s : String) {
            Log.d(TAG, "adding request to queue\n$s")
            queueLock.lock()
            requestQueue.add(s)
            queueLock.unlock()
        }

        private fun sendQueuedRequests() {

            queueLock.lock()

            if(requestQueue.size == 0) {
                queueLock.unlock()
                return
            }

            val nextQueueElement = requestQueue.remove()

            if(nextQueueElement != null) {
                val data = nextQueueElement.replace("\n", "")+"\n"

                Log.d(TAG, "sending data\n $data")
                try {
                    val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                    writer.write(data)
                    writer.flush()
                    writer.close()
                    Log.d(TAG, "sent!")
                }
                catch(ex : Exception) {
                    Log.w(TAG, "Error sending request, adding to queue again (${ex.message})")
                    requestQueue.add(nextQueueElement)
                    if(ex is SSLHandshakeException) {
                        socket = null
                    }
                    Thread.sleep(1000)
                }
            }

            queueLock.unlock()
        }

        private fun receive() {
            val stream = socket?.inputStream
            val reader = BufferedReader(InputStreamReader(stream))

            if(stream == null || reader == null) return

            if(stream.available() >= 2 && reader.ready()) {
                reader.read()
                val line = reader.readLine()
                if (line != null) {
                    Log.d(TAG, "received data from server: $line")
                    serverConnectionPlugin.receivedCommand(line)
                }
            }
        }
    }

}
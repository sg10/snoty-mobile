package me.snoty.mobile.server.connection

import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NetworkPacket
import java.io.*
import java.net.Inet4Address
import java.security.SecureRandom
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Created by Stefan on 12.01.2018.
 */
class RequestDelegator : AsyncTask<Void, Void, Boolean>() {

    private val TAG = "RequestDelegator"

    private var socket : SSLSocket? = null

    private var serverAddressChanged : Boolean = true
    fun refreshServerAddress() { serverAddressChanged = true }

    private var sendThreadRunning : Boolean = false
    private var receiveThreadRunning : Boolean = false

    override fun doInBackground(vararg x: Void): Boolean {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf(ConnectionHandler.instance.trustManager), SecureRandom())
        val ssf: SSLSocketFactory = sslContext.socketFactory

        try {
            while (true) {
                val serverAddress = ConnectionHandler.instance.serverAddress

                if (ConnectionHandler.instance.serverAddress == "") {
                    Log.d(TAG, "no connection data set")
                    handleError(ConnectionHandler.ConnectionError.NO_SERVER_SET)
                    return false
                }

                if (serverAddressChanged || socket == null || socket?.isConnected != true
                        || socket?.isClosed == true) {
                    serverAddressChanged = false

                    val addr = Inet4Address.getByName(serverAddress)

                    Log.d(TAG, "establishing connection to $addr:${ConnectionHandler.PORT} ...")
                    val newSocket = ssf.createSocket(addr, ConnectionHandler.PORT) as SSLSocket

                    newSocket.startHandshake() // throws SSLHandshakeException

                    Log.d(TAG, "socket created")
                    ConnectionHandler.instance.connected = true

                    this.socket = newSocket
                }

                if (socket?.isClosed == false) {
                    startSenderIfNotRunning()
                    startReceiverIfNotRunning()
                } else {
                    handleError(ConnectionHandler.ConnectionError.CONNECTION_CLOSED)
                    break
                }

                Thread.sleep(2000)
            }

        } catch (ex: IOException) {
            handleException(ex)
        }

        return false
    }

    private fun handleException(ex : Exception) {
        if (ex is SSLHandshakeException) {
            Log.d(TAG, "no server connection possible: ${ex.message}")
            handleError(ConnectionHandler.ConnectionError.FINGERPRINT_NO_MATCH)
        }
        if (ex.message?.contains("ECONNREFUSED") == true) {
            Log.d(TAG, "connection refused")
            handleError(ConnectionHandler.ConnectionError.CONNECTION_REFUSED)
        } else {
            Log.e(TAG, "Caught IO Exception", ex)
            Log.e(TAG, ex.message)
            handleError(ConnectionHandler.ConnectionError.CONNECTION_CLOSED)
        }
    }

    private fun handleError(reason : ConnectionHandler.ConnectionError) {
        ConnectionHandler.instance.handleError(reason)
        this.cancel(true)
    }

    private fun startSenderIfNotRunning() {
        synchronized(sendThreadRunning) {
            if(sendThreadRunning) return
        }
        Thread({
            synchronized(sendThreadRunning) {
                if(sendThreadRunning || socket?.isConnected == false) return@Thread
                else sendThreadRunning = true
            }

            Log.d(TAG, "Starting request sender thread")

            var packet : NetworkPacket? = null
            try {
                while(socket != null && socket?.isConnected == true) {
                    packet = ConnectionHandler.instance.getNextRequest()

                    if (packet != null) {
                        val data = NetworkPacketHandler.instance.toJSON(packet)

                        Log.d(TAG, "sending data\n" + NetworkPacketHandler.instance.toPrettyJSON(packet))
                        val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                        writer.write(data + "\n")
                        writer.flush()
                        writer.close()
                        Log.d(TAG, "sent!")
                    }
                    else {
                        Thread.sleep(500)
                    }
                }
            }
            catch(ex : Exception) {
                Log.w(TAG, "Error sending request, adding to queue again (${ex.message})")
                if(packet != null) {
                    ConnectionHandler.instance.addRequestToQueue(packet)
                }

                handleException(ex)
            }
            finally {
                Log.d(TAG, "Stopping request sender thread")
                synchronized(sendThreadRunning) { sendThreadRunning = false }
            }
        }).start()
    }

    private fun startReceiverIfNotRunning() {
        synchronized(receiveThreadRunning) {
            if(receiveThreadRunning) return
        }
        Thread({
            synchronized(receiveThreadRunning) {
                if (receiveThreadRunning || socket?.isConnected == false) return@Thread
                else receiveThreadRunning = true
            }

            Log.d(TAG, "Starting request receiver thread")

            try {

                val stream = socket?.inputStream
                val reader = BufferedReader(InputStreamReader(stream))

                while(socket != null && socket?.isConnected == true) {
                    val line = reader.readLine()
                    if (line != null) {
                        Log.d(TAG, "received data from server: $line")
                        val packet = NetworkPacketHandler.instance.fromJSON(line)
                        if(packet != null) {
                            ConnectionHandler.instance.processResponse(packet)
                        }
                    }
                }
            }
            catch(ex : Exception) {
                Log.w(TAG, "Error receiving request (${ex.message})")
                handleException(ex)
            }
            finally {
                Log.d(TAG, "Stopping request receiver thread")
                synchronized(receiveThreadRunning) { receiveThreadRunning = false }
            }
        }).start()
    }
}
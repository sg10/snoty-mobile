package me.snoty.mobile.server

import android.content.SharedPreferences
import android.net.SSLCertificateSocketFactory
import android.os.AsyncTask
import android.util.Log
import java.security.cert.X509Certificate
import javax.net.SocketFactory
import kotlin.properties.Delegates
import me.snoty.mobile.PreferenceConstants
import me.snoty.mobile.Utils
import me.snoty.mobile.processors.ServerConnection
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.security.SecureRandom
import javax.net.ssl.*


/**
 * Created by Stefan on 27.12.2017.
 */
class ConnectionHandler {
    companion object {
        private val TAG = "ConnHandler"

        private var socket: SSLSocket? = null
        private var serverAddress: String = ""
        private var serverFingerprint: String = ""

        fun updateServerPreferences(sharedPreferences: SharedPreferences) {
            Log.d(TAG, "Updating server preferences for requests")
            serverFingerprint = "" + sharedPreferences?.getString(PreferenceConstants.SERVER_FINGERPRINT, "")
            serverAddress = "" + sharedPreferences?.getString(PreferenceConstants.SERVER_IP, "")
        }

        fun initServerConnectionListener(serverConnectionPlugin: ServerConnection) {
            Receiver(serverConnectionPlugin).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        fun send(str : String) {
            Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str)
        }

        private fun waitConnected() {
            while(socket == null || socket?.isConnected == false) {
                connect()
                Thread.sleep(500)
            }
        }

        private fun connect() {
            ConnectionEstablisher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }


    class ConnectionEstablisher : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg x: Void): Boolean {
            if (socket != null && socket!!.isConnected) return true

            if (serverAddress == "") {
                Log.d(TAG, "no connection data set")
                return false
            }

            Log.d(TAG, "establishing connection to '$serverAddress' ...")

            try {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, arrayOf(CustomTrustManager()), SecureRandom())
                val ssf: SSLSocketFactory = sslContext.socketFactory
                val addr = Inet4Address.getByName(serverAddress)
                val newSocket = ssf.createSocket(addr, 9096) as SSLSocket
                newSocket.startHandshake()
                Log.d(TAG, "socket created")

                ConnectionHandler.socket = newSocket

                return true
            } catch (ioex: IOException) {
                Log.e(TAG, "Caught IO Exception", ioex)
                Log.e(TAG, ioex.message)
            }

            return false
        }
    }


    class Sender : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg dataToSend: String?): Boolean {
            if (dataToSend == null || dataToSend.isEmpty() || dataToSend[0] == null) {
                return false
            }

            val data = dataToSend[0] ?: ""

            ConnectionHandler.waitConnected()

            try {
                Log.d(TAG, "sending data\n$data")
                val writer = OutputStreamWriter(socket?.outputStream)
                writer.write(data)
                Log.d(TAG, "sent!")
                return true
            } catch(ex : Exception) {
                Log.w(TAG, "could not create connection to server")
            }
            return false
        }
    }

    class Receiver constructor(private val serverConnectionPlugin: ServerConnection) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg x: Void): Void {

            while(true) {
                try {
                    ConnectionHandler.waitConnected()
                    while(socket?.isConnected != null) {
                        val line = socket?.inputStream?.bufferedReader()?.readLine()
                        if (line != null) {
                            serverConnectionPlugin.receivedCommand(line)
                        }
                    }
                }
                catch(ex : Exception) {
                    Log.d(TAG, "lost server connection", ex)
                }
            }
        }
    }

}
package me.snoty.mobile.server.connection

import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NetworkPacket
import java.io.*
import java.net.Inet4Address
import java.security.SecureRandom
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

    var serverAddressChanged : Boolean = true
    fun refreshServerAddress() { serverAddressChanged = true }

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
                    sendQueuedRequests()
                    //receive()
                } else {
                    handleError(ConnectionHandler.ConnectionError.CONNECTION_CLOSED)
                    break
                }


                Thread.sleep(300)
            }

        } catch (ex: IOException) {
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

        return false
    }

    private fun handleError(reason : ConnectionHandler.ConnectionError) {
        ConnectionHandler.instance.handleError(reason)
        this.cancel(true)
    }

    private fun sendQueuedRequests() {
        val packet : NetworkPacket? = ConnectionHandler.instance.getNextRequest()

        if(packet != null) {
            val data = NetworkPacketHandler.instance.toJSON(packet)

            Log.d(TAG, "sending data\n" + NetworkPacketHandler.instance.toPrettyJSON(packet))
            try {
                val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                writer.write(data+"\n")
                writer.flush()
                writer.close()
                Log.d(TAG, "sent!")
            }
            catch(ex : Exception) {
                Log.w(TAG, "Error sending request, adding to queue again (${ex.message})")
                ConnectionHandler.instance.addRequestToQueue(packet)
                // handled by exception in doInBackground
                throw ex
            }
        }
        else {
            Thread.sleep(1000)
        }
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
                // to ConnectionHandler
            }
        }
    }
}
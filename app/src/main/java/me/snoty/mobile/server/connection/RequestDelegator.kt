package me.snoty.mobile.server.connection

import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NetworkPacket
import java.io.*
import javax.net.ssl.SSLSocket

/**
 * Created by Stefan on 12.01.2018.
 */
class RequestDelegator
    constructor(private val connectionChecker: ConnectionChecker)
    : AsyncTask<Void, Void, Boolean>() {

    private val TAG = "RequestDelegator"

    private var receiverThread : Thread? = null
    private var senderThread : Thread? = null

    override fun doInBackground(vararg x: Void): Boolean {
        // wait in case the client is currently connecting
        if (!ConnectionHandler.instance.connected) {
            Thread.sleep(2000)
        }

        while (ConnectionHandler.instance.connected) {
            val socket = connectionChecker.socket

            if(socket != null) {
                startSenderIfNotRunning(socket)
                startReceiverIfNotRunning(socket)
            }
            else {
                break
            }

            Thread.sleep(1000)
        }



        return false
    }

    private fun startSenderIfNotRunning(socket : SSLSocket) {
        if(senderThread?.isAlive == true) return

        senderThread = Thread({
            Log.d(TAG, "Starting request sender thread")

            var packet : NetworkPacket? = null
            try {
                while(ConnectionHandler.instance.connected) {
                    packet = ConnectionHandler.instance.getNextRequest()

                    if (packet != null) {
                        val data = NetworkPacketHandler.instance.toJSON(packet)

                        Log.d(TAG, "sending data\n" + NetworkPacketHandler.instance.toPrettyJSON(packet))
                        val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                        writer.write(data + "\n")
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
            }
        })
        senderThread?.start()
    }

    private fun startReceiverIfNotRunning(socket : SSLSocket) {
        if(receiverThread?.isAlive == true) return

        receiverThread = Thread({
            Log.d(TAG, "Starting request receiver thread")

            try {

                val stream = socket?.inputStream
                val reader = BufferedReader(InputStreamReader(stream))

                while(ConnectionHandler.instance.connected) {
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
            }
        })

        receiverThread?.start()
    }

    private fun handleException(ex: Exception) {
        this.cancel(true)
        ConnectionHandler.instance.handleSocketException(ex)
    }

}
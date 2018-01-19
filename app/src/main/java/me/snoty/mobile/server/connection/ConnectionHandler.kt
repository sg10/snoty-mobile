package me.snoty.mobile.server.connection

import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.ServerPreferences
import me.snoty.mobile.notifications.ListenerService
import me.snoty.mobile.processors.ServerConnection
import me.snoty.mobile.server.protocol.NetworkPacket
import java.util.*
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.server.NetworkPacketHandler
import javax.net.ssl.SSLHandshakeException


/**
 * Created by Stefan on 27.12.2017.
 */
class ConnectionHandler {

    private object Holder {
        val INSTANCE = ConnectionHandler()
    }

    companion object {
        private val TAG = "ConnHandler"
        val instance: ConnectionHandler by lazy { Holder.INSTANCE }
    }

    val trustManager: ServerFingerprintTrustManager = ServerFingerprintTrustManager()

    var processor: ServerConnection? = null

    private var connectionChecker : ConnectionChecker = ConnectionChecker()

    private val requestQueue: LinkedList<NetworkPacket> = LinkedList()
    private var requestDelegator: RequestDelegator? = null

    var lastConnectionError: ConnectionError? = null
        private set
    var connected: Boolean = false
        private set


    fun updateServerPreferences() {
        val fingerprint = ServerPreferences.instance.getFingerprint()
        val secret = ServerPreferences.instance.getSecret()
        updateServerPreferences(fingerprint, secret)
    }

    fun updateServerPreferences(fingerprint : String?, secret : String?) {
        Log.d(TAG, "Updating server preferences for requests")

        if(fingerprint != "" && fingerprint != null) {
            trustManager.setStoredFingerprint(fingerprint)
        }

        if(secret != "" && secret != null) {
            NetworkPacketHandler.instance.setSecret(secret)
        }
    }

    fun onConnected() {
        if(connected) return

        Log.d(TAG, "Server connect")

        connected = true
        if (requestDelegator == null || requestDelegator?.isCancelled == true) {
            Log.d(TAG, "starting request delegator")
            val requestDelegator = RequestDelegator(connectionChecker)
            requestDelegator?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            this.requestDelegator = requestDelegator
        }
        refreshDisplayedStatusElements()
        ListenerService.instance?.updateServerConnectedStatus()
    }

    fun onDisconnected() {
        if(!connected) return

        Log.d(TAG, "Server disconnect")

        connected = false
        connectionChecker.resetSocket()
        requestDelegator?.cancel(true)
        refreshDisplayedStatusElements()
        ListenerService.instance?.updateServerConnectedStatus()
        requestQueue.clear()
    }

    fun getNextRequest(): NetworkPacket? {
        var request: NetworkPacket? = null

        synchronized(requestQueue) {
            if (requestQueue.size > 0) {
                request = requestQueue.remove()
            }
        }

        return request
    }

    fun stop() {
        requestDelegator?.cancel(true)
        connectionChecker.started = false
    }

    fun start() {
        connectionChecker.poke()
    }

    fun addRequestToQueue(packet: NetworkPacket) {
        Log.d(TAG, "adding request to queue")
        synchronized(requestQueue) {
            requestQueue.add(packet)
        }

        connectionChecker.poke()
    }

    fun processResponse(packet: NetworkPacket) {
        processor?.receivedCommand(packet)
    }

    fun handleSocketException(ex: Exception?) {
        var error : ConnectionError? = null

        if (ex is SSLHandshakeException) {
            error = ConnectionError.FINGERPRINT_NO_MATCH
        }
        else if (ex?.message?.contains("Connection reset by peer") == true) {
            error = ConnectionError.CONNECTION_CLOSED
        }
        else if (ex?.message?.contains("ECONNREFUSED") == true) {
            error = ConnectionError.CONNECTION_REFUSED
        }
        else {
            error = ConnectionError.CONNECTION_CLOSED
        }

        if(error != lastConnectionError) {
            Log.d(TAG, error.name+"\n${ex?.message}")
        }
        lastConnectionError = error

        onDisconnected()
        refreshDisplayedStatusElements()
    }

    private fun refreshDisplayedStatusElements() {
        MainActivity.instance?.updateViews()
    }

}
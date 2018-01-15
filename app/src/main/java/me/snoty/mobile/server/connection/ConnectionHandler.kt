package me.snoty.mobile.server.connection

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import me.snoty.mobile.ServerPreferences
import me.snoty.mobile.notifications.ListenerService
import me.snoty.mobile.processors.ServerConnection
import me.snoty.mobile.server.protocol.NetworkPacket
import java.util.*
import android.os.Looper
import me.snoty.mobile.activities.MainActivity


/**
 * Created by Stefan on 27.12.2017.
 */
class ConnectionHandler {

    private object Holder {
        val INSTANCE = ConnectionHandler()
    }

    enum class ConnectionError {
        NO_SERVER_SET,
        FINGERPRINT_NO_MATCH,
        CONNECTION_CLOSED,
        CONNECTION_REFUSED
    }

    companion object {
        private val TAG = "ConnHandler"

        val instance: ConnectionHandler by lazy { Holder.INSTANCE }
    }

    val trustManager: ServerFingerprintTrustManager = ServerFingerprintTrustManager()

    var lastConnectionError: ConnectionError? = null
        private set

    var serverConnectionListener: ServerConnection? = null

    var connected: Boolean = false
        set(value) {
            val before = field
            field = value
            if(before != value) {
                updateLinkedResources()
            }
        }

    private val requestQueue: LinkedList<NetworkPacket> = LinkedList()

    private var requestDelegator: RequestDelegator? = null

    fun updateServerPreferences() {
        Log.d(TAG, "Updating server preferences for requests")

        val serverFingerprint = ServerPreferences.instance.getFingerprint()
        if(serverFingerprint != null && serverFingerprint != "") {
            trustManager.setStoredFingerprint(serverFingerprint)
        }
        else {
            Log.e(TAG, "did not update server fingerprint in trust manager")
        }

        requestDelegator?.updateServerPreferences()
    }

    fun connect() {
        if (requestDelegator == null || requestDelegator?.isCancelled == true) {
            updateServerPreferences()
            Log.d(TAG, "attempting server connect")
            val requestDelegator = RequestDelegator()
            requestDelegator?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            this.requestDelegator = requestDelegator
        }
    }

    fun disconnect() {
        requestDelegator?.cancel(true)
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

    fun addRequestToQueue(packet: NetworkPacket) {
        connect()
        Log.d(TAG, "adding request to queue")
        synchronized(requestQueue) {
            requestQueue.add(packet)
        }
    }

    fun processResponse(packet: NetworkPacket) {
        serverConnectionListener?.receivedCommand(packet)
    }

    fun handleError(reason: ConnectionError) {
        lastConnectionError = reason
        connected = false

        /*
        displayError(reason.name)

        when (reason) {
            ConnectionError.NO_SERVER_SET -> {

            }
            ConnectionError.FINGERPRINT_NO_MATCH -> {

            }
            ConnectionError.CONNECTION_CLOSED -> {

            }
            ConnectionError.CONNECTION_REFUSED -> {

            }
        }
        */

        updateLinkedResources()
    }

    private fun displayError(text: String) {
        Handler(Looper.getMainLooper()).post({
            val context = ListenerService.instance
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        })
    }

    private fun updateLinkedResources() {
        MainActivity.instance?.updateViews()
        ListenerService.instance?.updateServerConnectedStatus()
    }

}
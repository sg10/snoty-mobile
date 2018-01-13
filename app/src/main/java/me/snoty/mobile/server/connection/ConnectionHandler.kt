package me.snoty.mobile.server.connection

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import me.snoty.mobile.PreferenceConstants
import me.snoty.mobile.notifications.ListenerService
import me.snoty.mobile.processors.ServerConnection
import me.snoty.mobile.server.protocol.NetworkPacket
import java.util.*
import java.util.concurrent.locks.ReentrantLock
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

        val PORT = 9096

        val instance: ConnectionHandler by lazy { Holder.INSTANCE }
    }

    val trustManager: ServerFingerprintTrustManager = ServerFingerprintTrustManager()

    var serverAddress: String = ""
        private set

    var lastConnectionError: ConnectionError? = null
        private set

    var serverConnectionListener: ServerConnection? = null

    var connected: Boolean = false
        set(value) {
            if(field != value) updateMainActivity()
            field = value
        }

    private val requestQueue: LinkedList<NetworkPacket> = LinkedList()
    private val queueLock = ReentrantLock()

    private var requestDelegator: RequestDelegator? = null

    fun updateServerPreferences(context: Context) {
        Log.d(TAG, "Updating server preferences for requests")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        serverAddress = sharedPreferences?.getString(PreferenceConstants.SERVER_IP, "") ?: ""

        val serverFingerprint = sharedPreferences?.getString(PreferenceConstants.SERVER_FINGERPRINT, "") ?: ""
        trustManager.setStoredFingerprint(serverFingerprint)

        requestDelegator?.refreshServerAddress()
    }

    fun connect() {
        if (requestDelegator == null || requestDelegator?.isCancelled == true) {
            val requestDelegator = RequestDelegator()
            requestDelegator?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            this.requestDelegator = requestDelegator
        }
    }

    fun getNextRequest(): NetworkPacket? {
        var request: NetworkPacket? = null

        queueLock.lock()

        if (requestQueue.size > 0) {
            request = requestQueue.remove()
        }

        queueLock.unlock()

        return request
    }

    fun addRequestToQueue(packet: NetworkPacket) {
        connect()
        Log.d(TAG, "adding request to queue")
        queueLock.lock()
        requestQueue.add(packet)
        queueLock.unlock()
    }

    fun processResponse(packet: NetworkPacket) {
        serverConnectionListener?.receivedCommand(packet)
    }

    fun handleError(reason: ConnectionError) {
        lastConnectionError = reason
        connected = false

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

        updateMainActivity()
    }

    private fun displayError(text: String) {
        Handler(Looper.getMainLooper()).post({
            val context = ListenerService.instance
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        })
    }

    private fun updateMainActivity() {
        MainActivity.instance?.updateViews()
    }

}
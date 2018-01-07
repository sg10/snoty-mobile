package me.snoty.mobile.server

import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import java.security.cert.X509Certificate
import javax.net.SocketFactory
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.properties.Delegates
import me.snoty.mobile.PreferenceConstants
import me.snoty.mobile.Utils


/**
 * Created by Stefan on 27.12.2017.
 */
class ConnectionHandler constructor(dataToSend : String) : AsyncTask<String, Void, Boolean>() {
    companion object {
        private val TAG = "ConnHandler"

        private var socket : SSLSocket? = null
        private var serverAddress : String = ""
        private var serverFingerprint : String = ""

        fun updateServerPreferences(sharedPreferences: SharedPreferences) {
            Log.d(TAG, "Updating server preferences for requests")
            serverFingerprint = ""+sharedPreferences?.getString(PreferenceConstants.SERVER_FINGERPRINT, "")
            serverAddress = ""+sharedPreferences?.getString(PreferenceConstants.SERVER_IP, "")
        }

    }

    var dataToSend : String by Delegates.notNull()
    init {
        this.dataToSend = dataToSend
    }

    private fun establishConnection() {
        Log.d(TAG, "establishing connection ...")

        if(socket != null && socket!!.isConnected) return

        val sf : SocketFactory = SSLSocketFactory.getDefault()
        val newSocket = sf.createSocket("derstandard.at", 443) as SSLSocket
        newSocket.addHandshakeCompletedListener { e ->
            e?.peerCertificates?.forEach { cert ->
                if(cert is X509Certificate) {
                    val x509cert : X509Certificate = cert
                    val fingerprint = Utils.getCertificateFingerprint(x509cert)
                    Log.d(TAG, "comparing fingerprints\n$fingerprint\n$serverFingerprint")
                }
                else {
                    Log.e(TAG, "unexpected certificate format!")
                }
            }
        }
        newSocket.startHandshake()
    }

    private fun send(data : String) {

    }

    override fun doInBackground(vararg p0: String?): Boolean {
        establishConnection()
        send(dataToSend)
        return true
    }


}
package me.snoty.mobile.server.connection

import android.util.Log
import me.snoty.mobile.Utils
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Created by Stefan on 08.01.2018.
 */
class ServerFingerprintTrustManager : X509TrustManager {
    private val TAG = "TrustMgr"

    private var storedFingerprint : String? = null

    fun setStoredFingerprint(fingerprint : String) {
        storedFingerprint = fingerprint
    }


    override fun checkClientTrusted(certs: Array<out X509Certificate>?, s: String?) {
        throw Exception("trust store is not supposed to handle clients")
    }

    override fun checkServerTrusted(certs: Array<out X509Certificate>?, p1: String?) {
        certs?.forEach { cert ->
            if(cert is X509Certificate) {
                val x509cert : X509Certificate = cert
                val fingerprint = Utils.getCertificateFingerprint(x509cert)
                if(fingerprint != storedFingerprint) {
                    Log.e(TAG, "server's fingerprint is $fingerprint")
                    throw CertificateException("stored fingerprint does not match server")
                }
            }
            else {
                Log.e(TAG, "unexpected certificate format!")
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        throw Exception("trust store does not validate issuers")
    }
}
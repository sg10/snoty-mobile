package me.snoty.mobile.server

import android.util.Log
import me.snoty.mobile.Utils
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Created by Stefan on 08.01.2018.
 */
class CustomTrustManager : X509TrustManager {
    private val TAG = "TrustMgr"

    override fun checkClientTrusted(certs: Array<out X509Certificate>?, s: String?) {
        certs?.forEach { cert ->
            if(cert is X509Certificate) {
                val x509cert : X509Certificate = cert
                val fingerprint = Utils.getCertificateFingerprint(x509cert)
                Log.d(TAG, "fingerprint is $fingerprint")
            }
            else {
                Log.e(TAG, "unexpected certificate format!")
            }
        }
    }

    override fun checkServerTrusted(certs: Array<out X509Certificate>?, p1: String?) {
        certs?.forEach { cert ->
            if(cert is X509Certificate) {
                val x509cert : X509Certificate = cert
                val fingerprint = Utils.getCertificateFingerprint(x509cert)
                Log.d(TAG, "fingerprint is $fingerprint")
            }
            else {
                Log.e(TAG, "unexpected certificate format!")
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        Log.d(TAG, "getAcceptedIssuers")
        return arrayOf()
    }
}
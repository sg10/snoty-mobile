package me.snoty.mobile

import android.util.Log
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.*

/**
 * Created by Stefan on 07.01.2018.
 */
class Utils {

    companion object {

        private val TAG = "Utils"

        fun getCertificateFingerprint(cert: X509Certificate) : String {
            val md : MessageDigest = MessageDigest.getInstance("SHA-256")
            val der = cert.encoded
            md.update(der)
            val digest = md.digest()
            return byteArrayToHexString(digest)
        }

        fun byteArrayToHexString(bytes : ByteArray) : String {
            val formatter = Formatter()
            bytes.forEach { it ->
                formatter.format("%02x", it)
            }
            return formatter.toString()
        }


    }

}
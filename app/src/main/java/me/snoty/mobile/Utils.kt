package me.snoty.mobile

import android.util.Log
import android.view.View
import java.math.BigInteger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.*

/**
 * Created by Stefan on 07.01.2018.
 */
class Utils {

    companion object {

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

        fun hexStringToByteArray(s : String) : ByteArray {
            return BigInteger(s,16).toByteArray()
        }

        fun viewGoneIfTrue(b : Boolean) : Int {
            return if(b) View.GONE else View.VISIBLE
        }

    }

}
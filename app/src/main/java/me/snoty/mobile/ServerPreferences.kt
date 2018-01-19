package me.snoty.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.support.annotation.RequiresApi
import android.util.Log
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.notifications.ListenerService
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import java.nio.charset.Charset


/**
 * Created by Stefan on 07.01.2018.
 */
class ServerPreferences {

    private object Holder { val INSTANCE = ServerPreferences() }

    companion object {
        private val TAG = "ServerPrefs"

        val instance: ServerPreferences by lazy { Holder.INSTANCE }
    }

    private val KEY_SERVER_FINGERPRINT = "SERVER_FINGERPRINT"
    private val KEY_SERVER_SECRET = "SERVER_SECRET"
    private val KEY_SERVER_IP = "SERVER_IP"

    private val PORT = 9096

    private var prefs : SharedPreferences? = null

    fun getFingerprint() : String {
        init()
        return prefs?.getString(KEY_SERVER_FINGERPRINT, "") ?: ""
    }

    fun getSecret() : String {
        init()
        val secretEncrypted = prefs?.getString(KEY_SERVER_SECRET, "") ?: ""
        if(secretEncrypted != "") {
            val secretDecrypted = Cryptography.instance.attemptDecryptData(secretEncrypted)
            Log.d(TAG, "decrypted secret: " + secretDecrypted)
            return secretDecrypted
        }

        return ""
    }

    fun getAddress() : String {
        init()
        return prefs?.getString(KEY_SERVER_IP, "") ?: ""
    }

    fun getPort() : Int {
        init()
        return PORT
    }

    fun saveServerConnectionData(ip : String, fingerprint : String,
                                 secret : String, port : Int) : Boolean {
        Log.d(TAG, "trying to save connection data ...")
        init()
        val editor = prefs?.edit()
        if(editor == null) {
            Log.e(TAG, "could not open shared preferences for editing")
            return false
        }

        editor.putString(KEY_SERVER_FINGERPRINT, fingerprint)

        val secretEncrypted = Cryptography.instance.attemptEncryptData(secret)
        Log.d(TAG, "encrypted secret: "+secretEncrypted)
        editor.putString(KEY_SERVER_SECRET, secretEncrypted)

        editor.putString(KEY_SERVER_IP, ip)

        val saved = editor.commit()
        Log.d(TAG, "saved connection data: $saved")
        return saved
    }

    private fun init() {
        if(prefs == null) {
            val context : Context = ContextHelper.get()
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }


}
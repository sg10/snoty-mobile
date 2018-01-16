package me.snoty.mobile

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.notifications.ListenerService
import java.io.IOException
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


/**
 * Created by Stefan on 15.01.2018.
 * adapted from the AndroidKeystoreSample, linked in the official docs
 * https://github.com/googlesamples/android-BasicAndroidKeyStore/
 */
class Cryptography {

    private object Holder { val INSTANCE = Cryptography() }

    companion object {
        private val TAG = "Crypto"

        val instance: Cryptography by lazy { Holder.INSTANCE }
    }

    val KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore"
    val TYPE_RSA = "RSA"
    val PREFS_PUBLIC_KEY = "EncryptionPublicKey"

    private val mAlias = "cryptoKey"

    private var publicKey : PublicKey? = null

    private val charset = Charset.forName("UTF-8")

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    fun createKeys() {
        val start = GregorianCalendar()
        val end = GregorianCalendar()
        end.add(Calendar.YEAR, 1)

        val context : Context? = MainActivity.instance ?: ListenerService.instance

        val kpGenerator = KeyPairGenerator
                .getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID_KEYSTORE)

        val spec: AlgorithmParameterSpec

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            spec = KeyPairGeneratorSpec.Builder(context)
                    .setAlias(mAlias)
                    .setSubject(X500Principal("CN=" + mAlias))
                    .setSerialNumber(BigInteger.valueOf(1337))
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()


        } else {
            spec = KeyGenParameterSpec.Builder(mAlias, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                    .setDigests(KeyProperties.DIGEST_NONE)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSubject(X500Principal("CN=" + mAlias))
                    .setCertificateSerialNumber(BigInteger.valueOf(1337))
                    .setCertificateNotBefore(start.time)
                    .setCertificateNotAfter(end.time)
                    .build()
        }

        kpGenerator.initialize(spec)

        val kp = kpGenerator.generateKeyPair()
        Log.d(TAG, "Public Key is: " + kp.public.toString())

        publicKey = kp.public
        savePublicKey(kp.public)
    }

    private fun getTransformation(): String {
        var transformation = "RSA/ECB/PKCS1Padding"
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            transformation = "RSA/ECB/OAEPPadding"
        }*/
        return transformation
    }

    fun decryptData(inputHexStr : String) : String {
        val privateKey = getPrivateKey()

        if(privateKey == null) {
            Log.e(TAG, "error loading private key")
            return ""
        }
        else {
            Log.d(TAG, "private key loaded")
        }

        val data = Utils.hexStringToByteArray(inputHexStr)

        val cipher = Cipher.getInstance(getTransformation())
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val decryptedBytes = cipher.doFinal(data)

        return String(decryptedBytes, charset)
    }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class, InvalidKeyException::class,
            SignatureException::class, IOException::class, CertificateException::class)
    fun encryptData(inputStr: String): String? {
        loadPublicKey()

        if(publicKey == null) {
            Log.e(TAG, "error loading public key")
            return ""
        }
        else {
            Log.d(TAG, "public key is "+ Arrays.toString(publicKey?.encoded))
        }

        val data = inputStr.toByteArray(charset)

        val cipher = Cipher.getInstance(getTransformation())
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(data)

        return Utils.byteArrayToHexString(encryptedBytes)
    }

    private fun getPrivateKey(): PrivateKey? {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE)

        ks.load(null)

        val entry = ks.getEntry(mAlias, null)

        if (entry == null) {
            Log.w(TAG, "No key found under alias: " + mAlias)
            return null
        }

        if (entry !is KeyStore.PrivateKeyEntry) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry")
            return null
        }

        return entry.privateKey
    }

    private fun loadPublicKey() {
        val context : Context? = MainActivity.instance ?: ListenerService.instance

        if(publicKey == null && context != null) {
            Log.d(TAG, "loading public key from shared preferences")
            val publicKeyString = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREFS_PUBLIC_KEY, "")
            if(publicKey == null) {
                return
            }
            val keyBytes = Utils.hexStringToByteArray(publicKeyString)
            this.publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(X509EncodedKeySpec(keyBytes))

            Log.d(TAG, "Public key is  " + publicKey)
        }
    }

    private fun savePublicKey(publicKey: PublicKey) {
        val context : Context? = MainActivity.instance ?: ListenerService.instance

        if(context != null) {
            val publicKeyString = Utils.byteArrayToHexString(publicKey.encoded)
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PREFS_PUBLIC_KEY, publicKeyString)
                    .commit()
        }
        else {
            Log.e(TAG, "error saving public key, context was null")
        }
    }
}
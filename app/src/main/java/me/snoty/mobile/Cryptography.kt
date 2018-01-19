package me.snoty.mobile

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.IOException
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
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

    private val KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TYPE_RSA = "RSA"
    private val PREFS_PUBLIC_KEY = "EncryptionPublicKey"
    private val KEY_SIZE = 2048
    private val TRANSFORMATION = "RSA/ECB/OAEPwithSHA-256andMGF1Padding"
    private val KEY_ALIAS = "snoty_crypto_key"
    private val KEY_SPEC = OAEPParameterSpec("SHA-256", "MGF1",
            MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)


    private var publicKey : PublicKey? = null

    private val charset = Charset.forName("UTF-8")

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    fun createKeys() {
        if (keysAlreadyGenerated()) return

        Log.d(TAG, "generating key pair")

        val kpGenerator = KeyPairGenerator
                .getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID_KEYSTORE)

        val spec: AlgorithmParameterSpec

        val context : Context = ContextHelper.get()

        val start = GregorianCalendar()
        val end = GregorianCalendar()
        end.add(Calendar.YEAR, 1)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            spec = KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(X500Principal("CN=" + KEY_ALIAS))
                    .setSerialNumber(BigInteger.valueOf(1337))
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .setKeySize(KEY_SIZE)
                    .build()
        } else {
            spec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setCertificateSubject(X500Principal("CN=" + KEY_ALIAS))
                    .setCertificateSerialNumber(BigInteger.valueOf(1337))
                    .setCertificateNotBefore(start.time)
                    .setCertificateNotAfter(end.time)
                    .setKeySize(KEY_SIZE)
                    .build()
        }

        kpGenerator.initialize(spec)
        val kp = kpGenerator.generateKeyPair()
        Log.d(TAG, "saved private key to keystore")

        publicKey = kp.public
        savePublicKey(kp.public)
        Log.d(TAG, "saved public key to shared prefs")
    }

    fun keysAlreadyGenerated(): Boolean {
        Log.d(TAG, "checking if keypair already exists")
        if (loadPrivateKey() != null && loadPrivateKey() != null) {
            Log.d(TAG, "found public (shared prefs) and private key (keystore)")
            return true
        }
        return false
    }

    private fun decryptData(inputStr: String) : String {
        val privateKey = loadPrivateKey()

        if(privateKey == null) {
            Log.e(TAG, "error loading private key")
            return ""
        }
        else {
            Log.d(TAG, "private key loaded")
        }

        val data = Base64.decode(inputStr, Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey, KEY_SPEC)

        val decryptedBytes = cipher.doFinal(data)

        return String(decryptedBytes, charset)
    }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class, InvalidKeyException::class,
            SignatureException::class, IOException::class, CertificateException::class)
    private fun encryptData(inputStr: String): String? {
        loadPublicKey()

        if(publicKey == null) {
            Log.e(TAG, "error loading public key")
            return ""
        }
        else {
            Log.d(TAG, "public key is "+ Arrays.toString(publicKey?.encoded))
        }

        val data = inputStr.toByteArray(charset)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, KEY_SPEC)

        val encryptedBytes = cipher.doFinal(data)

        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun loadPrivateKey(): PrivateKey? {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE)

        ks.load(null)

        val entry = ks.getEntry(KEY_ALIAS, null)

        if (entry == null) {
            Log.w(TAG, "No key found under alias: " + KEY_ALIAS)
            return null
        }

        if (entry !is KeyStore.PrivateKeyEntry) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry")
            return null
        }

        return entry.privateKey
    }

    private fun loadPublicKey() : PublicKey? {
        val context : Context = ContextHelper.get()

        if(publicKey == null && context != null) {
            Log.d(TAG, "loading public key from shared preferences")
            val publicKeyString = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREFS_PUBLIC_KEY, "")
            if(publicKeyString == "") {
                createKeys()
                return null
            }
            val keyBytes = Utils.hexStringToByteArray(publicKeyString)
            this.publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(X509EncodedKeySpec(keyBytes)) ?: this.publicKey

            Log.d(TAG, "Public key is  " + publicKey)
            return publicKey
        }
        else if(context == null) {
            Log.d(TAG, "tried to load public key, but no context available")
        }

        return null
    }

    private fun savePublicKey(publicKey: PublicKey) {
        val context : Context = ContextHelper.get()

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

    fun attemptEncryptData(inputStr: String) : String? {
        Log.d(TAG, "encrypting: $inputStr")
        if(inputStr != "") {
            try {
                return encryptData(inputStr)
            }
            catch(ex : Exception) {
                Log.w(TAG, "error encrypting data ($inputStr)", ex)
            }
        }
        else {
            Log.w(TAG, "data to encrypt was empty")
        }
        return ""
    }

    fun attemptDecryptData(inputStr: String) : String {
        if(inputStr != "") {
            try {
                return decryptData(inputStr)
            }
            catch(ex : Exception) {
                Log.w(TAG, "error decrypting data ($inputStr)", ex)
            }
        }
        else {
            Log.w(TAG, "data to decrypt was empty")
        }
        return ""
    }

}
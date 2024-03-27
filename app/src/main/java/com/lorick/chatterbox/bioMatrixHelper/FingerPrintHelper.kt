package com.lorick.chatterbox.bioMatrixHelper

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class FingerprintHelper(private val context: Context, var bioMatrixCallBack : ((Boolean) -> Unit)? = null) :FingerprintManager.AuthenticationCallback() {
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var fingerprintManager: FingerprintManager
    lateinit var km :KeyguardManager


    fun initCryptoObject() {
        km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!km.isKeyguardSecure){
            Toast.makeText(context, "Lock screen Not Enabled", Toast.LENGTH_SHORT).show()
            return
        }

     try {
         val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
         keyGenerator.init(
             KeyGenParameterSpec.Builder("myKeyAlias", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                 .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                 .setUserAuthenticationRequired(true) // Require authentication to use the key
                 .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                 .build()
         )
         keyGenerator.generateKey()
     }catch (_:Exception){}

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        val key = keyStore.getKey("myKeyAlias", null)
        Log.d("dafffdfds",key.toString())
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cryptoObject = FingerprintManager.CryptoObject(cipher)
        fingerprintManager = context.getSystemService(FingerprintManager::class.java)

        if (!fingerprintManager.hasEnrolledFingerprints()){
            Toast.makeText(context, "Register least one Finger in setting", Toast.LENGTH_SHORT).show()
            return
        }
    }

    fun startAuthentication() {
        val cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        bioMatrixCallBack?.invoke(false)
        when (errorCode) {
            FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE -> {
//                showErrorSnack(context as Activity,"Fingerprint sensor is unavailable")
            }

            FingerprintManager.FINGERPRINT_ERROR_LOCKOUT -> {
//                showErrorSnack(context as Activity ,"Fingerprint authentication is temporarily locked")
            }
            else -> {
//                showErrorSnack(context as Activity,"Fingerprint authentication error: $errString")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        bioMatrixCallBack?.invoke(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        bioMatrixCallBack?.invoke(false)
//        showErrorSnack(context as Activity,"onAuthenticationFailed")
    }
}


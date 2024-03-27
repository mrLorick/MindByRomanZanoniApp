package com.lorick.chatterbox.bioMatrixHelper

import androidx.fragment.app.FragmentActivity
import com.lorick.chatterbox.utils.showToast
import dev.skomlach.biometric.compat.AuthenticationFailureReason
import dev.skomlach.biometric.compat.AuthenticationResult
import dev.skomlach.biometric.compat.BiometricApi
import dev.skomlach.biometric.compat.BiometricAuthRequest
import dev.skomlach.biometric.compat.BiometricConfirmation
import dev.skomlach.biometric.compat.BiometricCryptographyPurpose
import dev.skomlach.biometric.compat.BiometricManagerCompat
import dev.skomlach.biometric.compat.BiometricPromptCompat
import dev.skomlach.biometric.compat.BiometricType
import dev.skomlach.biometric.compat.crypto.CryptographyManager
import java.nio.charset.Charset

class CustomFaceOrFingerprintAuth(var activity: FragmentActivity)  {


     fun startBioAuth(callBack :(Boolean,String)->Unit) {
        val iris = BiometricAuthRequest(
            BiometricApi.AUTO,
            BiometricType.BIOMETRIC_IRIS,
            BiometricConfirmation.ANY
        )
        val faceId = BiometricAuthRequest(
            BiometricApi.AUTO,
            BiometricType.BIOMETRIC_FACE,
            BiometricConfirmation.ANY
        )
        val fingerprint = BiometricAuthRequest(
            BiometricApi.AUTO,
            BiometricType.BIOMETRIC_FINGERPRINT,
            BiometricConfirmation.ANY
        )
        var title = ""
        val currentBiometric =
            if (BiometricManagerCompat.isHardwareDetected(iris)
                && BiometricManagerCompat.hasEnrolled(iris)
            ) {
                title =
                    "Your eyes are not only beautiful, but you can use them to unlock our app"
                iris
            } else
                if (BiometricManagerCompat.isHardwareDetected(faceId)
                    && BiometricManagerCompat.hasEnrolled(faceId)
                ) {
                    title = "Use your smiling face to enter the app"
                    faceId
                } else if (BiometricManagerCompat.isHardwareDetected(fingerprint)
                    && BiometricManagerCompat.hasEnrolled(fingerprint)
                ) {
                    title = "Your unique fingerprints can unlock this app"
                    fingerprint
                } else {
                    null
                }

        currentBiometric?.let { biometricAuthRequest ->
            if (BiometricManagerCompat.isBiometricSensorPermanentlyLocked(biometricAuthRequest)
                || BiometricManagerCompat.isLockOut(biometricAuthRequest)
            ) {
                callBack(false,"NOT_ADD_BIOMETRIC")
                activity.showToast("Biometric not available right now. Try again later")
                return
            }

            val prompt = BiometricPromptCompat.Builder(activity).apply {
                this.setTitle(title)
                this.setEnabledNotification(false)//hide notification
                this.setEnabledBackgroundBiometricIcons(false)//hide duplicate biometric icons above dialog
                this.setCryptographyPurpose(
                    BiometricCryptographyPurpose(
                        BiometricCryptographyPurpose.ENCRYPT)
                )//request Cipher for encryption
            }
            /*    if(prompt.enableSilentAuth()){
                    showToast("Unable to use Silent Auth on current device :|")
                    return
                }*/
            prompt.build().authenticate(object : BiometricPromptCompat.AuthenticationCallback() {
                override fun onSucceeded(confirmed: Set<AuthenticationResult>) {
                    super.onSucceeded(confirmed)
                    val encryptedData = CryptographyManager.encryptData(
                        "Hello, my friends".toByteArray(Charset.forName("UTF-8")),
                        confirmed
                    )
                    callBack(true,"SUCCESS")
//                    activity.showToast("User authorized :)\n Biometric used for Encryption=${encryptedData?.biometricType}\n EncryptedData=${encryptedData?.data}; InitializationVector=${encryptedData?.initializationVector};")
                }

                override fun onCanceled() {
                    callBack(false,"CANCELED")
//                    activity.showToast("Auth canceled :|")
                }

                override fun onFailed(reason: AuthenticationFailureReason?, dialogDescription: CharSequence?) {
                    super.onFailed(reason, dialogDescription)
                    callBack(false,"FAILED")
                    activity.showToast("Fatal error happens :(\nReason $reason")
                }

                override fun onUIOpened() {}

                override fun onUIClosed() {}
            })
        } ?: run {
            callBack(false,"NOT_AVAILABLE")
            activity.showToast("No available biometric on this device")
        }

    }

}
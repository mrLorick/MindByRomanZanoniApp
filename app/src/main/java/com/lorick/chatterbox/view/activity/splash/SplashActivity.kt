package com.lorick.chatterbox.view.activity.splash

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.util.Base64
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.bioMatrixHelper.CustomFaceOrFingerprintAuth
import com.lorick.chatterbox.databinding.ActivitySplashBinding
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.BioMatrixSharedPrefs
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setStatusBarHideBoth
import com.lorick.chatterbox.view.activity.dashboard.DashboardActivity
import com.lorick.chatterbox.view.activity.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private var bioMatrixSharedPrefs : BioMatrixSharedPrefs? = null
    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var km: KeyguardManager
    var activity = this@SplashActivity

    @Inject
    lateinit var sharedPrefs :SharedPrefs

    override fun getLayoutRes(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        setStatusBarHideBoth()
        initialiseBioMatrixClass()
        handlingBio()
        generateHashKey(applicationContext)
    }

    private fun showBioMatrixDialog() {
        RunInScope.mainThread {
            delay(1000)

            CustomFaceOrFingerprintAuth(activity).startBioAuth { status, result  ->
                if (status) {
                    lifecycleScope.launch {
                        delay(1000)
                        if (sharedPrefs.getUserLogin()){
                            launchActivity<DashboardActivity> { }
                            finishAffinity()
                        }else{
                            launchActivity<OnBoardingActivity> {  }
                            finishAffinity()
                        }
                    }
                }else if (result == "CANCELED"){
                    finishActivity()
                }
            }
     /*       val bottomSheetBiometric = BottomSheetBiometric()
            bottomSheetBiometric.show(supportFragmentManager, "")*/
        }
    }

    fun generateHashKey(context: Context) {
        try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in packageInfo.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), Base64.DEFAULT))
                Log.d("HashKey", hashKey)
                // You can use hashKey as needed (e.g., for Facebook app settings)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }



    private fun handlingBio() {
        if (bioMatrixSharedPrefs?.getBoolean(AppConstants.BIO_MATRIX_STATUS_TOGGLE) == true){
            showBioMatrixDialog()
        }else{
            lifecycleScope.launch {
                delay(1000)
                if (sharedPrefs.getUserLogin()){
                    launchActivity<DashboardActivity> { }
                    finishAffinity()
                }else{
                    launchActivity<OnBoardingActivity> {  }
                    finishAffinity()
                }
            }
        }
    }

    private fun initialiseBioMatrixClass() {
        try {
            bioMatrixSharedPrefs = BioMatrixSharedPrefs(this@SplashActivity)
            km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(FingerprintManager::class.java)
            if (!km.isKeyguardSecure) {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_TOGGLE,false)
                return
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_TOGGLE,false)
                return
            }
        }catch (_:Exception){}
    }


    override fun viewModel() {

    }

}
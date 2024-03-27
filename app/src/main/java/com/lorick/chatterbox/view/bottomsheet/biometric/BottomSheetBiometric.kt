package com.lorick.chatterbox.view.bottomsheet.biometric

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lorick.chatterbox.R
import com.lorick.chatterbox.bioMatrixHelper.FingerprintHelper
import com.lorick.chatterbox.databinding.BottomsheetBiometricBinding
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.view.activity.dashboard.DashboardActivity
import com.lorick.chatterbox.view.activity.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetBiometric : BottomSheetDialogFragment(), View.OnClickListener {
    private var binding: BottomsheetBiometricBinding? = null
    var fingerprintHelper: FingerprintHelper? = null
    private lateinit var biometricPrompt: BiometricPrompt

    @Inject
    lateinit var sharedPrefs :SharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_biometric, container, false) as BottomsheetBiometricBinding
        setPeekHeight()
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBioMatrix()
        bioMatrixResultCallBack()
        onClickListener()
    }

    private fun faceAuthentication() {
        val biometricManager = androidx.biometric.BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate() == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric authentication can be used
            authenticateUser()
        } else {
            // Biometric authentication is not available or supported
            Toast.makeText(requireContext(), "Biometric authentication is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun authenticateUser() {
        val cancellationSignal = CancellationSignal()
        val mainExecutor = ContextCompat.getMainExecutor(requireContext())

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    biometricPrompt = BiometricPrompt.Builder(requireContext())
                        .setTitle("Authenticate to unlock")
                        .setSubtitle("Use your face to unlock")
                        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                        .build()
                }
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                biometricPrompt.authenticate(cancellationSignal,mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Handle authentication error
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Handle successful authentication
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Handle authentication failure
                    }
                })
            }
        }catch (_:Exception){}
    }

    private fun initBioMatrix(){
        fingerprintHelper = FingerprintHelper(requireContext())
        fingerprintHelper!!.initCryptoObject()
        fingerprintHelper!!.startAuthentication()
    }

    private fun bioMatrixResultCallBack(){
        fingerprintHelper?.bioMatrixCallBack = { status ->
            if (status) {
                lifecycleScope.launch {
                    delay(1000)
                    if (sharedPrefs.getUserLogin()){
                        requireActivity().launchActivity<DashboardActivity> { }
                        requireActivity().finishAffinity()
                    }else{
                        requireActivity().launchActivity<OnBoardingActivity> {  }
                        requireActivity().finishAffinity()
                    }
                }
            } else {
                val shakeAnimator = ObjectAnimator.ofFloat( binding?.ivFingerPrint, "translationX", -10f, 10f)
                shakeAnimator.duration = 100
                shakeAnimator.repeatCount = 5
                shakeAnimator.repeatMode = ObjectAnimator.REVERSE
                shakeAnimator.start()

                val redColor = ContextCompat.getColor(requireContext(), R.color.red)
                binding?.ivFingerPrint?.setColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                lifecycleScope.launch {
                    delay(500)
                    val color = ContextCompat.getColor(requireContext(), R.color.theme_color_green)
                    binding?.ivFingerPrint?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                }
            }
        }
    }

    private fun setPeekHeight() {
        dialog?.setOnShowListener {
            val dialogParent = binding?.layoutCoordinate?.parent as View
            BottomSheetBehavior.from(dialogParent).peekHeight = (binding?.layoutCoordinate?.height!! * 100).toInt()
            dialog?.setCanceledOnTouchOutside(false)
            dialogParent.requestLayout()
        }
    }


    override fun getTheme(): Int {
        return R.style.CustomBottomSheetTheme
    }


    fun onClickListener() {
        binding?.ivFingerPrint?.setOnClickListener(this)
        binding?.tvCancel?.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding?.ivFingerPrint -> {

            }

            binding?.tvCancel -> {
                requireActivity().finishAffinity()
            }
        }
    }
}
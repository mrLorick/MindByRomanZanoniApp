package com.lorick.chatterbox.view.activity.verificationCode

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.broadCastResciver.SmsBroadcastReceiver
import com.lorick.chatterbox.databinding.ActivityVerificationCodeBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.view.activity.createPassword.CreatePasswordActivity
import com.lorick.chatterbox.view.bottomsheet.registersuccessfully.BottomSheetRegisterSuccessfully
import com.lorick.chatterbox.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class VerificationCodeActivity : BaseActivity<ActivityVerificationCodeBinding>() {
    private val viewModal: AuthViewModel by viewModels()
    var activity = this@VerificationCodeActivity
    private var timeLeftInMillis: Long = 160000
    private var comesFrom = ""
    private lateinit var countDownTimer: CountDownTimer
    private var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    @Inject
    lateinit var validator : Validator

    @Inject
    lateinit var sharedPrefs : SharedPrefs

    override fun getLayoutRes() = R.layout.activity_verification_code

    override fun initView() {
        onClickListener()
        setToolbar()
        getIntentData()
        startSmsUserConsent()
        countDownStart()
        setEmailView()
        observeDataFromViewModal()
    }

    private fun setEmailView() {
        binding.apply {
            val color = "<p>${getString(R.string.verification_Code_sent).plus(" ")}<font color='#000000'>${viewModal.email.get().toString()}</font></p>"
            tvEmailDec.text = HtmlCompat.fromHtml(color, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.gone()
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }


    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            comesFrom = intent.getString(AppConstants.SCREEN_TYPE).toString()
            viewModal.email.set(intent.getString(AppConstants.USER_EMAIL).toString())
            setScreenTypeParam()
        }
    }


    private fun setScreenTypeParam() {
        if (comesFrom == AppConstants.SIGN_UP){
            viewModal.verificationType.set("verification")
        }else{
            viewModal.verificationType.set("forgot password")
        }
    }

    /** start Count down Resend Otp time*/
    private fun countDownStart() {
        countDownTimer =  object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onFinish() {

//                binding.tvResendOtp.visible()
            }

            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
//                binding.tvResendOtp.gone()
                val formattedTime = secondsToMinutes(secondsLeft)
                binding.tvTiming.text = formattedTime
            }
        }.start()
    }

    /** convert Second into Mint second Format -2:30 */
    fun secondsToMinutes(seconds: Long): String {
        val minutes = seconds / 60
        var mint = 0L
        if (minutes.toString().length == 1){
            mint=  0+minutes
        }
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", mint, remainingSeconds)
    }

    /** sms retrieve*/
    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener {
//
        }.addOnFailureListener {
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }
    }

    /** check otp from msg and hit the api*/
    private fun getOtpFromMessage(message: String?) {
        // This will match any 6 digit number in the message
        val pattern = Pattern.compile(AppConstants.ReadSmsPattern)
        val matcher = pattern.matcher(message!!)
        if (matcher.find()) {
            binding.firstPinView.setText(matcher.group(0)!!)
            viewModal.otp.set(matcher.group(0))
            RunInScope.ioThread {
                viewModal.hitVerificationOtpApi()
            }
        }
    }

    /** broadcast receiver receive the message from text message*/
    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    if (intent != null) {
                        startActivityForResult(intent, REQ_USER_CONSENT)
                    }
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    private fun onClickListener() {
        binding.apply {
            btnVerify.setOnClickListener {
                if (validator.validateOtpVerification(activity,binding)){
                    viewModal.otp.set(binding.firstPinView.text.toString())
                    RunInScope.ioThread {
                        viewModal.hitVerificationOtpApi()
                    }
                }
            }

            tvResendOtp.setOnClickListener {
                binding.firstPinView.text?.clear()
                countDownTimer.start()
                RunInScope.ioThread {
                    viewModal.hitResendOtpApi()
                }
            }
        }
    }



    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.verificationOtpSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if (comesFrom == AppConstants.SIGN_UP){
                                sharedPrefs.save(AppConstants.USER_AUTH_TOKEN, data.data?.token)
                                sharedPrefs.saveUserLogin(true)
                                sharedPrefs.save(AppConstants.SHARED_PREFS_USER_DATA, Gson().toJson(data.data))

                                val bottomSheetRegisterSuccessfully = BottomSheetRegisterSuccessfully()
                                bottomSheetRegisterSuccessfully.show(supportFragmentManager, "")
                            }else {
                                val bundle = bundleOf(AppConstants.USER_EMAIL to data.data?.email)
                                launchActivity<CreatePasswordActivity>(0,bundle) { }
                            }
                        } else {
                            showErrorSnack(activity, data?.message ?: "")
                        }
                    }
                    is Resource.Error -> {
                        isResponse.message?.let { msg ->
                            showErrorSnack(activity, msg)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModal.resendOtpSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {

                        } else {
                            showErrorSnack(activity, data?.message ?: "")
                        }
                    }
                    is Resource.Error -> {
                        isResponse.message?.let { msg ->
                            showErrorSnack(activity, msg)
                        }
                    }
                }
            }
        }

        viewModal.showLoading.observe(activity) {
            if (it) {
                MyProgressBar.showProgress(activity)
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    companion object {
        private const val REQ_USER_CONSENT = 200
    }
}
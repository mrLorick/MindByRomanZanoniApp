package com.lorick.chatterbox.view.activity.forgotPassword

import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityForgotPasswordBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.applyTextWatcherAndFilter
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.view.activity.verificationCode.VerificationCodeActivity
import com.lorick.chatterbox.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity<ActivityForgotPasswordBinding>() {
    private val viewModal: AuthViewModel by viewModels()
    var activity = this@ForgotPasswordActivity

    @Inject
    lateinit var validator: Validator

    override fun getLayoutRes() = R.layout.activity_forgot_password

    override fun initView() {
        getWatcherEmail()
        onClickListener()
        setToolbar()
        observeDataFromViewModal()
    }

    private fun getWatcherEmail() {
        binding.etEmail.applyTextWatcherAndFilter(validator, binding.ivTick)
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.visibility = View.GONE
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    private fun onClickListener() {
        binding.apply {
            btnSubmit.setOnClickListener {
                if (validator.validateForgotPassword(activity,binding)){
                    RunInScope.ioThread {
                        viewModal.hitForgotPasswordApi()
                    }
                }
            }
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.forgotPasswordSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.FORGOT_PASSWORD, AppConstants.USER_EMAIL to viewModal.email.get().toString())
                            launchActivity<VerificationCodeActivity>(0, bundle) { }
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
}
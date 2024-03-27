package com.lorick.chatterbox.view.activity.createPassword

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityCreatePasswordBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.view.bottomsheet.passwordchange.BottomSheetPasswordChange
import com.lorick.chatterbox.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreatePasswordActivity : BaseActivity<ActivityCreatePasswordBinding>() {
    private val viewModal: AuthViewModel by viewModels()
    var activity = this@CreatePasswordActivity

    @Inject
    lateinit var validator: Validator

    override fun getLayoutRes() = R.layout.activity_create_password

    override fun initView() {
        getIntentData()
        onClickListener()
        setToolbar()
        observeDataFromViewModal()
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun onClickListener() {
        binding.apply {
            btnCreatePassword.setOnClickListener {
                if (validator.validateResetPassword(activity,binding)){
                    RunInScope.ioThread {
                        viewModal.hitResetPasswordApi()
                    }
                }
            }
        }
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            viewModal.email.set(intent.getString(AppConstants.USER_EMAIL).toString())
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.resetPasswordSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            val bottomSheetPasswordChange = BottomSheetPasswordChange()
                            bottomSheetPasswordChange.show(supportFragmentManager, "")
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
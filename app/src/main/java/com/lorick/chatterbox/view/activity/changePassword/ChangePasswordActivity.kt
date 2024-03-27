package com.lorick.chatterbox.view.activity.changePassword

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityChangePasswordBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.showSuccessBarAlert
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@ChangePasswordActivity

    @Inject
    lateinit var validator: Validator

    override fun getLayoutRes(): Int = R.layout.activity_change_password

    override fun initView() {
        setToolbar()
        setOnClickListener()
        observeDataFromViewModal()
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun setOnClickListener() {
        binding.apply {
            btnCreatePassword.setOnClickListener {
                if (validator.validateChangePassword(activity,binding)){
                    RunInScope.ioThread {
                        viewModal.hitChangePasswordApi()
                    }
                }
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.change_password)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.changePasswordSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            showSuccessBarAlert(activity,getString(R.string.success),data.message ?: "Password Successfully Changed")
                            delay(1500)
                            finishActivity()
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
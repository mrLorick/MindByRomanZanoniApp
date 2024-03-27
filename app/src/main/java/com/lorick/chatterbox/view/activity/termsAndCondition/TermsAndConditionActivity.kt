package com.lorick.chatterbox.view.activity.termsAndCondition

import android.webkit.WebView
import android.webkit.WebViewClient
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityTermsAndConditionBinding
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity

class TermsAndConditionActivity : BaseActivity<ActivityTermsAndConditionBinding>() {
    private var screenType = ""
    var activity = this@TermsAndConditionActivity

    override fun getLayoutRes(): Int = R.layout.activity_terms_and_condition

    override fun initView() {
        MyProgressBar.showProgress(activity)
        getIntentData()
        setToolbar()
    }

    override fun viewModel() {

    }

    private fun getIntentData() {
        val intents = intent.extras
        if (intents != null){
            screenType = intents.getString(AppConstants.SCREEN_TYPE).toString()
            if(screenType == AppConstants.ABOUT_US) {
                loadUrlIntoWebView(AppConstants.ABOUT_US_URL)
            }else{
                loadUrlIntoWebView(AppConstants.TERMS_AND_CONDITION_URL)
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            if (screenType == AppConstants.ABOUT_US){
                tvToolTitle.text = getString(R.string.about_us)
            }else{
                tvToolTitle.text = getString(R.string.term_policy)
            }
            ivBack.setOnClickListener{
                finishActivity()
            }
        }
    }

    private fun loadUrlIntoWebView(url: String) {
        binding.webView.loadUrl(url)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                MyProgressBar.hideProgress()
            }
        }
    }
}
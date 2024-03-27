package com.lorick.chatterbox.view.activity.message

import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityMessageBinding
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.openChromeTab
import com.lorick.chatterbox.utils.openComingSoonDialog
import com.lorick.chatterbox.view.activity.contactUs.ContactUsActivity
import com.lorick.chatterbox.view.activity.messageList.MessageListActivity

class MessageActivity : BaseActivity<ActivityMessageBinding>() {
    override fun getLayoutRes(): Int {
        return R.layout.activity_message
    }

    override fun initView() {
        setToolbar()
        clickListeners()
    }

    private fun clickListeners() {
        binding.apply {
            cvTalkToBot.setOnClickListener {
                openComingSoonDialog { }
            }
            cvQuestion.setOnClickListener {
                launchActivity<ContactUsActivity> {  }
            }
            cvMessage.setOnClickListener {
                launchActivity<MessageListActivity> {  }
            }
            cvSessionBook.setOnClickListener {
                openChromeTab("https://www.google.com")
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.message_heading)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    override fun viewModel() {
    }

}
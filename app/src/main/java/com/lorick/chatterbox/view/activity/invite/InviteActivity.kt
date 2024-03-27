package com.lorick.chatterbox.view.activity.invite

import android.content.Intent
import android.net.Uri
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityInviteBinding
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.copyUrl
import com.lorick.chatterbox.utils.finishActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InviteActivity : BaseActivity<ActivityInviteBinding>() {
    override fun getLayoutRes(): Int  = R.layout.activity_invite


    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun initView() {
        setToolbar()
        setOnClickListener()
        marqueText()
    }

    private fun marqueText() {
        binding.apply {
            etInvite.isSelected  = true
            etInvite.requestFocus()
        }
    }

    override fun viewModel() {

    }

    private fun setOnClickListener() {
        binding.apply {
            btnInvite.setOnClickListener {
                shareInviteLink()
            }
            appCompatImageView3.setOnClickListener {
                copyUrl("https://play.google.com/store/apps/details?id=com.mindbyromanzanoni")
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.invite_a_friend)
            ivBack.setOnClickListener{
                finishActivity()
            }
        }
    }

    /** Share  event details  other apps */
    private fun shareInviteLink() {
        val deepLinkUrl = Uri.parse("https://play.google.com/store/apps/details?id=com.mindbyromanzanoni")
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Invite Link: $deepLinkUrl")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share using"))
    }
}
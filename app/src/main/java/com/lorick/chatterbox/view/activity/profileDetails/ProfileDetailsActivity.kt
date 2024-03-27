package com.lorick.chatterbox.view.activity.profileDetails

import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityProfileDetailsBinding
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.view.activity.editProfile.EditProfileActivity
import com.lorick.chatterbox.view.activity.editProfile.EditProfileActivity.Companion.callbackUpdatedProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileDetailsActivity : BaseActivity<ActivityProfileDetailsBinding>() {

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun getLayoutRes(): Int = R.layout.activity_profile_details

    companion object {
        lateinit var callbackSettingProfile :(Boolean)->Unit
    }

    override fun initView() {
        setToolbar()
        setOnClickListener()
        setUserDataOnViews()
        callBackHandlingUpdateProfile()
    }

    private fun callBackHandlingUpdateProfile() {
        callbackUpdatedProfile = { status->
            if (status){
                setUserDataOnViews()
                callbackSettingProfile.invoke(true)
            }
        }
    }

    private fun setUserDataOnViews() {
        binding.apply {
            cvImg.setImageFromUrl(R.drawable.no_image_placeholder,sharedPrefs.getUserData()?.userImage)
            etFullName.setText(sharedPrefs.getUserData()?.name)
            val countryWithNumber = sharedPrefs.getUserData()?.countryCode.plus(" ")+sharedPrefs.getUserData()?.contactNumber
            etContact.setText(countryWithNumber)
            etEmail.setText(sharedPrefs.getUserData()?.email)
        }
    }

    override fun viewModel() {

    }


    private fun setOnClickListener() {
        binding.apply {
            appCompatTextView2.setOnClickListener {
                launchActivity<EditProfileActivity> {  }
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.edit_profile)
            ivBack.setOnClickListener{
                finishActivity()
            }
        }
    }
}
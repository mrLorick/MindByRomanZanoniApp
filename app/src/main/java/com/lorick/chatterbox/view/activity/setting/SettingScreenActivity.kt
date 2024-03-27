package com.lorick.chatterbox.view.activity.setting


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivitySettingScreenBinding
import com.lorick.chatterbox.databinding.ItemSettingListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.BioMatrixSharedPrefs
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.invisible
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.previewImageAlertDialog
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.view.activity.changePassword.ChangePasswordActivity
import com.lorick.chatterbox.view.activity.contactUs.ContactUsActivity
import com.lorick.chatterbox.view.activity.invite.InviteActivity
import com.lorick.chatterbox.view.activity.login.LoginActivity
import com.lorick.chatterbox.view.activity.profileDetails.ProfileDetailsActivity
import com.lorick.chatterbox.view.activity.profileDetails.ProfileDetailsActivity.Companion.callbackSettingProfile
import com.lorick.chatterbox.view.activity.termsAndCondition.TermsAndConditionActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SettingScreenActivity : BaseActivity<ActivitySettingScreenBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@SettingScreenActivity
    private var myList = ArrayList<SettingList>()
    private var bioMatrixSharedPrefs: BioMatrixSharedPrefs? = null
    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var km: KeyguardManager

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var sharedPrefs: SharedPrefs


    override fun getLayoutRes(): Int = R.layout.activity_setting_screen

    override fun initView() {
        setToolbar()
        checkMobileDeviceSupportBioMatrix()
        getSettingList()
        initSettingRecyclerView()
        observeDataFromViewModal()
        hitUserProfileApi()
        setUserData()
        setOnClickListener()
        callBackHandlingUpdateProfile()
    }

    private fun callBackHandlingUpdateProfile() {
        callbackSettingProfile = { status ->
            if (status) {
                setUserData()
            }
        }
    }

    /** Check If bio matrix Support your device save i shared preference
     * 0 -- Device support
     * 1--  Device Your Device Not support Bio Matrix
     * 2-- Register least one Finger in setting
     * */
    private fun checkMobileDeviceSupportBioMatrix() {
        bioMatrixSharedPrefs = BioMatrixSharedPrefs(activity)
        try {
            km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(FingerprintManager::class.java)

            if (!km.isKeyguardSecure) {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_DEVICE, 1)
                return
            } else {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_DEVICE, 0)
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_DEVICE, 2)
                return
            } else {
                bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_DEVICE, 0)
            }
        }catch (_:Exception){}
    }


    /** set user data on Views*/
    private fun setUserData() {
        bioMatrixSharedPrefs = BioMatrixSharedPrefs(activity)

        binding.apply {
            cvImg.setImageFromUrl(R.drawable.no_image_placeholder, sharedPrefs.getUserData()?.userImage, progressBar)
            tvUserName.text = sharedPrefs.getUserData()?.name
            tvEmail.text = sharedPrefs.getUserData()?.email

            /** set Data From From Api or Shared Preference in Notification model class */
            viewModal.isNotificationOn.set(sharedPrefs.getUserData()?.isNotificationOn)
            myList[1].notification = viewModal.isNotificationOn.get() == true

            /** set Data From Shared Preference in Bio matrix model class */
            val bioMatrixStatus = bioMatrixSharedPrefs?.getBoolean(AppConstants.BIO_MATRIX_STATUS_TOGGLE)
            myList[3].bioMatrix = bioMatrixStatus == true
        }
    }

    override fun viewModel() {
    }

    private fun setOnClickListener() {
        binding.apply {
            constraintLayout.setOnClickListener {
                launchActivity<ProfileDetailsActivity> { }
            }
            cvImg.setOnClickListener {
                previewImageAlertDialog(activity,sharedPrefs.getUserData()?.userImage)
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.settings)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    /** set recycler view Meditation  List */
    private fun initSettingRecyclerView() {
        binding.rvSetting.adapter = resourceListAdapter
        resourceListAdapter.submitList(myList)
    }

    private val resourceListAdapter = object : GenericAdapter<ItemSettingListBinding, SettingList>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.item_setting_list
            }

            override fun onBindHolder(holder: ItemSettingListBinding, dataClass: SettingList, position: Int) {
                holder.apply {
                    tvUserName.text = dataClass.name
                    ivLike.setBackgroundResource(dataClass.icon)
                }

                holder.ivToggle.setOnClickListener {
                    when (position) {
                        1 -> {
                            if (dataClass.notification == true) {
                                dataClass.notification = false
                                viewModal.isNotificationOn.set(false)
                            } else {
                                dataClass.notification = true
                                viewModal.isNotificationOn.set(true)
                            }
                            hitUpdateNotificationSettingApi()
                        }

                        3 -> {
                            /** Check If bio matrix Support your device*/
                            if (bioMatrixSharedPrefs?.getString(AppConstants.BIO_MATRIX_STATUS_DEVICE) == 0){
                                if (dataClass.bioMatrix == true) {
                                    dataClass.bioMatrix = false
                                    viewModal.isBioMatrixOn.set(false)
                                } else {
                                    dataClass.bioMatrix = true
                                    viewModal.isBioMatrixOn.set(true)
                                }
                                handlingBioMatrixEnable(dataClass.bioMatrix!!)
                            }else if (bioMatrixSharedPrefs?.getString(AppConstants.BIO_MATRIX_STATUS_DEVICE) == 1){
                                showErrorSnack(activity,"Your Device Not support Biometric")
                            }else if (bioMatrixSharedPrefs?.getString(AppConstants.BIO_MATRIX_STATUS_DEVICE) == 2){
                                showErrorSnack(activity,"Register least one Finger in setting")
                            }
                        }
                    }
                }

                holder.root.setOnClickListener {
                    when (position) {
                        0 -> {
                            val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.ABOUT_US)
                            launchActivity<TermsAndConditionActivity>(0, bundle) { }
                        }

                        2 -> {
                            launchActivity<ChangePasswordActivity> { }
                        }

                        4 -> {
                            showFeed()
//                            val bottomSheetRegisterSuccessfully = BottomSheetRating()
//                            bottomSheetRegisterSuccessfully.show(supportFragmentManager, "")
                        }

                        5 -> {
                            launchActivity<ContactUsActivity> { }
                        }

                        6 -> {
                            val bundle =
                                bundleOf(AppConstants.SCREEN_TYPE to AppConstants.TERMS_AND_CONDITION)
                            launchActivity<TermsAndConditionActivity>(0, bundle) { }
                        }

                        7 -> {
                            launchActivity<InviteActivity> { }
                        }

                        8 -> {
                            hitLogoutApi()
                        }
                    }
                }

                if (dataClass.status == 1 || dataClass.status == 3) {
                    holder.ivToggle.visible()
                } else {
                    holder.ivToggle.invisible()
                }

                /** status == 1
                 * show in ui only toggles
                 * */
                if (dataClass.status == 1) {
                    if (dataClass.notification == true) {
                        holder.ivToggle.setBackgroundResource(R.drawable.ic_toggle_on)
                    } else {
                        holder.ivToggle.setBackgroundResource(R.drawable.ic_toggle_off)
                    }
                }

                if (dataClass.status == 3) {
                    if (dataClass.bioMatrix == true) {
                        holder.ivToggle.setBackgroundResource(R.drawable.ic_toggle_on)
                    } else {
                        holder.ivToggle.setBackgroundResource(R.drawable.ic_toggle_off)
                    }
                }

            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun handlingBioMatrixEnable(bioMatrix: Boolean) {
        bioMatrixSharedPrefs?.save(AppConstants.BIO_MATRIX_STATUS_TOGGLE,bioMatrix)
        resourceListAdapter.notifyDataSetChanged()
    }

    private fun hitLogoutApi() {
        RunInScope.ioThread {
            viewModal.userId.set(sharedPrefs.getUserData()?.userId.toString())
            viewModal.hitLogoutApi()
        }
    }

    private fun hitUserProfileApi() {
        RunInScope.ioThread {
            viewModal.hitUserProfileApi()
        }
    }

    private fun getSettingList(): ArrayList<SettingList> {
        myList = ArrayList()
        myList.add(SettingList("About Us", R.drawable.ic_info, 0))
        myList.add(SettingList("Notification", R.drawable.ic_notifications_bell, 1))
        myList.add(SettingList("Change Password", R.drawable.ic_change_password, 0))
//        myList.add(SettingList("My university", R.drawable.ic_my_universitys, 0))
        myList.add(SettingList("Biometric", R.drawable.ic_biometric, 3))
        myList.add(SettingList("Rate App", R.drawable.ic_rate_app, 0))
        myList.add(SettingList("Contact Us", R.drawable.ic_contact_us, 0))
        myList.add(SettingList("Term & Policy", R.drawable.ic_term___policy, 0))
        myList.add(SettingList("Invite a friend", R.drawable.ic_invites, 0))
        myList.add(SettingList("Logout", R.drawable.logout, 0))


        return myList
    }

    private fun hitUpdateNotificationSettingApi() {
        RunInScope.ioThread {
            viewModal.hitUpdateNotificationSettingApi()
        }
    }

    /** Observer Response via View model*/
    @SuppressLint("NotifyDataSetChanged")
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.updateNotificationSettingSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.isSuccess() == true) {
                            val userData = sharedPrefs.getUserData()
                            userData?.isNotificationOn = viewModal.isNotificationOn.get() == true
                            sharedPrefs.save(AppConstants.SHARED_PREFS_USER_DATA, Gson().toJson(userData))
                            resourceListAdapter.notifyDataSetChanged()
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
            viewModal.userProfileSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.isSuccess() == true) {
                            sharedPrefs.save(AppConstants.SHARED_PREFS_USER_DATA, Gson().toJson(data.data))
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
            viewModal.logoutSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.isSuccess() == true) {
                            sharedPrefs.clearPreference()
                            launchActivity<LoginActivity> { }
                            finishAffinity()
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


    class SettingList(
        var name: String,
        var icon: Int,
        var status: Int,
        var notification: Boolean? = false,
        var bioMatrix: Boolean? = false,
    )

    private fun showFeed() {
        val manager = ReviewManagerFactory.create(applicationContext)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                manager.launchReviewFlow(this, reviewInfo)
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
                Log.d("Error", reviewErrorCode.toString())
            }
        }
    }
}
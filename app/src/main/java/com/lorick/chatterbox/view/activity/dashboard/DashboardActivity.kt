package com.lorick.chatterbox.view.activity.dashboard

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lorick.chatterbox.R
import com.lorick.chatterbox.databinding.ActivityDashboardBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.openChromeTab
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.eventDetails.EventDetailActivity
import com.lorick.chatterbox.view.activity.message.MessageActivity
import com.lorick.chatterbox.view.activity.notification.NotificationActivity
import com.lorick.chatterbox.view.activity.onboarding.OnBoardingActivity
import com.lorick.chatterbox.view.activity.setting.SettingScreenActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@DashboardActivity


    @Inject
    lateinit var sharedPrefs : SharedPrefs

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navigation: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        observeDataFromViewModal()
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        callBackHandlingUpdateProfile()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostView) as NavHostFragment
        navController = navHostFragment.navController
        navigation = findViewById(R.id.bottomNavigationView)
        NavigationUI.setupWithNavController(navigation, navHostFragment.navController)
        navController.addOnDestinationChangedListener(this)
        bottomNavigationController()
        hitNotificationStatusAPI()
        onClick()
    }

    private fun hitNotificationStatusAPI() {
        RunInScope.ioThread {
            viewModal.hitNotificationStatusApi()
        }
    }

    private fun callBackHandlingUpdateProfile() {
        NotificationActivity.callbackNotificationStatus = { status->
            if (!status){
               binding.ivNotification.setImageResource(R.drawable.ic_notification)
            }else{
                binding.ivNotification.setImageResource(R.drawable.unseen_notification)
            }
        }
    }

    private fun onClick() {
        binding.apply {
            ivNotification.setOnClickListener {
                launchActivity<NotificationActivity> { }
            }
            ivMessage.setOnClickListener {
                launchActivity<MessageActivity> { }
            }
            ivSetting.setOnClickListener {
                launchActivity<SettingScreenActivity> { }
            }
            ivMyUniversity.setOnClickListener {
                openChromeTab("https://www.google.com")
            }
        }
    }

    private fun bottomNavigationController() {
        navigation.setOnItemSelectedListener { item ->
            // Handle item selection
            binding.apply {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        navController.navigate(R.id.homeFragment)
                        tvHeading.gone()
                        ivHeading.gone()
                        ivHomeLogo.visible()
                        item.setIcon(R.drawable.home_active)
                    }

                    R.id.meditationsFragment -> {
                        ivHomeLogo.gone()
                        tvHeading.visible()
                        ivHeading.visible()
                        navController.navigate(R.id.meditationsFragment)
                        tvHeading.text = getString(R.string.meditation)
                        ivHeading.setImageResource(R.drawable.ic_title_meditations)
                        item.setIcon(R.drawable.meditations_active)
                    }

                    R.id.edificationFragment -> {
                        navController.navigate(R.id.edificationFragment)
                        ivHomeLogo.gone()
                        tvHeading.visible()
                        ivHeading.visible()
                        tvHeading.text = getString(R.string.edification)
                        ivHeading.setImageResource(R.drawable.ic_title_edification)
                        item.setIcon(R.drawable.edification_active)
                    }

                    R.id.journalFragment -> {
                        navController.navigate(R.id.journalFragment)
                        ivHomeLogo.gone()
                        tvHeading.visible()
                        ivHeading.visible()
                        tvHeading.text = getString(R.string.journal)
                        ivHeading.setImageResource(R.drawable.ic_title_journal)
                        item.setIcon(R.drawable.journal_active)
                    }

                    R.id.resourceFragment -> {
                        navController.navigate(R.id.resourceFragment)
                        ivHomeLogo.gone()
                        tvHeading.visible()
                        ivHeading.visible()
                        tvHeading.text = getString(R.string.resources)
                        ivHeading.setImageResource(R.drawable.ic_title_resource)
                        item.setIcon(R.drawable.resources_active)
                    }
                }

            }


            // Deselect previously selected item (change its icon to the outline version)
            val menu = navigation.menu
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                if (menuItem != item) {
                    when (menuItem.itemId) {
                        R.id.homeFragment -> menuItem.setIcon(R.drawable.home_light)
                        R.id.meditationsFragment -> menuItem.setIcon(R.drawable.meditation_light)
                        R.id.edificationFragment -> menuItem.setIcon(R.drawable.edification_light)
                        R.id.journalFragment -> menuItem.setIcon(R.drawable.journal_light)
                        R.id.resourceFragment -> menuItem.setIcon(R.drawable.resource_light)
                        // Handle other items similarly
                    }
                }
            }
            true
        }

    }

    /** Handle Deep linking -- get event id from url*/
    private fun handleDeepLink() {
        val data : Uri? = intent?.data
        if(data!=null){
            val eventId = data.getQueryParameter("EventId")?.toInt()
            if (sharedPrefs.getUserLogin()){
                if (eventId != null){
                    val bundle = bundleOf(AppConstants.EVENT_ID to eventId)
                    launchActivity<EventDetailActivity>(0, bundle) { }
                }
            }else{
                launchActivity<OnBoardingActivity> {  }
                finishAffinity()
            }
        }
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishActivity()
        }
    }

    /** Observer Response via View model for*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.notificationStatusSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            setData(data.data)
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

    private fun setData(data: Boolean) {
        Log.d("Dsadadasdasd",data.toString())
        if(data){
            binding.ivNotification.setImageResource(R.drawable.unseen_notification)
        }else{
            binding.ivNotification.setImageResource(R.drawable.ic_notification)
        }
    }

}
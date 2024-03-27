package com.lorick.chatterbox.view.activity.notification

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.notification.NotificationListResponse
import com.lorick.chatterbox.data.response.notification.ReminderData
import com.lorick.chatterbox.databinding.ActivityNotificationBinding
import com.lorick.chatterbox.databinding.ItemNotificationListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.bottomsheet.setreminder.BottomSheetSetReminder
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationActivity : BaseActivity<ActivityNotificationBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@NotificationActivity
    var notificationReminderData : ReminderData? = null

    companion object {
        lateinit var callbackNotificationStatus :(Boolean)->Unit
    }

    override fun getLayoutRes() = R.layout.activity_notification

    override fun initView() {
        setToolbar()
        observeDataFromViewModal()
        apiHit()
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun setToolbar() {
        binding.apply {
            toolbar.tvToolTitle.text = getString(R.string.Notification)
            toolbar.ivNotification.visibility = View.VISIBLE
            toolbar.ivNotification.setOnClickListener {
                val bottomSheetSetReminder = BottomSheetSetReminder(notificationReminderData)
                bottomSheetSetReminder.callbackStatus = {status,id->
                    if (status){
                        notificationReminderData?.reminderTypeId = id
                    }
                }

                bottomSheetSetReminder.show(supportFragmentManager, "")
            }
            toolbar.ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitNotificationListApi()
        }
    }

    /** set recycler view Meditation  List */
    private fun initMeditationRecyclerView(data: ArrayList<NotificationListResponse>) {
        binding.rvNotification.adapter = resourceListAdapter
        resourceListAdapter.submitList(data)
    }


    private val resourceListAdapter =
        object : GenericAdapter<ItemNotificationListBinding, NotificationListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.item_notification_list
            }

            override fun onBindHolder(
                holder: ItemNotificationListBinding,
                dataClass: NotificationListResponse,
                position: Int
            ) {
                holder.apply {
                    tvTitle.text = dataClass.title
                    tvDesc.text = dataClass.body
                }
            }
        }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.notificationListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        notificationReminderData = data?.data?.remiderDetail
                        if (data?.success == true) {
                            if (data.data.notificationList?.isEmpty() == true){
                                binding.noDataFound.visible()
                            }else{
                                binding.noDataFound.gone()
                                callbackNotificationStatus.invoke(false)
                                initMeditationRecyclerView(data.data.notificationList ?: ArrayList())
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

        viewModal.showLoading.observe(activity) {
            if (it) {
                MyProgressBar.showProgress(activity)
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }
}
package com.lorick.chatterbox.view.activity.messageList

import android.util.Log
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.userMessageList.UserMessageListResponse
import com.lorick.chatterbox.databinding.ActivityMessageListBinding
import com.lorick.chatterbox.databinding.ItemMessageListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.MessageListDialog
import com.lorick.chatterbox.view.activity.chatList.UserChatListActivity
import com.lorick.chatterbox.view.activity.chatList.UserChatListActivity.Companion.callbackUserList
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MessageListActivity : BaseActivity<ActivityMessageListBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@MessageListActivity

    override fun getLayoutRes(): Int = R.layout.activity_message_list

    override fun initView() {
        setToolbar()
        observeDataFromViewModal()
        connectSocket()
        clickListeners()
        callBackHandle()
    }

    private fun callBackHandle() {
        callbackUserList ={
            if (it){
                connectSocket()
            }
        }
    }

    private fun connectSocket() {
        viewModal.apply {
            RunInScope.ioThread {
                connectSocket()
            }
        }
    }

    override fun viewModel() {

    }

    private fun clickListeners() {
        binding.apply {
            relativeLayout.setOnClickListener {
                lifecycleScope.launch {
                    val messageListDialog = MessageListDialog()
                    messageListDialog.show(supportFragmentManager, "message_dialog_fragment_tag")
                }
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.message_heading)
            ivBack.setOnClickListener{
                finishActivity()
            }
        }
    }

    /** set recycler view Message  List */
    private fun initMessageListRecyclerView(isResponse: ArrayList<UserMessageListResponse>) {
        binding.rvNotification.adapter = resourceListAdapter
        resourceListAdapter.submitList(isResponse)
    }

    private val resourceListAdapter = object : GenericAdapter<ItemMessageListBinding, UserMessageListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_message_list
        }

        override fun onBindHolder(holder: ItemMessageListBinding, dataClass: UserMessageListResponse, position: Int) {
            holder.tvUserName.text = dataClass.FullName
            holder.tvEmail.text = dataClass.Email
            holder.circleImageView.setImageFromUrl(R.drawable.placeholder_mind,dataClass.Image)

            holder.root.setOnClickListener {
                val bundle = bundleOf(AppConstants.RECEIVER_USER_ID to dataClass.OtherUserId)
                launchActivity<UserChatListActivity>(0,bundle) {  }
            }
        }
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.chatUsersListSharedFlow.collectLatest { isResponse ->
                Log.d("DATA_MESSAGE", Gson().toJson(isResponse).toString())
                if (!isResponse.Data.isNullOrEmpty()){
                    binding.noChatsFound.gone()
                    initMessageListRecyclerView(isResponse.Data ?: arrayListOf())
                }else{
                       binding.noChatsFound.visible()
                    }
                }
            }

        viewModal.showLoading.observe(activity) {
            binding.apply {
                if (it){
                    rvNotification.gone()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }else{
                    rvNotification.visible()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }
            }
        }
    }

    override fun finish() {
        super.finish()
//       viewModal.disconnectSocket()
    }
}
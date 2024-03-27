package com.lorick.chatterbox.view.activity.chatList

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.userMessageList.MessageListResponse
import com.lorick.chatterbox.databinding.ActivityUserChatListBinding
import com.lorick.chatterbox.databinding.ItemChatListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.convertDateFormatIntoTime
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserChatListActivity : BaseActivity<ActivityUserChatListBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    private var lastVisibleItemPosition = 0
    private var isLastItemVisible: Boolean = false
    var activity = this@UserChatListActivity
    private var myList: ArrayList<MessageListResponse> = arrayListOf()

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun getLayoutRes(): Int = R.layout.activity_user_chat_list

    override fun initView() {
        setToolbar()
        getIntentData()
        observeDataFromViewModal()
        getSocketReceiverResponse()
        clickListener()
        setScrollListener()
        setKeyboardListener()
    }

    companion object{
        lateinit var callbackUserList :(Boolean)->Unit
    }

    /** get chat list and
     * get receiver message from socket*/
    private fun getSocketReceiverResponse() {
        viewModal.apply {
            RunInScope.ioThread {
                getChatList()
                getReceiverMessages()
            }
        }
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            viewModal.otherUSerId.set(intent.getInt(AppConstants.RECEIVER_USER_ID))
        }
    }

    private fun setKeyboardListener() {
        if (myList.isNotEmpty()){
            val handler = Handler(Looper.getMainLooper())
            binding.root.viewTreeObserver.addOnGlobalLayoutListener {
                val r = Rect()
                binding.root.getWindowVisibleDisplayFrame(r)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - r.bottom
                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is visible, check if RecyclerView is scrolled to the last position
                    val lastPosition = chatListAdapter.itemCount - 1
                    val pos = lastVisibleItemPosition
                    isLastItemVisible = pos == lastPosition
                    // Scroll to the last position with a delay after the keyboard opens
                    if (isLastItemVisible) {
                        handler.postDelayed({
                            binding.rvNotification.smoothScrollToPosition(lastPosition)
                        }, 100)
                    }
                }
            }
        }
    }

    private fun setScrollListener() {
        val layoutManager = binding.rvNotification.layoutManager as LinearLayoutManager
        // Listen to RecyclerView scroll events to update lastVisibleItemPosition
        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            }
        })
    }

    private fun clickListener() {
        binding.apply {
            btnSend.setOnClickListener {
                if (validator.isValidChatMessage(activity, binding)) {
                    RunInScope.ioThread {
                        viewModal.sendMessage()
                        callbackUserList(true)
                    }
                }
            }
            etMessage.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (validator.isValidChatMessage(activity, binding)) {
                        RunInScope.ioThread {
                            viewModal.sendMessage()
                            callbackUserList(true)
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    /** set data in list when receive any message*/
    @SuppressLint("NotifyDataSetChanged")
    private fun sendMessage(isResponse: MessageListResponse) {
           Log.d("dkmnfkdsfnd",isResponse.toString())
            val messageData = MessageListResponse(isResponse.ChatId,isResponse.Image,isResponse.Message,isResponse.MessageOn,isResponse.MessageType, isResponse.Name, isResponse.OtherUserId, isResponse.UserId,isResponse.status,isResponse.IsRead)
            myList.add(messageData)
            chatListAdapter.submitList(myList)
            binding.rvNotification.scrollToPosition(chatListAdapter.itemCount - 1)
            lastVisibleItemPosition = chatListAdapter.itemCount - 1
            binding.etMessage.text?.clear()
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }


    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.message_heading)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }


    /** set recycler view Chat List */
    private fun initChatListRecyclerView(messages: ArrayList<MessageListResponse>) {
        myList = messages
        binding.rvNotification.adapter = chatListAdapter
        chatListAdapter.submitList(myList)
        val itemCount = chatListAdapter.itemCount
        lastVisibleItemPosition = itemCount - 1
        binding.rvNotification.scrollToPosition(lastVisibleItemPosition)
    }

    private val chatListAdapter = object : GenericAdapter<ItemChatListBinding, MessageListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_chat_list
        }

        override fun onBindHolder(holder: ItemChatListBinding, dataClass: MessageListResponse, position: Int) {
            if (sharedPrefs.getUserData()?.userId == dataClass.UserId){
                holder.apply {
                    tvMyMessage.text = dataClass.Message.toString().trim()
                    tvTimeMy.text = convertDateFormatIntoTime(dataClass.MessageOn)
                    myLayout.visible()
                    userMessageLayout.gone()

                    if (dataClass.IsRead == true){
                        ivMessageSeen.setBackgroundResource(R.drawable.ic_message_seen)
                    }else{
                        ivMessageSeen.setBackgroundResource(R.drawable.ic_message_un_seen)
                    }
                }
            } else {
                holder.apply {
                    myLayout.gone()
                    userMessageLayout.visible()
                    tvUserMessage.text = dataClass.Message.toString().trim()
                    tvTimeUser.text = convertDateFormatIntoTime(dataClass.MessageOn)
                }
            }
            holder.root.setOnClickListener {

            }
        }
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.messageListSharedFlow.collectLatest { isResponse ->
                Log.d("DATA_MESSAGE", Gson().toJson(isResponse).toString())
                RunInScope.mainThread {
                    initChatListRecyclerView(isResponse.Messages ?: arrayListOf())
                }
            }
        }

        lifecycleScope.launch {
            viewModal.sendMessageSharedFlow.collectLatest { isResponse ->
                Log.d("DATA_MESSAGE", Gson().toJson(isResponse).toString())
                sendMessage(isResponse)
                /** calling again bcz socket disconnected cant fetch receiver messages*/
                viewModal.getReceiverMessages()
            }
        }

        viewModal.showLoading.observe(activity) {
            binding.apply {
                if (it){
                    linearProgressIndicator.visible()
                }else{
                    linearProgressIndicator.gone()
                }
            }
        }
    }

    override fun finishActivity(requestCode: Int) {
        super.finishActivity(requestCode)
        callbackUserList(true)
    }
}
package com.lorick.chatterbox.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.data.response.chatUsers.ChatUsers
import com.lorick.chatterbox.databinding.ItemMessageListBinding
import com.lorick.chatterbox.databinding.MessageDialogBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.setSearchTextWatcher
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.view.activity.chatList.UserChatListActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessageListDialog : DialogFragment() {
    private val viewModal: HomeViewModel by viewModels()
    var binding : MessageDialogBinding? = null
    private var filteredList: ArrayList<ChatUsers> = arrayListOf()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create the dialog builder
        val builder = AlertDialog.Builder(requireActivity(),R.style.FullHeightDialog)
        binding = MessageDialogBinding.inflate(layoutInflater)

        // Customize the dialog
        builder.setView(binding!!.root)
        observeDataFromViewModal()
        RunInScope.ioThread {
            viewModal.hitChatUsersApi()
        }
        val alertDialog = builder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()
        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataFromViewModal()
    }


    /** Observer Response via View model*/
    fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.chatUsersSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            filteredList = data.data
                            getWatcherSearchMeditation()
                            initMessageListRecyclerView()
                        } else {
                        showErrorSnack(requireActivity(), data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg ->
                        showErrorSnack(requireActivity(), msg)
                        }
                    }
                }
            }
        }

        viewModal.showLoading.observe(requireActivity()) {
            binding?.apply {
                shimmerCommentList.shimmerAnimationEffect(it)
            }
        }
    }


    private fun getWatcherSearchMeditation() {
        // Call the extension function to set up the text watcher
        binding?.etSearch?.setSearchTextWatcher { query ->
            if (query.isEmpty()){
                binding?.ivImg?.setImageResource(R.drawable.search)
            }else{
                binding?.ivImg?.setImageResource(R.drawable.rating_close)
            }
            filter(query)
        }
    }

    /** Function to filter data based on search query*/
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val list = if (query.isEmpty()) {
            filteredList
        } else {
            filteredList.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }
        if(list.isEmpty()){
            resourceListAdapter.submitList(list)
        }else{
            resourceListAdapter.submitList(list)
        }
    }


    /** set recycler view Message  List */
    private fun initMessageListRecyclerView() {
        binding?.rvNotification?.adapter = resourceListAdapter
        resourceListAdapter.submitList(filteredList)
    }

    private val resourceListAdapter = object : GenericAdapter<ItemMessageListBinding, ChatUsers>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_message_list
        }

        override fun onBindHolder(holder: ItemMessageListBinding, dataClass: ChatUsers, position: Int) {
            holder.tvUserName.text = dataClass.name
            holder.tvEmail.text = dataClass.email
            holder.circleImageView.setImageFromUrl(R.drawable.placeholder_mind,dataClass.userImage)

            holder.root.setOnClickListener {
                val bundle = bundleOf(AppConstants.RECEIVER_USER_ID to dataClass.userId)
                requireActivity().launchActivity<UserChatListActivity>(0,bundle) {  }
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
package com.lorick.chatterbox.view.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseFragment
import com.lorick.chatterbox.data.response.home.EventListResponse
import com.lorick.chatterbox.databinding.FragmentHomeBinding
import com.lorick.chatterbox.databinding.ItemHomeBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.getTimeInAgo
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.hideKeyboard
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.startZoomMeeting
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.eventDetails.EventDetailActivity
import com.lorick.chatterbox.view.bottomsheet.comments.BottomSheetComments
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val viewModal: HomeViewModel by viewModels()
    lateinit var listCallBackSheet: (Boolean) -> Unit
    private var eventList: ArrayList<EventListResponse>? = arrayListOf()

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        setViewModel()
        observeDataFromViewModal()
        apiHit()
        getCallbackFromEventList()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                apiHit()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCallbackFromEventList() {
        EventDetailActivity.listCallBackSheet = { positions, status, commentCount, likesCount, isLiked ->
                if (status) {
                    eventList!![positions].totalComments = commentCount
                    eventList!![positions].totalFavourites = likesCount
                    if (isLiked == 1) {
                        eventList!![positions].isFavoritedbyUser = false
                    } else if (isLiked == 2) {
                        eventList!![positions].isFavoritedbyUser = true
                    }
                    postListAdapter.submitList(eventList)
                    postListAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun setViewModel() {
        binding.viewModel = viewModal
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitEventListApi()
        }
    }

    /** set recycler view Donation List */
    private fun initPostRecyclerView(data: ArrayList<EventListResponse>) {
        binding.rvPosts.adapter = postListAdapter
        postListAdapter.submitList(data)
    }

    private val postListAdapter = object : GenericAdapter<ItemHomeBinding, EventListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_home
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindHolder(holder: ItemHomeBinding, dataClass: EventListResponse, position: Int) {
            holder.apply {
                tvDesc.text = dataClass.eventDesc
                ivAddComment.setImageFromUrl(R.drawable.no_image_placeholder,sharedPrefs.getUserData()?.userImage ?: "")
                ivHome.setImageFromUrl(R.drawable.placeholder_mind, dataClass.videoThumbImage)

                tvViewComments.text = getString(R.string.view_all).plus(dataClass.totalComments.toString().plus(" ").plus("comments"))
                tvLikes.text = dataClass.totalFavourites.toString().plus(" ").plus(getString(R.string._100_likes))
                tvTime.text = getTimeInAgo(dataClass.createdOn)

                if (dataClass.isImage) {
                    includeVideoUi.gone()
                } else {
                    includeVideoUi.visible()
                }

                root.setOnClickListener {
                    val bundle = bundleOf(
                        AppConstants.EVENT_ID to dataClass.eventId,
                        AppConstants.POSITION to position
                    )
                    requireActivity().launchActivity<EventDetailActivity>(0, bundle) { }
                }

                tvViewComments.setOnClickListener {
                    val bottomSheetComments = BottomSheetComments(dataClass.eventId.toString())
                    bottomSheetComments.show(requireActivity().supportFragmentManager, "")
                    bottomSheetComments.dismissCallBackSheet = { status, count ->
                        if (status) {
                            dataClass.totalComments = count
                            notifyDataSetChanged()
                        }
                    }
                }

                ivShare.setOnClickListener {
                    shareEvent(dataClass.eventId)
                }

                ivComment.setOnClickListener {
                    val bottomSheetComments = BottomSheetComments(dataClass.eventId.toString())
                    bottomSheetComments.show(childFragmentManager, "")
                    bottomSheetComments.dismissCallBackSheet = { status, count ->
                        if (status) {
                            dataClass.totalComments = count
                            notifyDataSetChanged()
                        }
                    }
                }

                ivLike.setOnClickListener {
                    viewModal.eventId.set(dataClass.eventId.toString())
                    if (dataClass.isFavoritedbyUser) {
                        viewModal.isFavourite.set(false)
                    } else {
                        viewModal.isFavourite.set(true)
                    }
                    RunInScope.ioThread {
                        viewModal.hitUpdateFavouriteEventStatusApi()
                    }
                    listCallBackSheet = { status ->
                        if (status) {
                            if (dataClass.isFavoritedbyUser) {
                                dataClass.totalFavourites = dataClass.totalFavourites - 1
                                dataClass.isFavoritedbyUser = false
                            } else {
                                dataClass.totalFavourites = dataClass.totalFavourites + 1
                                dataClass.isFavoritedbyUser = true
                            }
                            notifyDataSetChanged()
                        }
                    }
                }

                holder.etAddComment.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        viewModal.commentDesc.set(holder.etAddComment.text?.trim().toString())
                        viewModal.eventId.set(dataClass.eventId.toString())
                        RunInScope.ioThread {
                            viewModal.hitAddCommentApi()
                        }
                        listCallBackSheet = { status ->
                            if (status) {
                                dataClass.totalComments = dataClass.totalComments + 1
                                holder.etAddComment.text?.clear()
                                notifyDataSetChanged()
                            }
                        }
                        true
                    } else {
                        false
                    }
                }


                layoutZoom.setOnClickListener {
                    startZoomMeeting(requireContext(), "2324")
                }

                if (dataClass.isFavoritedbyUser) {
                    ivLike.setBackgroundResource(R.drawable.liked_ic)
                } else {
                    ivLike.setBackgroundResource(R.drawable.ic_heart)
                }
            }
        }
    }

    /** Share  event details  other apps */
    private fun shareEvent(eventId: Int) {
        val deepLinkUrl = Uri.parse("https://mind.harishparas.com/share.html?EventId=$eventId")
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this event: $deepLinkUrl")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share using"))
    }

    /** Observer Response via View model*/
    @SuppressLint("NotifyDataSetChanged")
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.eventListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            eventList = data.data
                            initPostRecyclerView(data.data)
                            binding.swipeRefreshLayout.isRefreshing = false
                        } else {
                            showErrorSnack(requireActivity(), data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg -> showErrorSnack(requireActivity(), msg) }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModal.favouriteEventStatusSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            listCallBackSheet.invoke(true)
                        } else {
                            showErrorSnack(requireActivity(), data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg -> showErrorSnack(requireActivity(), msg) }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModal.addCommentSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            listCallBackSheet.invoke(true)
                            hideKeyboard(requireActivity())
                        } else {
                            showErrorSnack(requireActivity(), data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg -> showErrorSnack(requireActivity(), msg) }
                    }
                }
            }
        }

        viewModal.showLoading.observe(requireActivity()) {
            if (it) {
                binding.rvPosts.gone()
                binding.mainShimmerLayout.visible()
            } else {
                binding.rvPosts.visible()
                binding.mainShimmerLayout.gone()
            }
        }
    }
}

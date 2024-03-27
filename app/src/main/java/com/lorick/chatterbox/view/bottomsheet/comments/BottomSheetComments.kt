package com.lorick.chatterbox.view.bottomsheet.comments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lorick.chatterbox.R
import com.lorick.chatterbox.data.response.eventDetails.CommentListResponse
import com.lorick.chatterbox.databinding.BottomsheetCommentsBinding
import com.lorick.chatterbox.databinding.ItemCommentsBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.getTimeInAgo
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.hideKeyboard
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetComments(private var eventId: String) : BottomSheetDialogFragment(), View.OnClickListener {
    private val viewModal: HomeViewModel by viewModels()
    private var commentList : ArrayList<CommentListResponse>? = arrayListOf()
    private var binding: BottomsheetCommentsBinding? = null
    private var sheetBehavior: BottomSheetBehavior<View>? = null
    lateinit var dismissCallBackSheet : (Boolean,Int) -> Unit

    @Inject
    lateinit var validator: Validator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_comments, container, false) as BottomsheetCommentsBinding
        setPeekHeight()
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewModel = viewModal
        onClickListener()
        observeDataFromViewModal()
        hitApi()
        setSpinner()
    }

    private fun hitApi() {
        viewModal.eventId.set(eventId)
        RunInScope.ioThread {
            viewModal.hitCommentListApi()
        }
    }

    /** set Spinner Top Comment */
    private fun setSpinner() {
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.comment_list, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerComment?.adapter = adapter
    }

    private fun setPeekHeight() {
        dialog?.setOnShowListener {
            val dialogParent = binding?.layoutCoordinate?.parent as View
            sheetBehavior = BottomSheetBehavior.from(dialogParent)
            sheetBehavior?.peekHeight = (binding?.layoutCoordinate?.height!!  * 100).toInt()
            dialogParent.requestLayout()
        }
    }


    override fun getTheme(): Int {
        return R.style.CustomBottomSheetTheme
    }


    @SuppressLint("ClickableViewAccessibility")
    fun onClickListener() {
        binding?.apply {
            btnSend.setOnClickListener {
                if (validator.validateComment(requireActivity(), binding)) {
                    RunInScope.ioThread {
                        viewModal.hitAddCommentApi()
                    }
                }
            }

            rvComments.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    sheetBehavior?.setDraggable(false)
                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    sheetBehavior?.isDraggable = true
                }
                view.onTouchEvent(motionEvent)
                true
            }

            etComment.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (validator.validateComment(requireActivity(), binding)) {
                        RunInScope.ioThread {
                            viewModal.hitAddCommentApi()
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }
    }


    /** set recycler view Comment List */
    private fun initLikesRecyclerView() {
        binding?.rvComments?.adapter = commentsAdapter
        commentsAdapter.submitList(commentList)
    }

    private val commentsAdapter = object : GenericAdapter<ItemCommentsBinding, CommentListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_comments
        }

        override fun onBindHolder(holder: ItemCommentsBinding, dataClass: CommentListResponse, position: Int) {
            holder.apply {
                tvUserName.text = dataClass.commentedByName
                tvComment.text = dataClass.commentDesc
                circleImageView.setImageFromUrl(R.drawable.no_image_placeholder,dataClass.commentedByImage)
                tvOneDayAgo.text = getTimeInAgo(dataClass.commentedOn)

                // Check if it's the last item in the list
                if (position == itemCount - 1) {
                    view.gone()
                } else {
                    // Show the view for other items
                    view.visible()
                }
            }
        }
    }

    /** Observer Response via View model*/
    @SuppressLint("NotifyDataSetChanged")
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.commentListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            commentList = data.data ?: arrayListOf()
                            initLikesRecyclerView()
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
                            val list =  CommentListResponse(data.data.commentDesc,data.data.commentId,data.data.commentedById,data.data.commentedByImage,data.data.commentedByName,data.data.commentedOn,data.data.eventId)
                            commentList?.add(0,list)
                            commentsAdapter.submitList(commentList)
                            commentsAdapter.notifyDataSetChanged()
                            hideKeyboard(requireActivity())
                            dismissCallBackSheet.invoke(true,commentList?.size ?: 0)
                            viewModal.commentDesc.set("")
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
                binding?.rvComments?.gone()
                binding?.shimmerCommentList?.apply {
                    visible()
                    startShimmer()
                }
            } else {
                binding?.shimmerCommentList?.apply {
                    RunInScope.mainThread {
                        stopShimmer()
                        gone()
                        binding!!.rvComments.visible()
                    }
                }
            }
        }
    }


    override fun onClick(p0: View?) {
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallBackSheet.invoke(true,commentList?.size ?: 0)
    }

}
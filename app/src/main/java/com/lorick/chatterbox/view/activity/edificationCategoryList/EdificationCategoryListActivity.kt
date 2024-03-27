package com.lorick.chatterbox.view.activity.edificationCategoryList

import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.edification.EdificationTypeListResponse
import com.lorick.chatterbox.databinding.ActivityEdificationCategoryListBinding
import com.lorick.chatterbox.databinding.RowitemEdificationlistBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.edificationVideoPlayer.EdificationVideoPlayerActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EdificationCategoryListActivity : BaseActivity<ActivityEdificationCategoryListBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@EdificationCategoryListActivity
    private lateinit var categoryId: String
    private lateinit var catName: String
    private var list : ArrayList<EdificationTypeListResponse> = arrayListOf()

    override fun getLayoutRes() = R.layout.activity_edification_category_list

    override fun initView() {
        getIntentData()
        setToolbar()
        initData()
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            categoryId = intent.getString(AppConstants.EDIFICATION_CAT_ID).toString()
            catName = intent.getString(AppConstants.CAT_NAME).toString()
            viewModal.categoryId.set(categoryId)
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun initData() {
        apiHit()
        observeDataFromViewModal()
    }

    /** Meditation List api Call*/
    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitEdificationListApi()
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = catName
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    /** set recycler view Meditation  List */
    private fun initEdificationTypeRecyclerView(data: ArrayList<EdificationTypeListResponse>) {
        binding.rvCategoryEdification.adapter = categoryEdificationListAdapter
        categoryEdificationListAdapter.submitList(data)
    }

    private val categoryEdificationListAdapter =
        object : GenericAdapter<RowitemEdificationlistBinding, EdificationTypeListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.rowitem_edificationlist
            }

            override fun onBindHolder(
                holder: RowitemEdificationlistBinding,
                dataClass: EdificationTypeListResponse,
                position: Int
            ) {
                holder.apply {

                    tvName.text = dataClass.title
                    tvDec.text = dataClass.content
                    tvDuration.text = dataClass.duration
                    ivImage.setImageFromUrl(dataClass.videoThumbImage, progressBar)
                    tvDuration.text = dataClass.duration

                    root.setOnClickListener {
                        val dataJson = Gson().toJson(dataClass)
                        val dataList = Gson().toJson(list)
                        val bundle = bundleOf(
                            AppConstants.EDIFICATION_CAT_ID to categoryId,
                            AppConstants.EDIFICATION_DATA to dataJson,
                            AppConstants.EDIFICATION_TYPE_LIST to dataList
                        )
                        launchActivity<EdificationVideoPlayerActivity>(0, bundle) { }
                    }
                }
            }
        }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.edificationListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if (data.data.isEmpty()) {
                                binding.noDataFound.visible()
                            } else {
                                binding.noDataFound.gone()
                                list = data.data
                                initEdificationTypeRecyclerView(data.data)
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
                binding.apply {
                    rvCategoryEdification.gone()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }
            } else {
                binding.apply {
                    rvCategoryEdification.visible()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }
            }
        }
    }

}
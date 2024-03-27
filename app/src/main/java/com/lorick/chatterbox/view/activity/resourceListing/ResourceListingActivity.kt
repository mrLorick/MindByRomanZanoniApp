package com.lorick.chatterbox.view.activity.resourceListing

import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.resource.ResourceTypeList
import com.lorick.chatterbox.databinding.ActivityResourceListingBinding
import com.lorick.chatterbox.databinding.RowitemResourceslistBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.nowPlaying.NowPlayingActivity
import com.lorick.chatterbox.view.activity.openPdfViewer.OpenPdfActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResourceListingActivity : BaseActivity<ActivityResourceListingBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    private val activity = this@ResourceListingActivity
    private var catName = ""

    override fun getLayoutRes() = R.layout.activity_resource_listing

    override fun initView() {
        getIntentData()
        seToolBar()
        observeDataFromViewModal()
        hitResourceTypeListApi()
    }

    private fun hitResourceTypeListApi() {
        RunInScope.ioThread {
            viewModal.hitResourceListByTypeApi()
        }
    }

    override fun viewModel() {

    }

    private fun seToolBar() {
        binding.toolbar.apply {
            tvToolTitle.text = catName
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    /** get Intent data from
     * MeditationsFragment
     * */
    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            viewModal.resourceTypeId.set(intent.getString(AppConstants.RESOURCE_TYPE_ID).toString())
            catName = intent.getString(AppConstants.CAT_NAME).toString()
        }
    }


    /** set recycler view Meditation  List */
    private fun initMeditationRecyclerView(data: ArrayList<ResourceTypeList>) {
        binding.rvResourcesList.adapter = resourcesCategoryListAdapter
        resourcesCategoryListAdapter.submitList(data)
    }

    private val resourcesCategoryListAdapter =
        object : GenericAdapter<RowitemResourceslistBinding, ResourceTypeList>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.rowitem_resourceslist
            }

            override fun onBindHolder(holder: RowitemResourceslistBinding,  dataClass: ResourceTypeList, position: Int) {
                holder.apply {
                    holder.apply {
                        ivImage.setImageFromUrl(R.drawable.placeholder_mind,dataClass.imageName)
                        tvTitle.text = dataClass.title
                        tvDesc.text = dataClass.content
                        tvDuration.text = dataClass.duration
                    }
                    root.setOnClickListener {
                        val dataJson = Gson().toJson(dataClass)
                        val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.RESOURCE_SCREEN,AppConstants.RESOURCE_DETAILS to dataJson)
                        launchActivity<NowPlayingActivity>(0,bundle) { }
                    }
                    tvViewPdf.setOnClickListener {
                        val bundle = bundleOf(AppConstants.PDF_URL to dataClass.pdfFileName)
                        launchActivity<OpenPdfActivity>(0,bundle) {  }
                    }
                }
            }
        }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.resourceListByTypeSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if(data.data.isEmpty()){
                                binding.noDataFound.visible()
                            }else{
                                binding.noDataFound.gone()
                                initMeditationRecyclerView(data.data)
                            }
                        } else {
                            showErrorSnack(activity, data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg -> showErrorSnack(activity, msg) }
                    }
                }
            }
        }

        viewModal.showLoading.observe(activity) {
            if (it) {
                binding.rvResourcesList.gone()
                binding.shimmerCommentList.apply {
                    visible()
                    startShimmer()
                }
            } else {
                binding.shimmerCommentList.apply {
                    RunInScope.mainThread {
                        stopShimmer()
                        gone()
                        binding.rvResourcesList.visible()
                    }
                }
            }
        }
    }
}
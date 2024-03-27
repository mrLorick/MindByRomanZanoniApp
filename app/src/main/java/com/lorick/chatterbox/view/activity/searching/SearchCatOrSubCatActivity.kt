package com.lorick.chatterbox.view.activity.searching

import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.search.SearchCatOrSubCatListResponse
import com.lorick.chatterbox.databinding.ActivitySearchCatOrSubCatBinding
import com.lorick.chatterbox.databinding.ItemSearchCatOrSubCatBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.setSearchTextWatcher
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.edificationCategoryList.EdificationCategoryListActivity
import com.lorick.chatterbox.view.activity.edificationVideoPlayer.EdificationVideoPlayerActivity
import com.lorick.chatterbox.view.activity.meditationCategoryList.MeditationCategoryListActivity
import com.lorick.chatterbox.view.activity.nowPlaying.NowPlayingActivity
import com.lorick.chatterbox.view.activity.resourceListing.ResourceListingActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchCatOrSubCatActivity : BaseActivity<ActivitySearchCatOrSubCatBinding>() {
    private val viewModal: HomeViewModel by viewModels()

    override fun getLayoutRes(): Int {
        return R.layout.activity_search_cat_or_sub_cat
    }

    override fun initView() {
        setToolbar()
        getIntentData()
        initMeditationRecyclerView()
        observeDataFromViewModal()
        getWatcherSearchMeditation()
    }

    override fun viewModel() {

    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            val screenType = intent.getString(AppConstants.SCREEN_TYPE).toString()
            if (screenType == AppConstants.MEDITATION_SCREEN){
                viewModal.typeId.set(1)
            }else if(screenType == AppConstants.EDIFICATION_SCREEN){
                viewModal.typeId.set(2)
            }else if(screenType == AppConstants.RESOURCE_SCREEN){
                viewModal.typeId.set(3)
            }
        }
    }
    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.search)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }
    private fun getWatcherSearchMeditation() {
        // Call the extension function to set up the text watcher
        binding.etSearchMeditation.setSearchTextWatcher { query ->
            if (query.isEmpty()){
                binding.ivImg.setImageResource(R.drawable.search)
            }else{
                binding.ivImg.setImageResource(R.drawable.rating_close)
            }
            viewModal.searchKeyword.set(query)
            RunInScope.ioThread {
                viewModal.hitSearchApiAccordingToCatOrSubCatApi()
            }
        }
    }

    /** set recycler view Meditation  List */
    private fun initMeditationRecyclerView() {
        binding.rvMeditation.adapter = meditationListAdapter
    }

    /**
     * dataClass.typeId
     * 1-- for main cat
     * 2-- for video or audio
     *
     * viewModal.typeId.get()
     * 1- Meditation
     * 2- Edification
     * 3- Resources
     *
     * */
    private val meditationListAdapter = object : GenericAdapter<ItemSearchCatOrSubCatBinding, SearchCatOrSubCatListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_search_cat_or_sub_cat
        }

        override fun onBindHolder(holder: ItemSearchCatOrSubCatBinding, dataClass: SearchCatOrSubCatListResponse, position: Int) {
            holder.apply {
                if (dataClass.typeId == 1){
                    tvMeditationName.text = dataClass.categoryName
                    tvPlay.gone()
                }else if (dataClass.typeId == 2){
                    tvPlay.visible()
                    tvMeditationName.text = dataClass.title.plus(" (").plus(dataClass.categoryName).plus(")")
                }
                ivImage.setImageFromUrl(R.drawable.placeholder_mind,dataClass.thumbImage)

                root.setOnClickListener {
                    if (dataClass.typeId == 1){
                        if(viewModal.typeId.get() == 1){
                            val bundle = bundleOf(AppConstants.MEDITATION_CAT_ID to dataClass.categoryId.toString(), AppConstants.CAT_NAME to dataClass.categoryName)
                            launchActivity<MeditationCategoryListActivity>(0,bundle) { }
                        } else  if(viewModal.typeId.get() == 3){
                            val bundle = bundleOf(
                                AppConstants.RESOURCE_TYPE_ID to dataClass.categoryId.toString(),
                                AppConstants.CAT_NAME to dataClass.categoryName
                            )
                            launchActivity<ResourceListingActivity>(0, bundle) { }
                        } else  if(viewModal.typeId.get() == 2){
                            val bundle = bundleOf(
                                AppConstants.EDIFICATION_CAT_ID to dataClass.categoryId.toString(),
                                AppConstants.CAT_NAME to dataClass.categoryName
                            )
                            launchActivity<EdificationCategoryListActivity>(0, bundle) { }
                        }
                    }else if (dataClass.typeId == 2){
                        if(viewModal.typeId.get() == 1){
                            val dataJson = Gson().toJson(dataClass)
                            val bundle = bundleOf(
                                AppConstants.SCREEN_TYPE to AppConstants.MEDITATION_SCREEN,
                                AppConstants.MEDIATION_DETAILS to dataJson
                            )
                            launchActivity<NowPlayingActivity>(0, bundle) { }
                        } else  if(viewModal.typeId.get() == 3){
                            val dataJson = Gson().toJson(dataClass)
                            val bundle = bundleOf(
                                AppConstants.SCREEN_TYPE to AppConstants.RESOURCE_SCREEN,
                                AppConstants.RESOURCE_DETAILS to dataJson
                            )
                            launchActivity<NowPlayingActivity>(0, bundle) { }
                        }
                        else  if(viewModal.typeId.get() == 2){
                            val dataJson = Gson().toJson(dataClass)
                            val bundle = bundleOf(
                                AppConstants.EDIFICATION_CAT_ID to dataClass.categoryId,
                                AppConstants.EDIFICATION_DATA to dataJson,
                            )
                            launchActivity<EdificationVideoPlayerActivity>(0, bundle) { }
                        }
                    }
                }
            }
        }
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.searchCatOrSubCatSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if(data.data?.isEmpty() == true){
                                binding.noDataFound.visible()
                            }else {
                                binding.noDataFound.gone()
                            }
                            meditationListAdapter.submitList(data.data ?: arrayListOf())
                        } else {
                            showErrorSnack(this@SearchCatOrSubCatActivity, data?.message ?: "")
                        }
                    }
                    is Resource.Error -> {
                        isResponse.message?.let { msg -> showErrorSnack(this@SearchCatOrSubCatActivity,  msg) }
                    }
                }
            }
        }


        viewModal.showLoading.observe(this@SearchCatOrSubCatActivity) {
            binding.apply {
                if (it) {
                    binding.rvMeditation.gone()
                    binding.mainShimmerLayout.visible()
                } else {
                    binding.rvMeditation.visible()
                    binding.mainShimmerLayout.gone()
                }
            }
        }
    }
}
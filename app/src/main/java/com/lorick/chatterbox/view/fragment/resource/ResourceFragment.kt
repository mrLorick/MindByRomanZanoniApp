package com.lorick.chatterbox.view.fragment.resource

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseFragment
import com.lorick.chatterbox.data.response.resource.ResourceCategoryList
import com.lorick.chatterbox.databinding.FragmentResourceBinding
import com.lorick.chatterbox.databinding.RowitemResourcesBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.resourceListing.ResourceListingActivity
import com.lorick.chatterbox.view.activity.searching.SearchCatOrSubCatActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResourceFragment : BaseFragment<FragmentResourceBinding>(FragmentResourceBinding::inflate) {
    private val viewModal: HomeViewModel by viewModels()
    private var filteredList: ArrayList<ResourceCategoryList> = arrayListOf()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        setViewModel()
        initMeditationRecyclerView()
        observeDataFromViewModal()
        apiHit()
        onClickListener()
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitResourceCategoryApi()
        }
    }

    private fun setViewModel() {
        binding.viewModel = viewModal
    }



    private fun onClickListener() {
        binding.apply {
            binding.apply {
                ivImg.setOnClickListener {
                    etSearchMeditation.text?.clear()
                }
                etSearchMeditation.setOnClickListener {
                    val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.RESOURCE_SCREEN)
                    requireActivity().launchActivity<SearchCatOrSubCatActivity>(0,bundle) {  }
                }
            }
        }
    }

    /** set recycler view Resources  List */
    private fun initMeditationRecyclerView() {
        binding.rvResource.adapter = resourceListAdapter
    }

    private val resourceListAdapter =
        object : GenericAdapter<RowitemResourcesBinding, ResourceCategoryList>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.rowitem_resources
            }

            override fun onBindHolder(
                holder: RowitemResourcesBinding,
                dataClass: ResourceCategoryList,
                position: Int
            ) {
                holder.apply {
                    tvName.text = dataClass.resourceTypeMain
                    ivImage.setImageFromUrl(dataClass.categoryImage, progressBar)

                    cvResourcesItem.setOnClickListener {
                        val bundle = bundleOf(
                            AppConstants.RESOURCE_TYPE_ID to dataClass.resourceTypeId.toString(),
                            AppConstants.CAT_NAME to dataClass.resourceTypeMain
                        )
                        requireActivity().launchActivity<ResourceListingActivity>(0, bundle) { }
                    }
                }

            }
        }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.resourceListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if (data.data?.isEmpty() == true) {
                                binding.noDataFound.visible()
                            } else {
                                binding.noDataFound.gone()
                                resourceListAdapter.submitList(data.data ?: arrayListOf())
                            }
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
            binding.apply {
                mainLayoutShimmer.shimmerAnimationEffect(it)
            }
        }
    }
}
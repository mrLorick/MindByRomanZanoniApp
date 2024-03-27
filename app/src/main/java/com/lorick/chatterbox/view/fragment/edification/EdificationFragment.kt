package com.lorick.chatterbox.view.fragment.edification

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseFragment
import com.lorick.chatterbox.data.response.edification.EdificationCatListResponse
import com.lorick.chatterbox.databinding.FragmentEdificationBinding
import com.lorick.chatterbox.databinding.RowitemEdificationBinding
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
import com.lorick.chatterbox.view.activity.edificationCategoryList.EdificationCategoryListActivity
import com.lorick.chatterbox.view.activity.searching.SearchCatOrSubCatActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EdificationFragment :
    BaseFragment<FragmentEdificationBinding>(FragmentEdificationBinding::inflate) {
    private val viewModal: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        setViewModel()
        clickListener()
        initEdificationRecyclerView()
        observeDataFromViewModal()
        apiHit()
    }

    private fun setViewModel() {
        binding.viewModel = viewModal
    }

    private fun clickListener() {
        binding.apply {
            ivImg.setOnClickListener {
                etSearchMeditation.text?.clear()
            }
            etSearchMeditation.setOnClickListener {
                val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.EDIFICATION_SCREEN)
                requireActivity().launchActivity<SearchCatOrSubCatActivity>(0,bundle) {  }
            }
        }
    }


    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitEdificationCategoryListApi()
        }
    }

    /** set recycler view Edification  List */
    private fun initEdificationRecyclerView() {
        binding.rvEdification.adapter = edificationListAdapter
    }

    private val edificationListAdapter =
        object : GenericAdapter<RowitemEdificationBinding, EdificationCatListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.rowitem_edification
            }

            override fun onBindHolder(
                holder: RowitemEdificationBinding,
                dataClass: EdificationCatListResponse,
                position: Int
            ) {
                holder.apply {

                    tvEdificationName.text = dataClass.ediCategoryName
                    ivImage.setImageFromUrl(dataClass.edificationCategoryImage, progressBar)

                    cvEdificationItem.setOnClickListener {
                        val bundle = bundleOf(
                            AppConstants.EDIFICATION_CAT_ID to dataClass.edificationId.toString(),
                            AppConstants.CAT_NAME to dataClass.ediCategoryName
                        )
                        requireActivity().launchActivity<EdificationCategoryListActivity>(0, bundle) { }
                    }
                }

            }
        }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.edificationCategoryListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if (data.data?.isEmpty() == true) {
                                binding.noDataFound.visible()
                            } else {
                                binding.noDataFound.gone()
                                edificationListAdapter.submitList(data.data ?: arrayListOf())
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
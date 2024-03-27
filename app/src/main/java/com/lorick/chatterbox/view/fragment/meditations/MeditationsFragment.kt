package com.lorick.chatterbox.view.fragment.meditations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseFragment
import com.lorick.chatterbox.data.response.meditation.MeditationCatListResponse
import com.lorick.chatterbox.databinding.FragmentMeditationsBinding
import com.lorick.chatterbox.databinding.RowitemMeditationBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.constant.AppConstants.CAT_NAME
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.setSearchTextWatcher
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.meditationCategoryList.MeditationCategoryListActivity
import com.lorick.chatterbox.view.activity.searching.SearchCatOrSubCatActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeditationsFragment : BaseFragment<FragmentMeditationsBinding>(FragmentMeditationsBinding::inflate) {
    private val viewModal: HomeViewModel by viewModels()
    private var filteredList: ArrayList<MeditationCatListResponse> = arrayListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        setViewModel()
        clickListener()
        apiHit()
        observeDataFromViewModal()
    }

    private fun getWatcherSearchMeditation() {
        binding.etSearchMeditation.isEnabled = true
        // Call the extension function to set up the text watcher
        binding.etSearchMeditation.setSearchTextWatcher { query ->
            if (query.isEmpty()){
                binding.ivImg.setImageResource(R.drawable.search)
            }else{
                viewModal.searchKeyword.set(query)
                viewModal.typeId.set(1)
                RunInScope.ioThread {
                    viewModal.hitSearchApiAccordingToCatOrSubCatApi()
                }
                binding.ivImg.setImageResource(R.drawable.rating_close)
            }
            filter(query)
        }
    }


    private fun clickListener() {
        binding.apply {
            ivImg.setOnClickListener {
                etSearchMeditation.text?.clear()
            }
            etSearchMeditation.setOnClickListener {
                val bundle = bundleOf(AppConstants.SCREEN_TYPE to AppConstants.MEDITATION_SCREEN)
                requireActivity().launchActivity<SearchCatOrSubCatActivity>(0,bundle) {  }
            }
        }
    }


    private fun setViewModel() {
        binding.viewModel = viewModal
    }


    /** Function to filter data based on search query*/
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val list: ArrayList<MeditationCatListResponse> = if (query.isEmpty()) {
            filteredList
        } else {
            filteredList.filter { item ->
                item.categoryName.contains(query, ignoreCase = true)
            } as ArrayList<MeditationCatListResponse>
        }
        if(list.isEmpty()){
            binding.noDataFound.visible()
            meditationListAdapter.submitList(list)
        }else{
            binding.noDataFound.gone()
            meditationListAdapter.submitList(list)
        }
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitMeditationCatListApi()
        }
    }

    /** set recycler view Meditation  List */
    private fun initMeditationRecyclerView() {
        binding.rvMeditation.adapter = meditationListAdapter
        meditationListAdapter.submitList(filteredList)
    }

    private val meditationListAdapter = object : GenericAdapter<RowitemMeditationBinding, MeditationCatListResponse>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.rowitem_meditation
        }

        override fun onBindHolder(holder: RowitemMeditationBinding, dataClass: MeditationCatListResponse, position: Int) {
            holder.apply {
                tvMeditationName.text = dataClass.categoryName
                ivImage.setImageFromUrl(dataClass.categoryImage,progressBar)

                cvMeditationItem.setOnClickListener {
                    val bundle = bundleOf(AppConstants.MEDITATION_CAT_ID to dataClass.medidationCatId.toString(), CAT_NAME to dataClass.categoryName)
                    requireActivity().launchActivity<MeditationCategoryListActivity>(0,bundle) { }
                }
            }
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.meditationCatListSharedFlow.collectLatest {isResponse->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            filteredList = data.data
                            if(data.data.isEmpty()){
                                binding.apply {
                                    noDataFound.visible()
                                    searchLayout.gone()
                                }
                            }else{
                                binding.apply {
                                    searchLayout.visible()
                                    noDataFound.gone()
                                }
                                initMeditationRecyclerView()
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
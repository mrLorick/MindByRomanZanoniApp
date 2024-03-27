package com.lorick.chatterbox.view.fragment.journal

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseFragment
import com.lorick.chatterbox.data.response.journal.JournalListResponse
import com.lorick.chatterbox.databinding.FragmentJournalBinding
import com.lorick.chatterbox.databinding.ItemJournalTableBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.constant.AppConstants.JOURNAL_ID
import com.lorick.chatterbox.utils.constant.AppConstants.POSITION
import com.lorick.chatterbox.utils.formatDate
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.editJournal.EditJournalActivity
import com.lorick.chatterbox.view.activity.editJournal.EditJournalActivity.Companion.callBackAddJournal
import com.lorick.chatterbox.view.activity.editJournal.EditJournalActivity.Companion.callBackUpdateJournal
import com.lorick.chatterbox.view.activity.viewJouranl.ViewJournalActivity
import com.lorick.chatterbox.view.activity.viewJouranl.ViewJournalActivity.Companion.callBackDeleteJournal
import com.lorick.chatterbox.view.bottomsheet.journaltype.BottomSheetJournalType
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalFragment : BaseFragment<FragmentJournalBinding>(FragmentJournalBinding::inflate) {
    private val viewModal: HomeViewModel by viewModels()
    private var bottomSheetJournalType: BottomSheetJournalType? = null
    private var filteredList: ArrayList<JournalListResponse> = arrayListOf()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun setViewModel() {
        binding.viewModel = viewModal
    }

    private fun initData() {
        setViewModel()
        clickListeners()
        apiHit()
        observeDataFromViewModal()
        getWatcherSearchMeditation()
        callbackHandlingJournal()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun callbackHandlingJournal() {
        callBackAddJournal ={
            if (it){
                apiHit()
            }
        }
        callBackUpdateJournal ={
            if (it){
                apiHit()
            }
        }
        callBackDeleteJournal ={position,status ->
            if (status){
                filteredList.removeAt(position)
                postListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitJournalListApi()
        }
    }

    /** Text watcher when user search Journal list*/
    private fun getWatcherSearchMeditation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                viewModal.meditationSearchText.set(p0.toString())
                filter(p0.toString())
            }
        }
        binding.etsearch.addTextChangedListener(watcher)
    }


    /** Function to filter data based on search query*/
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val list: ArrayList<JournalListResponse>
        if (query.isEmpty()) {
            list = filteredList
            binding.ivCross.gone()
        } else {
            list = filteredList.filter { item ->
                item.subject.contains(query, ignoreCase = true)
            } as ArrayList<JournalListResponse>
            binding.ivCross.visible()
        }
        postListAdapter.submitList(list)
    }


    private fun clickListeners() {
        binding.apply {
            btnNewEntry.setOnClickListener {
                bottomSheetJournalType = BottomSheetJournalType()
                bottomSheetJournalType?.show(requireActivity().supportFragmentManager, "")
            }
            ivCross.setOnClickListener {
                etsearch.text?.clear()
            }
        }
    }

    /** set recycler view Donation List */
    private fun initPostRecyclerView() {
        binding.rvTable.adapter = postListAdapter
        postListAdapter.submitList(filteredList)
    }

    private val postListAdapter = object : GenericAdapter<ItemJournalTableBinding, JournalListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.item_journal_table
            }

            override fun onBindHolder(holder: ItemJournalTableBinding, dataClass: JournalListResponse, position: Int) {
                holder.apply {
                    tvSubject.text = dataClass.subject
                    tvType.text = dataClass.type
                    tvDate.text = formatDate(dataClass.journalDate)

                    holder.root.setOnClickListener {
                        val bundle = bundleOf(
                            JOURNAL_ID to dataClass.journalId.toString(),
                            POSITION to position.toString(),
                            AppConstants.ENTRY_TYPE to dataClass.typeId.toString())
                        requireActivity().launchActivity<ViewJournalActivity>(0, bundle) { }
                    }
                    tvEdit.setOnClickListener {
                        val bundle = bundleOf(
                            AppConstants.SCREEN_TYPE to "EDIT",
                            JOURNAL_ID to dataClass.journalId.toString(),
                            AppConstants.ENTRY_TYPE to dataClass.typeId.toString()
                        )
                        requireActivity().launchActivity<EditJournalActivity>(0, bundle) { }
                    }
                }
            }
        }


    override fun onResume() {
        super.onResume()
        bottomSheetJournalType?.dismiss()
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.journalListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            filteredList = data.data
                            initPostRecyclerView()
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
                binding.apply {
                    rvTable.gone()
                    mainLayoutShimmer.shimmerAnimationEffect(it)
                }
            } else {
                binding.apply {
                    rvTable.visible()
                    mainLayoutShimmer.shimmerAnimationEffect(it)
                }
            }
        }
    }
}
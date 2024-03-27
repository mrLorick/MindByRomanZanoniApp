package com.lorick.chatterbox.view.activity.viewJouranl

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.journal.ViewJournalsDataResponse
import com.lorick.chatterbox.databinding.ActivityViewJournalBinding
import com.lorick.chatterbox.databinding.ItemProsConsListNotEditBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.changeDate
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.isNegative
import com.lorick.chatterbox.utils.isPositive
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.editJournal.EditJournalActivity
import com.lorick.chatterbox.view.activity.editJournal.EditJournalActivity.Companion.callBackUpdateJournal
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewJournalActivity : BaseActivity<ActivityViewJournalBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    private var activity = this@ViewJournalActivity
    private var entryType = ""
    private var position = ""
    private var prosOneList : ArrayList<EditJournalActivity.WeightMethodItems>?= arrayListOf()
    private var prosTwoList : ArrayList<EditJournalActivity.WeightMethodItems>?= arrayListOf()
    private var crossOneList : ArrayList<EditJournalActivity.WeightMethodItems>?= arrayListOf()
    private var crossTwoList : ArrayList<EditJournalActivity.WeightMethodItems>?= arrayListOf()
    private var subTotalProsOne = 0
    private var subTotalProsTwo = 0
    private var subTotalConsOne = 0
    private var subTotalConsTwo = 0
    private var weightProOne = 0
    private var weightProTwo = 0
    companion object{
        lateinit var callBackDeleteJournal :(Int,Boolean) ->Unit
    }

    override fun getLayoutRes(): Int = R.layout.activity_view_journal

    override fun initView() {
        getIntentData()
        observeDataFromViewModal()
        setOnClickListener()
        initJournalOneRecyclerView()
        initJournalProsTwoRecyclerView()
        initJournalConsOneRecyclerView()
        initJournalConsTwoRecyclerView()
        hitApi()
        callbackHandlingJournal()
    }

    private fun hitApi() {
        RunInScope.ioThread {
            viewModal.hitGetJournalDetailApi()
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }


    private fun callbackHandlingJournal() {
        callBackUpdateJournal ={
            if (it){
                hitApi()
            }
        }
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            position = intent.getString(AppConstants.POSITION).toString()
            viewModal.journalID.set(intent.getString(AppConstants.JOURNAL_ID).toString())
            entryType = intent.getString(AppConstants.ENTRY_TYPE).toString()
            viewHandling()
        }
    }
    private fun setDataOnView(journalListResponse: ViewJournalsDataResponse) {
        binding.apply {
            viewModal.subject.set(journalListResponse.subject)
            viewModal.notes.set(journalListResponse.notes)
            viewModal.journalID.set(journalListResponse.journalId.toString())
            viewModal.date.set(changeDate(journalListResponse.journalDate))

            /**
             *  2--Jot Method
             * 3--Weight method
             * */
            if (entryType == "2"){
                if (journalListResponse.weightJournalNotes?.isNotEmpty() == true){
                    etNoteOne.text = journalListResponse.weightJournalNotes[0].notesTitle
                    etNoTwo.text = journalListResponse.weightJournalNotes[1].notesTitle
                    etNoteOneDec.text = journalListResponse.weightJournalNotes[0].notesPros
                    etNoteTwoDec.text = journalListResponse.weightJournalNotes[1].notesPros
                }
            }else if (entryType == "3"){
                if (journalListResponse.weightJournalNotesAs?.isNotEmpty() == true){
                    val prosOne = journalListResponse.weightJournalNotesAs[0].weightItemsAs ?: arrayListOf()
                    val consOne = journalListResponse.weightJournalNotesAs[1].weightItemsAs ?: arrayListOf()

                    for (i in prosOne.indices){
                        prosOneList?.add(
                            EditJournalActivity.WeightMethodItems(
                                prosOne[i].itemDesc ?: "",
                                prosOne[i].typeId ?: 0
                            )
                        )
                    }
                    for (i in consOne.indices){
                        crossOneList?.add(
                            EditJournalActivity.WeightMethodItems(
                                consOne[i].itemDesc ?: "",
                                consOne[i].typeId ?: 0
                            )
                        )
                    }

                    subTotalProsOne = journalListResponse.weightJournalNotesAs[0].totalPoints ?: 0
                    subTotalConsOne = journalListResponse.weightJournalNotesAs[1].totalPoints ?: 0

                    etNoteFourOne.text = journalListResponse.weightJournalNotesAs[0].notesTitle ?: "0"

                }

                if (journalListResponse.weightJournalNotesBs?.isNotEmpty() == true){
                    val prosTwo = journalListResponse.weightJournalNotesBs[0].weightItemsBs ?: arrayListOf()
                    val consTwo = journalListResponse.weightJournalNotesBs[1].weightItemsBs ?: arrayListOf()

                    for (i in prosTwo.indices){
                        prosTwoList?.add(
                            EditJournalActivity.WeightMethodItems(
                                prosTwo[i].itemDesc ?: "",
                                prosTwo[i].typeId ?: 0
                            )
                        )
                    }
                    for (i in consTwo.indices){
                        crossTwoList?.add(
                            EditJournalActivity.WeightMethodItems(
                                consTwo[i].itemDesc ?: "",
                                consTwo[i].typeId ?: 0
                            )
                        )
                    }

                    subTotalProsTwo = journalListResponse.weightJournalNotesBs[0].totalPoints ?: 0
                    subTotalConsTwo = journalListResponse.weightJournalNotesBs[1].totalPoints ?: 0

                    etNoteFourTwo.text = journalListResponse.weightJournalNotesBs[0].notesTitle ?: "0"

                }

                weightProOne = subTotalProsOne.minus(subTotalConsOne)
                weightProTwo = subTotalProsTwo.minus(subTotalConsTwo)

                tvWhtCount.text = weightProOne.toString()
                tvWhtCount2.text = weightProTwo.toString()

                tvSubTotalProsOne.text = getString(R.string.subtotal,subTotalProsOne.toString())
                tvSubTotalProsTwo.text = getString(R.string.subtotal,subTotalProsTwo.toString())
                tvSubTotalConsOne.text = getString(R.string.subtotal,subTotalConsOne.toString())
                tvSubTotalConsTwo.text = getString(R.string.subtotal,subTotalConsTwo.toString())

                checkDigitNegative(tvWhtCount,weightProOne)
                checkDigitNegative(tvWhtCount2,weightProTwo)

                journalProsOneListAdapter.submitList(prosOneList)
                journalProsTwoListAdapter.submitList(prosTwoList)
                journalConsOneListAdapter.submitList(crossOneList)
                journalConsTwoListAdapter.submitList(crossTwoList)
            }
        }
    }

    private fun setOnClickListener() {
        binding.apply {
            btnCreatePassword.setOnClickListener {
                finishActivity()
            }

            ivEdit.setOnClickListener {
                val bundle = bundleOf(
                    AppConstants.SCREEN_TYPE to "EDIT",
                    AppConstants.JOURNAL_ID to viewModal.journalID.get().toString(),
                    AppConstants.ENTRY_TYPE to entryType
                )
                launchActivity<EditJournalActivity>(0, bundle) { }
            }

            ivBack.setOnClickListener {
                finishActivity()
            }

            ivDelete.setOnClickListener {
                RunInScope.ioThread {
                    viewModal.hitDeleteJournalApi()
                }
            }
        }
    }

    private fun viewHandling() {
        binding.apply {
            when (entryType) {
                "1" -> {
                    viewModal.entryTypeId.set("1")
                    tvNotesDec.visible()
                    doubleLayout.gone()
                    layoutFour.gone()
                }

                "2" -> {
                    viewModal.entryTypeId.set("2")
                    tvNotesDec.gone()
                    doubleLayout.visible()
                    layoutFour.gone()
                }

                "3" -> {
                    viewModal.entryTypeId.set("3")
                    tvNotesDec.gone()
                    doubleLayout.gone()
                    layoutFour.visible()
                }
            }
        }
    }

    private fun checkDigitNegative(id: TextView, number:Int){
        if (number.isPositive()) {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_color))
        } else if (number.isNegative()) {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.red))
        } else {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.light_mode_black))
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.journalDetailSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            setDataOnView(data.data)
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

        lifecycleScope.launch {
            viewModal.deleteJournalSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            callBackDeleteJournal.invoke(position.toInt(),true)
                            finishActivity()
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
                MyProgressBar.showProgress(activity)
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }

    /** set recycler view Pros one  List */
    private fun initJournalOneRecyclerView() {
        binding.rvProsOne.adapter = journalProsOneListAdapter
        journalProsOneListAdapter.submitList(arrayListOf())
    }


    private val journalProsOneListAdapter = object : GenericAdapter<ItemProsConsListNotEditBinding, EditJournalActivity.WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list_not_edit
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindHolder(holder: ItemProsConsListNotEditBinding, dataClass: EditJournalActivity.WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.text = dataClass.ItemDesc
            }
        }
    }


    /** set recycler view Pros one  List */
    private fun initJournalProsTwoRecyclerView() {
        binding.rvProsTwo.adapter = journalProsTwoListAdapter
        journalProsTwoListAdapter.submitList(arrayListOf())
    }


    private val journalProsTwoListAdapter = object : GenericAdapter<ItemProsConsListNotEditBinding, EditJournalActivity.WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list_not_edit
        }

        override fun onBindHolder(holder: ItemProsConsListNotEditBinding, dataClass: EditJournalActivity.WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.text = dataClass.ItemDesc
            }
        }
    }

    /** set recycler view Pros one  List */
    private fun initJournalConsOneRecyclerView() {
        binding.rvConsOne.adapter = journalConsOneListAdapter
        journalConsOneListAdapter.submitList(arrayListOf())
    }

    private val journalConsOneListAdapter = object : GenericAdapter<ItemProsConsListNotEditBinding, EditJournalActivity.WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list_not_edit
        }

        override fun onBindHolder(holder: ItemProsConsListNotEditBinding, dataClass: EditJournalActivity.WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.text = dataClass.ItemDesc
            }
        }
    }

    /** set recycler view Pros one  List */
    private fun initJournalConsTwoRecyclerView() {
        binding.rvConsTwo.adapter = journalConsTwoListAdapter
        journalConsTwoListAdapter.submitList(arrayListOf())
    }

    private val journalConsTwoListAdapter = object : GenericAdapter<ItemProsConsListNotEditBinding, EditJournalActivity.WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list_not_edit
        }

        override fun onBindHolder(holder: ItemProsConsListNotEditBinding, dataClass: EditJournalActivity.WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.text = dataClass.ItemDesc
            }
        }
    }

}
package com.lorick.chatterbox.view.activity.editJournal

import android.annotation.SuppressLint
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.journal.ViewJournalsDataResponse
import com.lorick.chatterbox.databinding.ActivityEditJournalBinding
import com.lorick.chatterbox.databinding.ItemProsConsListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.changeDate
import com.lorick.chatterbox.utils.constant.ApiConstants
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.getCurrentDateFormat
import com.lorick.chatterbox.utils.getFirstWordOrInt
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.isNegative
import com.lorick.chatterbox.utils.isPositive
import com.lorick.chatterbox.utils.openAlertSaveJournalData
import com.lorick.chatterbox.utils.setAfterTextChangeWatcher
import com.lorick.chatterbox.utils.showDatePicker
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class EditJournalActivity : BaseActivity<ActivityEditJournalBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@EditJournalActivity
    private var screenType = ""
    private var entryType = ""
    private var prosOneList : ArrayList<WeightMethodItems>?= arrayListOf()
    private var prosTwoList : ArrayList<WeightMethodItems>?= arrayListOf()
    private var crossOneList : ArrayList<WeightMethodItems>?= arrayListOf()
    private var crossTwoList : ArrayList<WeightMethodItems>?= arrayListOf()
    private var subTotalProsOne = 0
    private var subTotalProsTwo = 0
    private var subTotalConsOne = 0
    private var subTotalConsTwo = 0
    private var weightProOne = 0
    private var weightProTwo = 0
    val param = HashMap<String, Any?>()

    @Inject
    lateinit var validator: Validator

    companion object{
        lateinit var callBackAddJournal :(Boolean) ->Unit
        lateinit var callBackUpdateJournal :(Boolean) ->Unit
    }

    override fun getLayoutRes(): Int = R.layout.activity_edit_journal

    override fun initView() {
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
        observeDataFromViewModal()
        getIntentData()
        initJournalOneRecyclerView()
        initJournalProsTwoRecyclerView()
        initJournalConsOneRecyclerView()
        initJournalConsTwoRecyclerView()
        setOnClickListener()
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }
    private fun hitApi() {
        RunInScope.ioThread {
            viewModal.hitGetJournalDetailApi()
        }
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            screenType = intent.getString(AppConstants.SCREEN_TYPE).toString()
            entryType = intent.getString(AppConstants.ENTRY_TYPE).toString()
            viewModal.journalID.set(intent.getString(AppConstants.JOURNAL_ID).toString())
            Log.d("JSON_FORMAT_DATA",entryType)

            if (screenType == "EDIT") {
                hitApi()
            }else{
                val currentDate = getCurrentDateFormat()
                viewModal.date.set(currentDate)
            }
            viewHandling()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setOnClickListener() {
        binding.apply {
            btnSave.setOnClickListener {
                if (screenType == "ADD") {
                    if (handlingViewValidationNotes(entryType)){
                        getDataFromViews(entryType)
                        RunInScope.ioThread {
                            viewModal.hitAddJournalApi()
                        }
                    }
                } else {
                    if (handlingViewValidationNotes(entryType)){
                        getDataFromViews(entryType)
                        RunInScope.ioThread {
                            viewModal.hitUpdateJournalApi()
                        }
                    }
                }
            }

            etDates.setOnClickListener {
                showDatePickerCalender()
            }
            ivBack.setOnClickListener {
                showAlertDialogForConfirmation()
            }
            btnCancel.setOnClickListener {
                finishActivity()
            }

            etNoteFourOneDec.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (etNoteFourOneDec.text?.isNotBlank() == true){
                        prosOneList?.add(WeightMethodItems(etNoteFourOneDec.text.toString(),1))
                        journalProsOneListAdapter.submitList(prosOneList)
                        journalProsOneListAdapter.notifyItemInserted(prosOneList?.size ?: 0)
                        binding.rvProsOne.scrollToPosition(journalProsOneListAdapter.itemCount - 1)
                        etNoteFourOneDec.text?.clear()

                        subTotalProsOne = 0
                        weightProOne = 0
                        prosOneList?.forEach {
                            if (it.ItemDesc.trim().firstOrNull()?.toString()?.toIntOrNull() != null){
                                subTotalProsOne = subTotalProsOne.plus( getFirstWordOrInt(it.ItemDesc))
                                tvSubTotalProsOne.text = getString(R.string.subtotal,subTotalProsOne.toString())
                                weightProOne = subTotalProsOne.minus(subTotalConsOne)
                                checkDigitNegative(binding.tvWhtCount,weightProOne)
                                tvWhtCount.text = weightProOne.toString()
                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }
            etNoteFourTwoDec.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (etNoteFourTwoDec.text?.isNotBlank() == true){
                        prosTwoList?.add(WeightMethodItems(etNoteFourTwoDec.text.toString(),1))
                        journalProsTwoListAdapter.submitList(prosTwoList)
                        journalProsTwoListAdapter.notifyItemInserted(prosTwoList?.size ?: 0)
                        binding.rvProsTwo.scrollToPosition(journalProsTwoListAdapter.itemCount - 1)
                        etNoteFourTwoDec.text?.clear()

                        weightProTwo = 0
                        subTotalProsTwo = 0
                        prosTwoList?.forEach {
                            if (it.ItemDesc.trim().firstOrNull()?.toString()?.toIntOrNull() != null){
                                subTotalProsTwo = subTotalProsTwo.plus(getFirstWordOrInt(it.ItemDesc))
                                tvSubTotalProsTwo.text = getString(R.string.subtotal,subTotalProsTwo.toString())
                                weightProTwo = subTotalProsTwo.minus(subTotalConsTwo)
                                checkDigitNegative(binding.tvWhtCount2,weightProTwo)
                                tvWhtCount2.text = weightProTwo.toString()
                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }

            etNoteFourThreeDec.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (etNoteFourThreeDec.text?.isNotBlank() == true){
                        crossOneList?.add(WeightMethodItems(etNoteFourThreeDec.text.toString(),2))
                        journalConsOneListAdapter.submitList(crossOneList)
                        journalConsOneListAdapter.notifyItemInserted(crossOneList?.size ?: 0)
                        binding.rvConsOne.scrollToPosition(journalConsOneListAdapter.itemCount - 1)
                        etNoteFourThreeDec.text?.clear()

                        subTotalConsOne = 0
                        weightProOne = 0
                        crossOneList?.forEach {
                            if (it.ItemDesc.trim().firstOrNull()?.toString()?.toIntOrNull() != null){
                                subTotalConsOne = subTotalConsOne.plus(getFirstWordOrInt(it.ItemDesc))
                                tvSubTotalConsOne.text = getString(R.string.subtotal,subTotalConsOne.toString())
                                weightProOne = subTotalProsOne.minus(subTotalConsOne)
                                checkDigitNegative(binding.tvWhtCount,weightProOne)
                                tvWhtCount.text = weightProOne.toString()
                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }

            etNoteFourFourDec.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (etNoteFourFourDec.text?.isNotBlank() == true){
                        crossTwoList?.add(WeightMethodItems(etNoteFourFourDec.text.toString(),2))
                        journalConsTwoListAdapter.submitList(crossTwoList)
                        journalConsTwoListAdapter.notifyItemInserted(crossTwoList?.size ?: 0)
                        binding.rvConsTwo.scrollToPosition(journalConsTwoListAdapter.itemCount - 1)
                        etNoteFourFourDec.text?.clear()

                        weightProTwo = 0
                        subTotalConsTwo = 0
                        crossTwoList?.forEach {
                            if (it.ItemDesc.trim().firstOrNull()?.toString()?.toIntOrNull() != null){
                                subTotalConsTwo = subTotalConsTwo.plus(getFirstWordOrInt(it.ItemDesc))
                                tvSubTotalConsTwo.text = getString(R.string.subtotal,subTotalConsTwo.toString())
                                weightProTwo = subTotalProsTwo.minus(subTotalConsTwo)
                                checkDigitNegative(binding.tvWhtCount2,weightProTwo)
                                tvWhtCount2.text = weightProTwo.toString()
                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    fun showAlertDialogForConfirmation(){
        openAlertSaveJournalData {
            if (it){
                if (screenType == "ADD") {
                    if (handlingViewValidationNotes(entryType)){
                        getDataFromViews(entryType)
                        RunInScope.ioThread {
                            viewModal.hitAddJournalApi()
                        }
                    }
                } else {
                    if (handlingViewValidationNotes(entryType)){
                        getDataFromViews(entryType)
                        RunInScope.ioThread {
                            viewModal.hitUpdateJournalApi()
                        }
                    }
                }
            }else{
                finishActivity()
            }
        }
    }

    private fun showDatePickerCalender() {
        showDatePicker(activity){ selectedDate ->
            // selectedDate is in the format yyyy-MM-dd
            binding.etDates.text = selectedDate
        }
    }

    fun checkDigitNegative(id:TextView,number:Int){
        if (number.isPositive()) {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_color))
        } else if (number.isNegative()) {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.red))
        } else {
            id.setTextColor(ContextCompat.getColor(applicationContext,R.color.light_mode_black))
        }
    }


    /** Convert Json format Weight method for (type 3)*/
    private fun getDataFromViews(entryType: String) {
        // Create the outer JSON object
        val weightJournalNotesArrayAs = JSONArray()
        val jotMethodJournalNotesArray = JSONArray()
        val weightJournalNotesArrayBs = JSONArray()

        if (entryType == "2"){
            val jotPros1Object = JSONObject()
            val jotPros2Object = JSONObject()

            jotPros1Object.put("NotesTitle",  binding.etNoteOne.text.toString())
            jotPros1Object.put("NotesPros", binding.etNoteOneDec.text.toString())

            jotPros2Object.put("NotesTitle",  binding.etNoTwo.text.toString())
            jotPros2Object.put("NotesPros", binding.etNoteTwoDec.text.toString())

            jotMethodJournalNotesArray.put(jotPros1Object)
            jotMethodJournalNotesArray.put(jotPros2Object)
        }else if (entryType == "3"){

            val firstProsObject = JSONObject()
            firstProsObject.put("NotesTitle",  binding.etNoteFourOne.text.toString())
            firstProsObject.put("NotesPros", "hi")
            firstProsObject.put("TotalPoints",subTotalProsOne)
            firstProsObject.put("wt", binding.tvWhtCount.text.toString().toInt())

            val weightItemsAsArrayFirstPros = JSONArray()
            val jsonStringsList1 = prosOneList?.map { Gson().toJson(it) }

            jsonStringsList1?.forEach { jsonString ->
                try {
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject.has("ItemDesc") && jsonObject.has("TypeId")) {
                        weightItemsAsArrayFirstPros.put(jsonObject)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            firstProsObject.put("weightitemsAs", weightItemsAsArrayFirstPros)
            weightJournalNotesArrayAs.put(firstProsObject)

            val firstConsInnerObject = JSONObject()
            firstConsInnerObject.put("NotesTitle", "")
            firstConsInnerObject.put("NotesPros", "233")
            firstConsInnerObject.put("TotalPoints", subTotalConsOne)
            firstConsInnerObject.put("wt", binding.tvWhtCount.text.toString().toInt())

            val weightItemsAsArray2 = JSONArray()
            val jsonStringsList2 = crossOneList?.map { Gson().toJson(it) }
            jsonStringsList2?.forEach { jsonString ->
                try {
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject.has("ItemDesc") && jsonObject.has("TypeId")) {
                        weightItemsAsArray2.put(jsonObject)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            firstConsInnerObject.put("weightitemsAs", weightItemsAsArray2)
            weightJournalNotesArrayAs.put(firstConsInnerObject)



            // Create the outer JSON object
            val secondProsObject = JSONObject()
            secondProsObject.put("NotesTitle",  binding.etNoteFourTwo.text.toString())
            secondProsObject.put("NotesPros", "TEST")
            secondProsObject.put("TotalPoints", subTotalProsTwo)
            secondProsObject.put("wt", binding.tvWhtCount2.text.toString().toInt())

            val weightItemsAsArraySecondPros = JSONArray()
            val jsonStringsListPros2 = prosTwoList?.map { Gson().toJson(it) }

            jsonStringsListPros2?.forEach { jsonString ->
                try {
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject.has("ItemDesc") && jsonObject.has("TypeId")) {
                        weightItemsAsArraySecondPros.put(jsonObject)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            secondProsObject.put("weightitemsBs", weightItemsAsArraySecondPros)
            weightJournalNotesArrayBs.put(secondProsObject)

            val secondConsInnerObject = JSONObject()
            secondConsInnerObject.put("NotesTitle", "")
            secondConsInnerObject.put("NotesPros", "TEST")
            secondConsInnerObject.put("TotalPoints", subTotalConsTwo)
            secondConsInnerObject.put("wt", binding.tvWhtCount2.text.toString().toInt())

            val weightItemsAsArrayCons2 = JSONArray()
            val jsonStringsListCons2 = crossTwoList?.map { Gson().toJson(it) }
            jsonStringsListCons2?.forEach { jsonString ->
                try {
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject.has("ItemDesc") && jsonObject.has("TypeId")) {
                        weightItemsAsArrayCons2.put(jsonObject)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            secondConsInnerObject.put("weightitemsBs", weightItemsAsArrayCons2)
            weightJournalNotesArrayBs.put(secondConsInnerObject)

        }

        viewModal.jsonObjectAddJournalWeight.apply {
            put(ApiConstants.ApiParams.SUBJECT.value, viewModal.subject.get()?.trim() ?: "")
            put(ApiConstants.ApiParams.ENTRY_TYPE_ID.value, viewModal.entryTypeId.get()?.trim() ?: "")
            put(ApiConstants.ApiParams.NOTES.value, viewModal.notes.get()?.trim() ?: "")
            put(ApiConstants.ApiParams.JOURNAL_DATE.value, binding.etDates.text.toString())

            if (screenType == "EDIT") {
                put(ApiConstants.ApiParams.JOURNAL_ID.value, viewModal.journalID.get().toString())
            }

            if (entryType == "2"){
                put(ApiConstants.ApiParams.WEIGHT_JOURNAL_NOTES.value, jotMethodJournalNotesArray)
            }else if (entryType == "3"){
                put(ApiConstants.ApiParams.WEIGHT_JOURNAL_NOTES_AS.value, weightJournalNotesArrayAs)
                put(ApiConstants.ApiParams.WEIGHT_JOURNAL_NOTES_BS.value, weightJournalNotesArrayBs)
            }
        }

        Log.d("JSON_FORMAT_DATA",viewModal.jsonObjectAddJournalWeight.toString())
    }


    private fun setData(journalListResponse: ViewJournalsDataResponse) {
        binding.apply {
            viewModal.subject.set(journalListResponse.subject)
            viewModal.notes.set(journalListResponse.notes)
            viewModal.journalID.set(journalListResponse.journalId.toString())
            viewModal.date.set(changeDate(journalListResponse.journalDate))
            btnSave.text = getString(R.string.update)

            if (entryType == "2"){
                if (journalListResponse.weightJournalNotes?.isNotEmpty() == true){
                    etNoteOne.setText(journalListResponse.weightJournalNotes[0].notesTitle)
                    etNoteOneDec.setText(journalListResponse.weightJournalNotes[0].notesPros)
                    etNoTwo.setText(journalListResponse.weightJournalNotes[1].notesTitle)
                    etNoteTwoDec.setText(journalListResponse.weightJournalNotes[1].notesPros)
                }
            }else if (entryType == "3"){
                if (journalListResponse.weightJournalNotesAs?.isNotEmpty() == true){
                    val prosOne = journalListResponse.weightJournalNotesAs[0].weightItemsAs ?: arrayListOf()
                    val consOne = journalListResponse.weightJournalNotesAs[1].weightItemsAs ?: arrayListOf()

                    for (i in prosOne.indices){
                        prosOneList?.add(WeightMethodItems(prosOne[i].itemDesc ?: "",prosOne[i].typeId ?: 0))
                    }
                    for (i in consOne.indices){
                        crossOneList?.add(WeightMethodItems(consOne[i].itemDesc ?: "",consOne[i].typeId ?: 0))
                    }

                    subTotalProsOne = journalListResponse.weightJournalNotesAs[0].totalPoints ?: 0
                    subTotalConsOne = journalListResponse.weightJournalNotesAs[1].totalPoints ?: 0

                    etNoteFourOne.setText(journalListResponse.weightJournalNotesAs[0].notesTitle ?: "0")

                }

                if (journalListResponse.weightJournalNotesBs?.isNotEmpty() == true){
                    val prosTwo = journalListResponse.weightJournalNotesBs[0].weightItemsBs ?: arrayListOf()
                    val consTwo = journalListResponse.weightJournalNotesBs[1].weightItemsBs ?: arrayListOf()

                    for (i in prosTwo.indices){
                        prosTwoList?.add(WeightMethodItems(prosTwo[i].itemDesc ?: "",prosTwo[i].typeId ?: 0))
                    }
                    for (i in consTwo.indices){
                        crossTwoList?.add(WeightMethodItems(consTwo[i].itemDesc ?: "",consTwo[i].typeId ?: 0))
                    }

                    subTotalProsTwo = journalListResponse.weightJournalNotesBs[0].totalPoints ?: 0
                    subTotalConsTwo = journalListResponse.weightJournalNotesBs[1].totalPoints ?: 0

                    etNoteFourTwo.setText(journalListResponse.weightJournalNotesBs[0].notesTitle ?: "0")

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

    private fun viewHandling() {
        binding.apply {
            when (entryType) {
                "1" -> {
                    viewModal.entryTypeId.set("1")
                    singleLayout.visible()
                    doubleLayout.gone()
                    layoutFour.gone()
                    view90.gone()
                }

                "2" -> {
                    viewModal.entryTypeId.set("2")
                    singleLayout.gone()
                    doubleLayout.visible()
                    layoutFour.gone()
                    view90.gone()
                }

                "3" -> {
                    viewModal.entryTypeId.set("3")
                    singleLayout.gone()
                    doubleLayout.gone()
                    layoutFour.visible()
                    view90.visible()
                }
            }
        }
    }

    /** Observer Response via View model for*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.journalDetailSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            setData(data.data)
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
            viewModal.addJournalSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            callBackAddJournal.invoke(true)
                            finishActivity()
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

        lifecycleScope.launch {
            viewModal.updateJournalSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            callBackAddJournal.invoke(true)
                            callBackUpdateJournal.invoke(true)
                            finishActivity()
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
                MyProgressBar.showProgress(activity)
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }
    private fun handlingViewValidationNotes(entryType: String):Boolean{
        when (entryType) {
            "1" -> {
                binding.apply {
                    if (etSubject.text?.isBlank() == true){
                        showErrorSnack(activity,"Please Enter your Subject")
                        return false
                    }else if (etNotes.text?.isBlank() == true){
                        showErrorSnack(activity,"Please Enter your Notes")
                        return false
                    }
                }
            }
            "2" -> {
                binding.apply {
                    if (etSubject.text?.isBlank() == true){
                        showErrorSnack(activity,"Please Enter your Subject")
                        return false
                    }else if (etNoteOne.text?.isNotBlank() == true){
                        if (etNoteOneDec.text?.isBlank() == true){
                            showErrorSnack(activity,"Blank Description of ${etNoteOne.text.toString()}")
                            return false
                        }
                    }else if (etNoTwo.text?.isNotBlank() == true){
                        if (etNoteTwoDec.text?.isBlank() == true){
                            showErrorSnack(activity,"Blank Description of ${etNoTwo.text.toString()}")
                            return false
                        }
                    }
                }
            }
            "3" -> {
                binding.apply {
                    if (etSubject.text?.isBlank() == true){
                        showErrorSnack(activity,"Please Enter your Subject")
                        return false
                    }
                }
            }
        }
        return true
    }


    /** set recycler view Pros one  List */
    private fun initJournalOneRecyclerView() {
        binding.rvProsOne.adapter = journalProsOneListAdapter
        journalProsOneListAdapter.submitList(arrayListOf())
    }


    private val journalProsOneListAdapter = object : GenericAdapter<ItemProsConsListBinding, WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindHolder(holder: ItemProsConsListBinding, dataClass: WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.setText(dataClass.ItemDesc)

                etNoteFourOneDec.setAfterTextChangeWatcher{
                    dataClass.ItemDesc = etNoteFourOneDec.text.toString()
                    subTotalProsOne = 0
                    currentList.forEach {
                        subTotalProsOne = subTotalProsOne.plus( getFirstWordOrInt(it.ItemDesc))
                        binding.tvSubTotalProsOne.text = getString(R.string.subtotal,subTotalProsOne.toString())
                        weightProOne = subTotalProsOne.minus(subTotalConsOne)
                        checkDigitNegative(binding.tvWhtCount,weightProOne)
                        binding.tvWhtCount.text = weightProOne.toString()
                    }
                }
            }
        }
    }


    /** set recycler view Pros one  List */
    private fun initJournalProsTwoRecyclerView() {
        binding.rvProsTwo.adapter = journalProsTwoListAdapter
        journalProsTwoListAdapter.submitList(arrayListOf())
    }


    private val journalProsTwoListAdapter = object : GenericAdapter<ItemProsConsListBinding, WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list
        }

        override fun onBindHolder(holder: ItemProsConsListBinding, dataClass: WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.setText(dataClass.ItemDesc)

                etNoteFourOneDec.setAfterTextChangeWatcher{
                    dataClass.ItemDesc = etNoteFourOneDec.text.toString()
                    subTotalProsTwo = 0
                    currentList.forEach {
                        subTotalProsTwo = subTotalProsTwo.plus(getFirstWordOrInt(it.ItemDesc))
                        binding.tvSubTotalProsTwo.text = getString(R.string.subtotal,subTotalProsTwo.toString())
                        weightProTwo = subTotalProsTwo.minus(subTotalConsTwo)
                        checkDigitNegative(binding.tvWhtCount2,weightProTwo)
                        binding.tvWhtCount2.text = weightProTwo.toString()
                    }
                }
            }
        }
    }

    /** set recycler view Pros one  List */
    private fun initJournalConsOneRecyclerView() {
        binding.rvConsOne.adapter = journalConsOneListAdapter
        journalConsOneListAdapter.submitList(arrayListOf())
    }

    private val journalConsOneListAdapter = object : GenericAdapter<ItemProsConsListBinding, WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list
        }

        override fun onBindHolder(holder: ItemProsConsListBinding, dataClass: WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.setText(dataClass.ItemDesc)

                etNoteFourOneDec.setAfterTextChangeWatcher{
                    dataClass.ItemDesc = etNoteFourOneDec.text.toString()
                    subTotalConsOne = 0
                    currentList.forEach {
                        subTotalConsOne = subTotalConsOne.plus(getFirstWordOrInt(it.ItemDesc))
                        binding.tvSubTotalConsOne.text = getString(R.string.subtotal,subTotalConsOne.toString())
                        weightProOne = subTotalProsOne.minus(subTotalConsOne)
                        checkDigitNegative(binding.tvWhtCount,weightProOne)
                        binding.tvWhtCount.text = weightProOne.toString()
                    }
                }
            }
        }
    }

    /** set recycler view Pros one  List */
    private fun initJournalConsTwoRecyclerView() {
        binding.rvConsTwo.adapter = journalConsTwoListAdapter
        journalConsTwoListAdapter.submitList(arrayListOf())
    }

    private val journalConsTwoListAdapter = object : GenericAdapter<ItemProsConsListBinding, WeightMethodItems>() {
        override fun getResourceLayoutId(): Int {
            return R.layout.item_pros_cons_list
        }

        override fun onBindHolder(holder: ItemProsConsListBinding, dataClass: WeightMethodItems, position: Int) {
            holder.apply {
                etNoteFourOneDec.setText(dataClass.ItemDesc)

                etNoteFourOneDec.setAfterTextChangeWatcher{
                    dataClass.ItemDesc = etNoteFourOneDec.text.toString()
                    subTotalConsTwo = 0
                    currentList.forEach {
                        subTotalConsTwo = subTotalConsTwo.plus(getFirstWordOrInt(it.ItemDesc))
                        binding.tvSubTotalConsTwo.text = getString(R.string.subtotal,subTotalConsTwo.toString())
                        weightProTwo = subTotalProsTwo.minus(subTotalConsTwo)
                        checkDigitNegative(binding.tvWhtCount2,weightProTwo)
                        binding.tvWhtCount2.text = weightProTwo.toString()
                    }
                }
            }
        }
    }

    data class WeightMethodItems(
        var ItemDesc:String,
        var TypeId:Int
    )

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showAlertDialogForConfirmation()
        }
    }

}
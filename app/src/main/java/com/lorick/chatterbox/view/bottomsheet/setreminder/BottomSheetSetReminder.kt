package com.lorick.chatterbox.view.bottomsheet.setreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lorick.chatterbox.R
import com.lorick.chatterbox.data.response.notification.ReminderData
import com.lorick.chatterbox.databinding.BottomsheetSetReminderBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.customDateFormat
import com.lorick.chatterbox.utils.showDatePickerDate
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.showTimePicker
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class BottomSheetSetReminder(var notificationReminderData: ReminderData?) : BottomSheetDialogFragment(), View.OnClickListener {
    private var binding: BottomsheetSetReminderBinding? = null
    private val viewModal: HomeViewModel by viewModels()
    lateinit var callbackStatus :(Boolean,Int) -> Unit
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_set_reminder, container, false) as BottomsheetSetReminderBinding
        setPeekHeight()

        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListener()
        setData()
        observeDataFromViewModal()
    }

    private fun setData() {
        notificationReminderData?.apply {
            binding?.apply {
                if (reminderTypeId == 0){
                    radioPickDate.isChecked = true
                }else if (reminderTypeId == 1){
                    radioFreeForm.isChecked = true
                }else if (reminderTypeId == 2){
                    radioToday.isChecked = true
                }else if (reminderTypeId == 3){
                    radioTomorrow.isChecked = true
                }
            }
        }
    }

    private fun setPeekHeight() {
        dialog?.setOnShowListener {
            val dialogParent = binding?.layoutCoordinate?.parent as View
            BottomSheetBehavior.from(dialogParent).peekHeight =
                (binding?.layoutCoordinate?.height!! * 0.99).toInt()
            dialogParent.requestLayout()
        }
    }


    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitNotificationReminderApi()
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetTheme
    }


    fun onClickListener() {
        binding?.apply {
            /**
             *  AnHourEarly = 1,
             *  Today = 2,
             *  Tomorrow = 3,
             *  CustomTime=0
             *  */

            radioFreeForm.setOnClickListener {
                radioFreeForm.isChecked = true
                viewModal.notificationReminderDate.set("")
                viewModal.notificationTypeID.set(1)
                apiHit()
            }
            radioToday.setOnClickListener {
                radioToday.isChecked = true
                viewModal.notificationReminderDate.set("")
                viewModal.notificationTypeID.set(2)
                apiHit()
            }
            radioTomorrow.setOnClickListener {
                radioTomorrow.isChecked = true
                viewModal.notificationReminderDate.set("")
                viewModal.notificationTypeID.set(3)
                apiHit()
            }
            radioPickDate.setOnClickListener {
                showDatePickerDate(requireContext(),{year,month,date->
                    requireActivity().showTimePicker(
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),false) { hr, mint ->
                        val dateTime = "$year $hr:$mint"
                        val getTime = customDateFormat(dateTime,"yyyy-MM-dd HH:mm","yyyy-MM-dd'T'HH:mm:ss.SSS")
                        viewModal.notificationReminderDate.set(getTime)
                        viewModal.notificationTypeID.set(0)
                        apiHit()
                    }
                },{
                    dismiss()
                })
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
//            binding?.ivCancel -> {
//                dialog?.dismiss()
//            }

        }
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.notificationReminderResponseSharedPrefs.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            dismiss()
                            callbackStatus(true,viewModal.notificationTypeID.get() ?: 0)
                        } else {
                            showErrorSnack(requireActivity(), data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg ->
                            showErrorSnack(requireActivity(), msg)
                        }
                    }
                }
            }
        }

        viewModal.showLoading.observe(requireActivity()) {
            if (it) {
                MyProgressBar.showProgress(requireContext())
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }
}
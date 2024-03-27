package com.lorick.chatterbox.view.bottomsheet.passwordchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lorick.chatterbox.R
import com.lorick.chatterbox.databinding.BottomsheetPasswordchangesucessfullyBinding
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.view.activity.login.LoginActivity

class BottomSheetPasswordChange : BottomSheetDialogFragment(), View.OnClickListener {
    private var binding: BottomsheetPasswordchangesucessfullyBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottomsheet_passwordchangesucessfully,
            container,
            false
        ) as BottomsheetPasswordchangesucessfullyBinding
        setPeekHeight()
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListener()
    }

    private fun setPeekHeight() {
        dialog?.setOnShowListener {
            val dialogParent = binding?.layoutCoordinate?.parent as View
            BottomSheetBehavior.from(dialogParent).peekHeight =
                (binding?.layoutCoordinate?.height!! * 0.99).toInt()
            dialogParent.requestLayout()
        }
    }


    override fun getTheme(): Int {
        return R.style.CustomBottomSheetTheme
    }


    private fun onClickListener() {
        binding?.btnBacktoLogin?.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {

            binding?.btnBacktoLogin -> {
                requireActivity().launchActivity<LoginActivity> { }

            }
        }
    }
}

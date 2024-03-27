package com.lorick.chatterbox.view.activity.onboarding

import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayoutMediator
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityOnBoardingBinding
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.overrideImageStatusBar
import com.lorick.chatterbox.view.activity.login.LoginActivity
import com.lorick.chatterbox.view.activity.signup.SignupActivity
import com.lorick.chatterbox.view.adapter.ViewPagerAdapter

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>(), ViewPager.OnPageChangeListener {
    val adapter by lazy {
        ViewPagerAdapter()
    }

    override fun getLayoutRes(): Int = R.layout.activity_on_boarding

    override fun initView() {
        overrideImageStatusBar(this)
        setAdapter()
        onClickListener()
    }

    override fun viewModel() {

    }

    private fun setAdapter() {
        binding.apply {
            viewpager.adapter = adapter
            TabLayoutMediator(tabLayout, viewpager) { k, j ->
                k.text = ""
            }.attach()
            arrayOfNulls<String>(3).also {
                adapter.submitList(it.toMutableList())
            }
        }
    }

    private fun onClickListener() {
        binding.apply {
            btnLogin.setOnClickListener {
                launchActivity<LoginActivity> { }
            }

            btnGetStarted.setOnClickListener {
                launchActivity<SignupActivity> { }
            }

        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }


}
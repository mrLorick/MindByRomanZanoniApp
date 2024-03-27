package com.lorick.chatterbox.view.activity.nowPlaying

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.meditation.MeditationTypeListResponse
import com.lorick.chatterbox.data.response.resource.ResourceTypeList
import com.lorick.chatterbox.databinding.ActivityNowPlayingBinding
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NowPlayingActivity : BaseActivity<ActivityNowPlayingBinding>() {

    override fun getLayoutRes(): Int = R.layout.activity_now_playing
    private val viewModal: HomeViewModel by viewModels()

    override fun initView() {
        getIntentData()
        setToolbar()
        observeDataFromViewModal()
        setOnClickListener()
        setSpinnerSpeed()
    }

    private fun setSpinnerSpeed() {
        val adapter = ArrayAdapter.createFromResource(this, R.array.speed_video, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        binding.tvSpeed.adapter = adapter
        binding.tvSpeed.setSelection(3)
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            val screenType = intent.getString(AppConstants.SCREEN_TYPE).toString()
            if (screenType == AppConstants.MEDITATION_SCREEN){
                val getMeditationData = intent.getString(AppConstants.MEDIATION_DETAILS).toString()
                viewModal.meditationDetails = Gson().fromJson(getMeditationData, MeditationTypeListResponse::class.java)
                viewModal.initializingPlayers(context = this, viewModal.meditationDetails?.title ?: "", viewModal.meditationDetails?.videoName ?: "", viewModal.meditationDetails?.videoThumbImage ?: "")
            }else if(screenType == AppConstants.RESOURCE_SCREEN){
                val getResourceData = intent.getString(AppConstants.RESOURCE_DETAILS).toString()
                viewModal.resourceDetails = Gson().fromJson(getResourceData, ResourceTypeList::class.java)
                viewModal.initializingPlayers(context = this, viewModal.resourceDetails?.title ?: "", viewModal.resourceDetails?.audioName ?: "", viewModal.resourceDetails?.imageName ?: "")
            }
            setUiData(screenType)
        }
    }

    private fun setUiData(screenType: String) {
        Log.d("ekdnfken", viewModal.meditationDetails.toString())
        binding.apply {
            if (screenType == AppConstants.MEDITATION_SCREEN){
                viewModal.meditationDetails?.apply {
                    appCompatTextView4.text = title
                    tvEndTime.text = duration
                    tvDec.text = content
                    cvImg.setImageFromUrl(R.drawable.placeholder_mind, videoThumbImage)
                }
            }else if(screenType == AppConstants.RESOURCE_SCREEN){
                viewModal.resourceDetails?.apply {
                    appCompatTextView4.text = title
                    tvEndTime.text = duration
                    tvDec.text = content
                    cvImg.setImageFromUrl(R.drawable.placeholder_mind, imageName)
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.apply {
            appCompatImageView9.setOnClickListener {
                /** Play button working in customization */
                viewModal.mediaController?.playOrPauseMusic()
            }
            ivDecrease10sec.setOnClickListener {
                /** Play button working in customization */
                val progress = binding.rangeSlider.progress
                viewModel?.mediaController?.changeMusicProgress(
                    progress = progress.toLong() - 10000L
                )
            }
            ivIncrease10sec.setOnClickListener {
                /** Play button working in customization */
                val progress = binding.rangeSlider.progress
                viewModel?.mediaController?.changeMusicProgress(
                    progress = progress.toLong() + 10000L
                )
            }
            tvSpeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {

                    /** Speed fast */
                    when(position){
                        0->{  viewModal.mediaController?.fastSpeed(0.25F)}
                        1->{  viewModal.mediaController?.fastSpeed(0.5F)}
                        2->{  viewModal.mediaController?.fastSpeed(0.75F)}
                        3->{ viewModal.mediaController?.fastSpeed(1f)}
                        4->{  viewModal.mediaController?.fastSpeed(1.25F)}
                        5->{  viewModal.mediaController?.fastSpeed(1.5F)}
                        6->{  viewModal.mediaController?.fastSpeed(2f)}
                    }

                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.tvSpeed.setSelection(3)
                }
            }
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }


    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.now_playing)
            ivBack.setOnClickListener{
                finishActivity()
            }
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            RunInScope.mainThread {
                viewModal.mediaController?.musicPLayingState()?.collectLatest { isPlaying ->
                    binding.appCompatImageView9.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            if (isPlaying) R.drawable.ic_pause_icon else R.drawable.ic_play_icon
                        )
                    )
                }
            }
        }
        /** Buffering */
        lifecycleScope.launch {
            RunInScope.mainThread {
                viewModal.mediaController?.musicBuffering()?.collectLatest { isBuffering ->
                    binding.apply {
                        if (isBuffering){
                            buffering.visible()
                        }else{
                            buffering.gone()
                        }
                    }
                }
            }
        }

        /** Seek bar progress */
        lifecycleScope.launch(Dispatchers.Main) {
            viewModal.mediaController?.musicProgress()?.collectLatest { mediaProgress ->

                viewModal.musicStart.set(mediaProgress.musicProgress)
                binding.rangeSlider.progress = mediaProgress.seekBarProgress.toInt()
            }
        }

        /** Music end time */
        lifecycleScope.launch(Dispatchers.Main) {
            viewModal.mediaController?.musicEndTime()?.collectLatest { mediaProgress ->
                viewModal.musicEnd.set(mediaProgress.musicProgress)
                binding.rangeSlider.max = mediaProgress.seekBarProgress.toInt()
            }
        }
    }

    override fun finishAffinity() {
        super.finishAffinity()
        viewModal.mediaController?.releaseMediaController()
    }

    override fun finish() {
        super.finish()
        viewModal.mediaController?.releaseMediaController()
    }
}

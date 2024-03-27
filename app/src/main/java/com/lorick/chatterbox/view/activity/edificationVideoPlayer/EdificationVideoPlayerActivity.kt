package com.lorick.chatterbox.view.activity.edificationVideoPlayer

import android.annotation.SuppressLint
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.edification.EdificationTypeListResponse
import com.lorick.chatterbox.databinding.ActivityEdificationVideoPlayerBinding
import com.lorick.chatterbox.databinding.RowitemEdificationlistBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivityVideoZoom
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.view.activity.landscapeVideoPlay.LandscapeVideoPlayActivity
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EdificationVideoPlayerActivity : BaseActivity<ActivityEdificationVideoPlayerBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@EdificationVideoPlayerActivity
    private var getJsonData = ""
    private var edificationListItemResponse: EdificationTypeListResponse? = null
    private var filteredArrayList : ArrayList<EdificationTypeListResponse>? = null
    private var edificationTypeList: ArrayList<EdificationTypeListResponse>? = null
    private var player: ExoPlayer? = null

    override fun getLayoutRes(): Int = R.layout.activity_edification_video_player

    override fun initView() {
        getIntentData()
        setToolbar()
        setOnClickListener()
        observeDataFromViewModal()
//        apiHit()
    }

    private fun setOnClickListener() {
        binding.apply {
            videoView.setOnClickListener {
                if (player?.isPlaying == true) {
                    handlingPlayIcon(false)
                } else if (player?.isPlaying == false) {
                    handlingPlayIcon(true)
                }
            }
            ivPlayOrPause.setOnClickListener {
                if (player?.isPlaying == true) {
                    handlingPlayIcon(false)
                } else if (player?.isPlaying == false) {
                    handlingPlayIcon(true)
                }
            }
            icZoom.setOnClickListener {
                val  bundle = bundleOf(AppConstants.VIDEO_URL to edificationListItemResponse?.videoName.toString())
                launchActivityVideoZoom<LandscapeVideoPlayActivity>(0,bundle) {  }
            }
        }
    }

    override fun viewModel() {

    }

    /** Meditation List api Call*/
    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitEdificationListApi()
        }
    }


    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.now_playing)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }
    private fun getArrayListFromString(list:String): ArrayList<EdificationTypeListResponse>? {
        val gson = Gson()
        return if (list.isNotBlank()) {
            val type = object : TypeToken<List<EdificationTypeListResponse>>() {}.type
            return gson.fromJson(list, type)
        } else {
            null
        }
    }


    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null){
            getJsonData = intent.getString(AppConstants.EDIFICATION_DATA).toString()
            edificationListItemResponse = Gson().fromJson(getJsonData, EdificationTypeListResponse::class.java)
            val list  = intent.getString(AppConstants.EDIFICATION_TYPE_LIST)
            edificationTypeList = getArrayListFromString(list.toString())
            viewModal.categoryId.set(intent.getString(AppConstants.EDIFICATION_CAT_ID).toString())
            setData(edificationListItemResponse)
            setList()
        }
    }

    private fun setList() {
        // Filter the list based on the categoryId
        val filteredList = edificationTypeList?.filter { it.categoryId != edificationListItemResponse?.categoryId }
        // Convert the filtered list back to an ArrayList
        filteredArrayList = filteredList?.let { ArrayList(it) }
        filteredArrayList?.let { initMeditationRecyclerView(it) }
    }

    private fun setData(edificationListItemResponse: EdificationTypeListResponse?) {
        binding.apply {
            tvSongName.text = edificationListItemResponse?.title
            tvSongNameDec.text = edificationListItemResponse?.content
        }
        initPlayer(edificationListItemResponse?.videoName.toString())
    }



    /** set recycler view Meditation  List */
    private fun initMeditationRecyclerView(data: ArrayList<EdificationTypeListResponse>) {
        binding.rvCategoryEdification.adapter = categoryEdificationListAdapter
        categoryEdificationListAdapter.submitList(data)
    }

    private val categoryEdificationListAdapter =
        object : GenericAdapter<RowitemEdificationlistBinding, EdificationTypeListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.rowitem_edificationlist
            }
            override fun onBindHolder(holder: RowitemEdificationlistBinding, dataClass: EdificationTypeListResponse, position: Int) {
                holder.apply {
                    tvName.text = dataClass.title
                    tvDec.text = dataClass.content
                    tvDuration.text = dataClass.duration
                    ivImage.setImageFromUrl(dataClass.videoThumbImage,progressBar)

                    root.setOnClickListener{
                        filterList(dataClass)
                    }
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(dataClass: EdificationTypeListResponse) {
        filteredArrayList?.clear()
        val newList = edificationTypeList?.filter { it.categoryId != dataClass.categoryId }
        newList?.forEach {
            filteredArrayList?.add(it)
        }
        releasePlayer()
        setData(dataClass)
        categoryEdificationListAdapter.notifyDataSetChanged()
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.edificationListSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            initMeditationRecyclerView(data.data)
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
                binding.rvCategoryEdification.gone()
                binding.shimmerCommentList.apply {
                    visible()
                    startShimmer()
                }
            } else {
                binding.shimmerCommentList.apply {
                    RunInScope.mainThread {
                        stopShimmer()
                        gone()
                        binding.rvCategoryEdification.visible()
                    }
                }
            }
        }
    }


    private fun initPlayer(mediaPath: String) {
        player = ExoPlayer.Builder(applicationContext)
            .build()
            .apply { addListener(playerListener) }
        setUri(mediaPath)
        binding.videoView.player = player
    }

    private fun releasePlayer() {
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    private fun pause() {
        player?.playWhenReady = false
    }

    private fun play() {
        player?.playWhenReady = true
    }

    private fun restartPlayer() {
        player?.seekTo(0)
        player?.playWhenReady = true
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    handlingPlayIcon(false)
                }
                Player.STATE_READY -> {
                    handlingPlayIcon(true)
                }
                Player.STATE_BUFFERING -> {
                    handlingPlayIcon(playStatus = false, isBuffering = true)
                }
                Player.STATE_IDLE -> {

                }
            }
        }
    }

    private fun handlingPlayIcon(playStatus: Boolean, isBuffering: Boolean = false) {
        binding.apply {
            if (isBuffering) {
                ivPlayOrPause.setBackgroundResource(R.drawable.ic_simple_icon)
                ivPlayOrPause.visible()
                videoProgress.visible()
            } else {
                if (playStatus) {
                    play()
                    ivPlayOrPause.setBackgroundResource(R.drawable.ic_pause_icon)
                    ivPlayOrPause.visible()
                    videoProgress.gone()
                    RunInScope.mainThread {
                        delay(500)
                        ivPlayOrPause.gone()
                    }
                } else {
                    pause()
                    ivPlayOrPause.setBackgroundResource(R.drawable.ic_play_icon)
                    ivPlayOrPause.visible()
                    videoProgress.gone()
                }
            }
        }
    }

    private fun setUri(mediaPath: String) {
        val metaData = MediaMetadata.Builder()
            .setTitle("Title")
            .setAlbumTitle("Album")
            .build()

        val mediaMetaData = MediaItem.Builder()
            .setUri(mediaPath.toUri())
            .setMediaMetadata(metaData)
            .build()
        player?.repeatMode = Player.REPEAT_MODE_ALL
        player?.setMediaItem(mediaMetaData)
        player?.prepare()
    }


    override fun onStop() {
        super.onStop()
        player?.pause()
    }

}
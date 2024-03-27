package com.lorick.chatterbox.view.activity.eventDetails

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.text.Html
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.data.response.eventDetails.CommentListResponse
import com.lorick.chatterbox.data.response.eventDetails.Details
import com.lorick.chatterbox.data.response.eventDetails.WhoLikesFavouriteList
import com.lorick.chatterbox.databinding.ActivityEventDetailBinding
import com.lorick.chatterbox.databinding.ItemCommentsBinding
import com.lorick.chatterbox.databinding.ItemLatestLikeListBinding
import com.lorick.chatterbox.genrics.GenericAdapter
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.PdfViewAndDownload
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.convertDateFormat
import com.lorick.chatterbox.utils.convertTimeFormat
import com.lorick.chatterbox.utils.finishActivity
import com.lorick.chatterbox.utils.getTimeInAgo
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.launchActivityVideoZoom
import com.lorick.chatterbox.utils.setImageFromUrl
import com.lorick.chatterbox.utils.shimmerAnimationEffect
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.utils.startZoomMeeting
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.utils.writeExternalStoragePermission
import com.lorick.chatterbox.videoOrAudioControls.MediaCache
import com.lorick.chatterbox.view.activity.landscapeVideoPlay.LandscapeVideoPlayActivity
import com.lorick.chatterbox.view.activity.openPdfViewer.OpenPdfActivity
import com.lorick.chatterbox.view.bottomsheet.comments.BottomSheetComments
import com.lorick.chatterbox.view.bottomsheet.likes.BottomSheetLikes
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class EventDetailActivity : BaseActivity<ActivityEventDetailBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    var activity = this@EventDetailActivity
    private var position : Int = -1
    private var eventDetails  : Details? = null
    private var player: ExoPlayer? = null
    private lateinit var htmlString : String
    private var pdfViewOrDownload : PdfViewAndDownload? = null


    /** isLiked
     * 0 -- no change
     * 1 -- UnLike
     * 2 -- Like
     * */
    companion object {
        lateinit var listCallBackSheet: (position: Int, Boolean, commentCount: Int, likeCount: Int, isLikedStatus: Int) -> Unit
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_event_detail
    }

    override fun initView() {
        setToolbar()
        getIntentData()
        initialisePdfViewClass()
        clickListeners()
        observeDataFromViewModal()
        apiHit()
    }


    private fun callBackPdfViewDownload() {
        writeExternalStoragePermission(applicationContext,{
            pdfViewOrDownload?.startDownload(applicationContext, eventDetails?.docFileName.toString(),eventDetails?.title.toString())
        },{ })
    }

    private fun initialisePdfViewClass() {
        pdfViewOrDownload = PdfViewAndDownload()
    }

    private fun initCommentRecyclerView(commentList: ArrayList<CommentListResponse>) {
        binding.rvComments.adapter = commentListAdapter
        commentListAdapter.submitList(commentList)
    }

    private val commentListAdapter = object : GenericAdapter<ItemCommentsBinding, CommentListResponse>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.item_comments
            }

            override fun onBindHolder(holder: ItemCommentsBinding, dataClass: CommentListResponse, position: Int) {
                holder.apply {
                    tvUserName.text = dataClass.commentedByName
                    tvComment.text = dataClass.commentDesc
                    circleImageView.setImageFromUrl(R.drawable.no_image_placeholder, dataClass.commentedByImage)
                    tvOneDayAgo.text = getTimeInAgo(dataClass.commentedOn)


                    // Check if it's the last item in the list
                    if (position == itemCount - 1) {
                        view.gone()
                    } else {
                        // Show the view for other items
                        view.visible()
                    }
                }

            }
        }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun clickListeners() {
        binding.apply {
            tvLikedBy.setOnClickListener {
                val bottomSheetLikes = BottomSheetLikes(viewModal.eventId.get().toString())
                bottomSheetLikes.show(supportFragmentManager, "")
            }

            ivShare.setOnClickListener {
                shareEvent(viewModal.eventId.get().toString().toInt())
            }

            tvViewAllComments.setOnClickListener {
                val bottomSheetComments = BottomSheetComments(viewModal.eventId.get().toString())
                bottomSheetComments.show(supportFragmentManager, "")
                bottomSheetComments.dismissCallBackSheet = { status, count ->
                    if (status) {
                        eventDetails?.totalComments = count
                        tvViewAllComments.text = getString(R.string.view_all).plus(count.toString().plus(" ").plus("comments"))
                        listCallBackSheet.invoke(position,true,count,eventDetails?.totalFavourites ?: 0,0)
                    }
                }
            }

            ivFavourite.setOnClickListener {
                if (eventDetails?.isFavoritedbyUser == true) {
                    viewModal.isFavourite.set(false)
                } else {
                    viewModal.isFavourite.set(true)
                }
                RunInScope.ioThread {
                    viewModal.hitUpdateFavouriteEventStatusApi()
                }
            }

            layout1.setOnClickListener {
                startZoomMeeting(this@EventDetailActivity, "2324")
            }
            ivPlayOrPause.setOnClickListener {
                if (player?.isLoading == false) {
                    if (player?.isPlaying == true) {
                        handlingPlayIcon(false)
                    } else if (player?.isPlaying == false) {
                        handlingPlayIcon(true)
                    }
                }
            }
            videoView.setOnClickListener {
                if (player?.isLoading == false) {
                    if (player?.isPlaying == true) {
                        handlingPlayIcon(false)
                    } else if (player?.isPlaying == false) {
                        handlingPlayIcon(true)
                    }
                }
            }
            icZoom.setOnClickListener {
                val  bundle = bundleOf(AppConstants.VIDEO_URL to eventDetails?.mediaPath)
                launchActivityVideoZoom<LandscapeVideoPlayActivity>(0,bundle) {  }
            }
            icDownloadPdf.setOnClickListener {
                callBackPdfViewDownload()
            }
            relativeLayout.setOnClickListener {
                val bundle = bundleOf(AppConstants.PDF_URL to eventDetails?.docFileName)
                launchActivity<OpenPdfActivity>(0,bundle) {  }
            }
        }
    }


    /** Share  event details  other apps */
    private fun shareEvent(eventId: Int) {
        val deepLinkUrl = Uri.parse("https://mind.harishparas.com/share.html?EventId=$eventId")
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this event: $deepLinkUrl")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share using"))
    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            viewModal.eventId.set(intent.getInt(AppConstants.EVENT_ID).toString())
            position = intent.getInt(AppConstants.POSITION)
        }
    }

    private fun apiHit() {
        RunInScope.ioThread {
            viewModal.hitEventDetailsApi()
        }
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            tvToolTitle.text = getString(R.string.event_details)
            ivBack.setOnClickListener {
                finishActivity()
            }
        }
    }

    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.eventDetailsSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            eventDetails = data.data
                            setDataApi(data.data)
                            initCommentRecyclerView(data.data.commentList ?: arrayListOf())
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
            viewModal.favouriteEventStatusSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.success == true) {
                            if(eventDetails?.isFavoritedbyUser == true){
                                eventDetails?.isFavoritedbyUser = false
                                binding.apply {
                                    eventDetails?.totalFavourites = eventDetails?.totalFavourites?.minus(1)!!
                                    ivFavourite.setBackgroundResource(R.drawable.ic_fav_dislike)
                                }
                                listCallBackSheet.invoke(position,true,eventDetails?.totalComments ?: 0,eventDetails?.totalFavourites ?: 0,1)
                            }else{
                                binding.apply {
                                    eventDetails?.totalFavourites = eventDetails?.totalFavourites?.plus(1)!!
                                    ivFavourite.setBackgroundResource(R.drawable.like_pic)
                                }
                                eventDetails?.isFavoritedbyUser = true
                                listCallBackSheet.invoke(position,true,eventDetails?.totalComments ?: 0,eventDetails?.totalFavourites ?: 0,2)
                            }
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
                binding.apply {
                    layoutOtherView.gone()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }
            } else {
                binding.apply {
                    layoutOtherView.visible()
                    shimmerCommentList.shimmerAnimationEffect(it)
                }
            }
        }
    }

    private fun setDataApi(data: Details) {
        binding.apply {
            if (data.docFileName?.isNotBlank() == true){
                relativeLayout.visible()
                tvEmail.text = data.title.replace(" ","_").trim().lowercase(Locale.ROOT).plus(".pdf")
            }else{
                relativeLayout.gone()
            }
            tvUsername.text = data.title
            tvDesc.text = data.eventDesc
            val formattedDate = data.eventDate.convertDateFormat("yyyy-dd-MM", "dd/MM/yyyy")
            tvDate.text = getString(R.string.date_, formattedDate)
            val formattedTime = data.eventTime.convertTimeFormat("HH:mm:ss", "hh:mm a")
            tvTime.text = getString(R.string.time_, formattedTime)
            tvViewAllComments.text = getString(R.string.view_all).plus(data.totalComments.toString().plus(" ").plus("comments"))
            var countLikesByOther = data.totalFavourites
           /** cant show my like count in this that's why remove 1*/
            if (data.isFavoritedbyUser){
                countLikesByOther = countLikesByOther.minus(1)
                htmlString = "Liked by <font color=\"#31B5A0\">${countLikesByOther} </font> others"
            }else{
                htmlString = "Liked by <font color=\"#31B5A0\">${data.totalFavourites} </font> others"
            }
            tvLikedBy.text = Html.fromHtml(htmlString,Html.FROM_HTML_MODE_COMPACT)

            if (data.isFavoritedbyUser) {
                ivFavourite.setBackgroundResource(R.drawable.like_pic)
            } else {
                ivFavourite.setBackgroundResource(R.drawable.ic_fav_dislike)
            }
            initLatestLikeRecyclerView(data.usersListWhoFavouriteEvent ?: arrayListOf())
            if (data.isImage) {
                videoView.gone()
                ivPlayOrPause.gone()
                ivHome.visible()
                videoProgress.gone()
                ivHome.setImageFromUrl(data.mediaPath, progressBar)
            } else {
                videoView.visible()
                ivPlayOrPause.visible()
                ivHome.gone()
                initPlayer(data.mediaPath)
                binding.videoView.player = player
            }
        }
    }


    /** set recycler view Meditation  List */
    private fun initLatestLikeRecyclerView(data: ArrayList<WhoLikesFavouriteList>) {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rcLatestLikeList.apply {
            adapter = latestLikeListAdapter
            setHasFixedSize(true)
            addItemDecoration(OverlapDecoration(data.size-1))
            layoutManager.stackFromEnd = false
            setLayoutManager(layoutManager)
        }
        latestLikeListAdapter.submitList(data)
    }


    private val latestLikeListAdapter = object : GenericAdapter<ItemLatestLikeListBinding, WhoLikesFavouriteList>() {
            override fun getResourceLayoutId(): Int {
                return R.layout.item_latest_like_list
            }

            override fun onBindHolder(holder: ItemLatestLikeListBinding, dataClass: WhoLikesFavouriteList, position: Int) {
                holder.apply {
                    circleImageView.setImageFromUrl(R.drawable.no_image_placeholder,dataClass.userImage)
                }
            }
        }


    class OverlapDecoration(private var counts: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val itemPosition = parent.getChildAdapterPosition(view)
            if(counts>0 && itemPosition<counts){
                outRect.set(0, 0, vertOverlap, 0)
            }
        }
        companion object {
            private const val vertOverlap = -20
        }
    }

    @OptIn(UnstableApi::class) private fun setUri(mediaPath: String) {
        val metaData = MediaMetadata.Builder()
            .setTitle("Title")
            .setAlbumTitle("Album")
            .build()

        val mediaMetaData = MediaItem.Builder()
            .setUri(mediaPath.toUri())
            .setMediaMetadata(metaData)
            .build()
        val mediaItem = mediaPath.let { MediaItem.fromUri(it) }
        val mediaSourceFactory = ProgressiveMediaSource.Factory(MediaCache.getInstance(context = applicationContext).cacheFactory)
        val mediaSource = mediaItem.let { mediaSourceFactory.createMediaSource(it) }
        mediaSource.let { player?.setMediaSource(it, true) }
        player?.repeatMode = Player.REPEAT_MODE_ALL
        player?.setMediaItem(mediaMetaData)
        player?.prepare()
    }


    private fun initPlayer(mediaPath: String) {
        player = ExoPlayer.Builder(applicationContext)
            .build()
            .apply { addListener(playerListener) }
        setUri(mediaPath)
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

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
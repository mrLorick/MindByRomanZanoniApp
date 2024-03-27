package com.lorick.chatterbox.data.response.eventDetails

import com.lorick.chatterbox.base.BaseResponse

data class EventDetailsResponse(
    val `data`: Details,
) : BaseResponse()

data class Details(
    val createdOn: String,
    val eventDate: String,
    val eventDesc: String,
    val eventId: Int,
    val eventTime: String,
    val isImage: Boolean,
    var isFavoritedbyUser: Boolean,
    val mediaPath: String,
    val title: String,
    var totalComments: Int,
    var totalFavourites: Int,
    val totalShared: Int,
    val usersListWhoFavouriteEvent: ArrayList<WhoLikesFavouriteList>? = arrayListOf(),
    val zoomLink: String,
    val commentList : ArrayList<CommentListResponse>? = arrayListOf(),
    val docFileName: String?
)

data class WhoLikesFavouriteList(
    val userId: Int,
    val userImage: String,
    val userName: String?,
    val favEventId: Int,
)

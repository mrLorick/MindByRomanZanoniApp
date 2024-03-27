package com.lorick.chatterbox.data.response.home

import com.lorick.chatterbox.base.BaseResponse

data class EventResponse(
    val `data`: ArrayList<EventListResponse>,
) : BaseResponse()

data class EventListResponse(
    val createdOn: String,
    val eventDesc: String,
    val eventDate: String,
    val eventTime: String,
    val eventId: Int,
    var isFavoritedbyUser: Boolean,
    var isImage: Boolean,
    val mediaPath: String,
    val title: String,
    var totalComments: Int,
    var totalFavourites: Int,
    val totalShared: Int,
    val usersListWhoFavouriteEvent: Any,
    val zoomLink: String,
    val videoThumbImage: String
)
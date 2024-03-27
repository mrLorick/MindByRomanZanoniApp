package com.lorick.chatterbox.data.response.eventDetails

import com.lorick.chatterbox.base.BaseResponse

data class CommentResponse(
    val `data`: ArrayList<CommentListResponse>?,
) : BaseResponse()

data class CommentListResponse(
    val commentDesc: String?,
    val commentId: Int?,
    val commentedById: Int?,
    val commentedByImage: String? = "",
    val commentedByName: String?= "",
    val commentedOn: String?,
    val eventId: Int?
)
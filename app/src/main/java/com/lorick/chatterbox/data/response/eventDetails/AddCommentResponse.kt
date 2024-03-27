package com.lorick.chatterbox.data.response.eventDetails

import com.lorick.chatterbox.base.BaseResponse

data class AddCommentResponse(
    val `data`: CommentListResponse,
) : BaseResponse()

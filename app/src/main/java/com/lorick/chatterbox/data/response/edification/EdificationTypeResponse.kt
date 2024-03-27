package com.lorick.chatterbox.data.response.edification

import com.lorick.chatterbox.base.BaseResponse

data class EdificationTypeResponse(
    val `data`: ArrayList<EdificationTypeListResponse>,
    ) : BaseResponse()

data class EdificationTypeListResponse(
    val isImage: Boolean? = false,
    val categoryId: Int,
    val categoryName: String?,
    val content: String?,
    val createdOn: String?,
    val duration: String?,
    val meditationTypeId: Int,
    val title: String?,
    val videoName: String?,
    val videoThumbImage: String?
)

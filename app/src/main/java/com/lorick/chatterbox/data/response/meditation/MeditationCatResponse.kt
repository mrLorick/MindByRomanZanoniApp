package com.lorick.chatterbox.data.response.meditation

import com.lorick.chatterbox.base.BaseResponse

data class MeditationCatResponse(
    val `data`: ArrayList<MeditationCatListResponse>,
) : BaseResponse()

data class MeditationCatListResponse(
    val categoryImage: String,
    val categoryName: String,
    val courseDuration: String,
    val createdOn: String,
    val medidationCatId: Int,
    val meditationTypes: List<Any>
)
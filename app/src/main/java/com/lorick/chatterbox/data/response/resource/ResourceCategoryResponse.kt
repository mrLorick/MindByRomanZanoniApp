package com.lorick.chatterbox.data.response.resource

import com.lorick.chatterbox.base.BaseResponse

data class ResourceCategoryResponse(
    val `data`: ArrayList<ResourceCategoryList>?,

    ) : BaseResponse()

data class ResourceCategoryList(
    val categoryImage: String,
    val resourceTypeId: Int,
    val resourceTypeMain: String
)

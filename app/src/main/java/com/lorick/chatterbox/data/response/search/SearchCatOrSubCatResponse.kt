package com.lorick.chatterbox.data.response.search

import com.lorick.chatterbox.base.BaseResponse

data class SearchCatOrSubCatResponse(
    val `data`: ArrayList<SearchCatOrSubCatListResponse>? = arrayListOf(),
) : BaseResponse()

data class SearchCatOrSubCatListResponse(
    val categoryId: Int,
    val categoryName: String,
    val content: String,
    val createdOn: String,
    val duration: String,
    val videoName: String,
    val id: Int,
    val thumbImage: String,
    val title: String,
    val typeId: Int
)
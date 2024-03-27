package com.lorick.chatterbox.data.response.resource

import com.lorick.chatterbox.base.BaseResponse

data class ResourcesResponse(
    val `data`: ResourcesDataResponse,
) : BaseResponse()

data class ResourcesDataResponse(
    val exerciseRoutineResources: ArrayList<MealInspirationResource>,
    val mealInspirationResources: ArrayList<MealInspirationResource>
)

data class ExerciseRoutineResource(
    val content: String,
    val createdOn: String,
    val imageName: String,
    val pdfFileName: String,
    val resourceType: String,
    val resourcesId: Int,
    val title: String
)

data class MealInspirationResource(
    val content: String,
    val createdOn: String,
    val imageName: String,
    val pdfFileName: String,
    val resourceType: String,
    val resourcesId: Int,
    val title: String,
    val resourcesTypeId : Int,
)
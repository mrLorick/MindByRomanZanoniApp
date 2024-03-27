package com.lorick.chatterbox.data.response.userData

import com.lorick.chatterbox.base.BaseResponse

data class UserDataResponse(
    val data: UserDataModel? = null,
) : BaseResponse()

data class UserDataModel(
    var contactNumber: String,
    var countryCode: String,
    val deviceToken: String,
    val deviceType: String,
    val invitationLink: String,
    val email: String,
    val isBiometricOn: Boolean,
    val isEmailVerified: Boolean = false,
    var isNotificationOn: Boolean,
    val location: String,
    var name: String,
    val token: String,
    val userId: Int,
    var userImage: String
)
package com.lorick.chatterbox.data.response.userMessageList

data class MessageResponse(
    val Messages: ArrayList<MessageListResponse>? = arrayListOf(),
    val OtherUserId: Int,
    val OtherUserImage: String,
    val OtherUserName: String
)

data class MessageListResponse(
    val ChatId: Int?,
    val Image: String?,
    val Message: String?,
    val MessageOn: String?,
    val MessageType: String?,
    val Name: String?,
    val OtherUserId: Int?,
    val UserId: Int?,
    val status: Int?,
    var IsRead: Boolean? = false,
)

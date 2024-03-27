package com.lorick.chatterbox.data.response.userMessageList


class UserMessageResponse(
    var Success: Boolean,
    var status_code: Int,
    var Message : String,
    var Data: ArrayList<UserMessageListResponse>? = null
)

class UserMessageListResponse(
    var FullName: String,
    var Image: String,
    var Rating: Float,
    var UnseenCount: Int,
    var LastMessage: String,
    var ErrorMessage : String,
    var MessageOn: String,
    var OtherUserId: Int,
    var MessageType: String,
    var UniqueID: String,
    var Email: String,
)

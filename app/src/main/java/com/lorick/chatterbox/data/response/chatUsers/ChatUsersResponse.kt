package com.lorick.chatterbox.data.response.chatUsers

import com.lorick.chatterbox.base.BaseResponse

data class ChatUsersResponse(
    val `data`: ArrayList<ChatUsers>,
) : BaseResponse()

data class ChatUsers(
    val name: String,
    val email: String,
    val userId: Int,
    val userImage: String
)
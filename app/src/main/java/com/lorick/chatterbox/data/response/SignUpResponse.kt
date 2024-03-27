package com.lorick.chatterbox.data.response

data class SignUpResponse(
    val `data`: Any,
    val message: String,
    val status_code: Int,
    val success: Boolean
)
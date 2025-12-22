package com.aivanouski.auth_kotlin_demo.application.dto.response

data class MessageResponse(
    val message: String,
    val success: Boolean = true
)

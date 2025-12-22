package com.aivanouski.auth_kotlin_demo.application.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String?,

    @field:NotBlank(message = "Password is required")
    val password: String?
)

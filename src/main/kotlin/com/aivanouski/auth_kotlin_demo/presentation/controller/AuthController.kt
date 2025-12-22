package com.aivanouski.auth_kotlin_demo.presentation.controller

import com.aivanouski.auth_kotlin_demo.application.dto.request.ForgotPasswordRequest
import com.aivanouski.auth_kotlin_demo.application.dto.request.LoginRequest
import com.aivanouski.auth_kotlin_demo.application.dto.request.RefreshTokenRequest
import com.aivanouski.auth_kotlin_demo.application.dto.request.RegisterRequest
import com.aivanouski.auth_kotlin_demo.application.dto.request.ResetPasswordRequest
import com.aivanouski.auth_kotlin_demo.application.dto.response.AuthResponse
import com.aivanouski.auth_kotlin_demo.application.dto.response.MessageResponse
import com.aivanouski.auth_kotlin_demo.application.service.AuthService
import com.aivanouski.auth_kotlin_demo.application.service.EmailVerificationService
import com.aivanouski.auth_kotlin_demo.application.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request.refreshToken!!)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/verify-email")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<MessageResponse> {
        emailVerificationService.verifyEmail(token)
        return ResponseEntity.ok(
            MessageResponse(
                message = "Email verified successfully. You can now log in."
            )
        )
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        passwordResetService.createPasswordResetToken(request.email!!)
        return ResponseEntity.ok(
            MessageResponse(
                message = "If the email exists, a password reset link has been sent."
            )
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        passwordResetService.resetPassword(request.token!!, request.newPassword!!)
        return ResponseEntity.ok(
            MessageResponse(
                message = "Password reset successfully. You can now log in with your new password."
            )
        )
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(
            MessageResponse(
                message = "Auth service is running"
            )
        )
    }
}

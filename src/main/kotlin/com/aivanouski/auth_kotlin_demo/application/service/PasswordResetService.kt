package com.aivanouski.auth_kotlin_demo.application.service

import com.aivanouski.auth_kotlin_demo.application.exception.BadRequestException
import com.aivanouski.auth_kotlin_demo.application.exception.InvalidTokenException
import com.aivanouski.auth_kotlin_demo.application.exception.TokenExpiredException
import com.aivanouski.auth_kotlin_demo.domain.entity.PasswordResetToken
import com.aivanouski.auth_kotlin_demo.infrastructure.config.SecurityProperties
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.PasswordResetTokenRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val securityProperties: SecurityProperties
) {

    @Transactional
    fun createPasswordResetToken(email: String) {
        val user = userRepository.findByEmail(email)
            .getOrNull() ?: return

        passwordResetTokenRepository.deleteByUser(user)

        val token = PasswordResetToken(
            token = UUID.randomUUID().toString(),
            user = user,
            expiresAt = Instant.now().plusMillis(securityProperties.passwordReset.tokenExpiration)
        )
        val savedToken = passwordResetTokenRepository.save(token)

        val resetUrl = "${securityProperties.passwordReset.baseUrl}/reset-password?token=${savedToken.token}"
        emailService.sendPasswordResetEmail(user.email!!, user.firstName!!, resetUrl)
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow { InvalidTokenException("Invalid password reset token") }

        if (resetToken.used) {
            throw BadRequestException("Token has already been used")
        }

        if (resetToken.expiresAt!!.isBefore(Instant.now())) {
            throw TokenExpiredException("Password reset token has expired")
        }

        val user = resetToken.user!!
        val updatedUser = user.copy(
            password = passwordEncoder.encode(newPassword) ?: throw RuntimeException("Failed to encode password"),
            updatedAt = Instant.now()
        )
        userRepository.save(updatedUser)

        resetToken.used = true
        passwordResetTokenRepository.save(resetToken)
    }
}
package com.aivanouski.auth_kotlin_demo.application.service

import com.aivanouski.auth_kotlin_demo.application.exception.BadRequestException
import com.aivanouski.auth_kotlin_demo.application.exception.InvalidTokenException
import com.aivanouski.auth_kotlin_demo.application.exception.TokenExpiredException
import com.aivanouski.auth_kotlin_demo.domain.entity.EmailVerificationToken
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import com.aivanouski.auth_kotlin_demo.infrastructure.config.SecurityProperties
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.EmailVerificationTokenRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val securityProperties: SecurityProperties
) {

    @Transactional
    fun createVerificationToken(user: User): EmailVerificationToken {
        emailVerificationTokenRepository.deleteByUser(user)

        val token = EmailVerificationToken(
            token = UUID.randomUUID().toString(),
            user = user,
            expiresAt = Instant.now().plusMillis(securityProperties.emailVerification.tokenExpiration)
        )
        val savedToken = emailVerificationTokenRepository.save(token)

        val verificationUrl = "${securityProperties.emailVerification.baseUrl}/api/v1/auth/verify-email?token=${savedToken.token}"
        emailService.sendVerificationEmail(user.email!!, user.firstName!!, verificationUrl)

        return savedToken
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            .orElseThrow { InvalidTokenException("Invalid verification token") }

        if (verificationToken.used) {
            throw BadRequestException("Token has already been used")
        }

        if (verificationToken.expiresAt!!.isBefore(Instant.now())) {
            throw TokenExpiredException("Verification token has expired")
        }

        val user = verificationToken.user!!
        val updatedUser = user.copy(
            emailVerified = true,
            enabled = true,
            updatedAt = Instant.now()
        )
        userRepository.save(updatedUser)

        verificationToken.used = true
        emailVerificationTokenRepository.save(verificationToken)
    }
}
package com.aivanouski.auth_kotlin_demo.application.service

import com.aivanouski.auth_kotlin_demo.application.exception.InvalidTokenException
import com.aivanouski.auth_kotlin_demo.application.exception.TokenExpiredException
import com.aivanouski.auth_kotlin_demo.domain.entity.RefreshToken
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import com.aivanouski.auth_kotlin_demo.infrastructure.config.SecurityProperties
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val securityProperties: SecurityProperties
) {

    @Transactional
    fun createRefreshToken(user: User): RefreshToken {
        val token = RefreshToken(
            token = UUID.randomUUID().toString(),
            user = user,
            expiresAt = Instant.now().plusMillis(securityProperties.jwt.refreshTokenExpiration)
        )
        return refreshTokenRepository.save(token)
    }

    @Transactional
    fun validateRefreshToken(token: String): RefreshToken {
        val refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow { InvalidTokenException("Invalid refresh token") }

        if (refreshToken.revoked) {
            throw InvalidTokenException("Refresh token has been revoked")
        }

        if (refreshToken.expiresAt!!.isBefore(Instant.now())) {
            throw TokenExpiredException("Refresh token has expired")
        }

        return refreshToken
    }

    @Transactional
    fun revokeRefreshToken(token: String) {
        refreshTokenRepository.findByToken(token).ifPresent { refreshToken ->
            refreshToken.revoked = true
            refreshTokenRepository.save(refreshToken)
        }
    }

    @Transactional
    fun revokeAllUserTokens(user: User) {
        refreshTokenRepository.revokeAllByUser(user)
    }
}
package com.aivanouski.auth_kotlin_demo.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application.security")
data class SecurityProperties(
    var jwt: JwtProperties = JwtProperties(),
    var emailVerification: EmailVerificationProperties = EmailVerificationProperties(),
    var passwordReset: PasswordResetProperties = PasswordResetProperties()
)

data class JwtProperties(
    var secretKey: String = "",
    var accessTokenExpiration: Long = 900000,
    var refreshTokenExpiration: Long = 604800000
)

data class EmailVerificationProperties(
    var tokenExpiration: Long = 86400000,
    var baseUrl: String = "http://localhost:8080"
)

data class PasswordResetProperties(
    var tokenExpiration: Long = 3600000,
    var baseUrl: String = "http://localhost:8080"
)
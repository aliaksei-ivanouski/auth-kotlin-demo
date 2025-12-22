package com.aivanouski.auth_kotlin_demo.application.service

import com.aivanouski.auth_kotlin_demo.application.dto.request.LoginRequest
import com.aivanouski.auth_kotlin_demo.application.dto.request.RegisterRequest
import com.aivanouski.auth_kotlin_demo.application.dto.response.AuthResponse
import com.aivanouski.auth_kotlin_demo.application.dto.response.UserResponse
import com.aivanouski.auth_kotlin_demo.application.exception.EmailAlreadyExistsException
import com.aivanouski.auth_kotlin_demo.application.exception.UnauthorizedException
import com.aivanouski.auth_kotlin_demo.domain.entity.Role
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import com.aivanouski.auth_kotlin_demo.infrastructure.config.SecurityProperties
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val tokenService: TokenService,
    private val emailVerificationService: EmailVerificationService,
    private val authenticationManager: AuthenticationManager,
    private val securityProperties: SecurityProperties
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email!!)) {
            throw EmailAlreadyExistsException("Email already exists: ${request.email}")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password) ?: throw RuntimeException("Failed to encode password"),
            firstName = request.firstName!!,
            lastName = request.lastName!!,
            role = Role.USER,
            enabled = false,
            emailVerified = false
        )

        val savedUser = userRepository.save(user)
        emailVerificationService.createVerificationToken(savedUser)

        return generateAuthResponse(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email!!)
            .orElseThrow { UnauthorizedException("Invalid credentials") }

        if (!user.emailVerified) {
            throw UnauthorizedException("Email not verified. Please check your email.")
        }

        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(refreshTokenString: String): AuthResponse {
        val refreshToken = tokenService.validateRefreshToken(refreshTokenString)
        val user = refreshToken.user!!

        tokenService.revokeRefreshToken(refreshTokenString)

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = tokenService.createRefreshToken(user)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken.token!!,
            expiresIn = securityProperties.jwt.accessTokenExpiration / 1000,
            user = UserResponse.fromEntity(user)
        )
    }
}
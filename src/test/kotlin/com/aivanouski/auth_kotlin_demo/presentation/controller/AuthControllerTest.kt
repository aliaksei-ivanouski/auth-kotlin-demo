package com.aivanouski.auth_kotlin_demo.presentation.controller

import com.aivanouski.auth_kotlin_demo.BaseIntegrationTest
import com.aivanouski.auth_kotlin_demo.domain.entity.Role
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.EmailVerificationTokenRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.PasswordResetTokenRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.RefreshTokenRepository
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@DisplayName("AuthController Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest : BaseIntegrationTest() {

    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository

    @Autowired
    lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    inner class RegisterTests {

        @Test
        fun `should register user successfully with valid data`() {
            // When & Then
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "test@example.com",
                        "password": "password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isCreated() }
                    content { jsonPath("$.accessToken", notNullValue()) }
                    content { jsonPath("$.refreshToken", notNullValue()) }
                    content { jsonPath("$.expiresIn", `is`(900)) }
                    content { jsonPath("$.user.email", `is`("test@example.com")) }
                    content { jsonPath("$.user.firstName", `is`("John")) }
                    content { jsonPath("$.user.lastName", `is`("Doe")) }
                    content { jsonPath("$.user.role", `is`(Role.USER.name)) }
                    content { jsonPath("$.user.emailVerified", `is`(false)) }
                }
        }

        @Test
        fun `should return 400 when fields validation failed`() {
            // When & Then
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "invalid-email",
                        "password": "passw",
                        "firstName": "",
                        "lastName": ""
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Validation Failed")) }
                    content { jsonPath("$.message", `is`("Validation failed for one or more fields")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/register")) }
                    content { jsonPath("$.fieldErrors.email", `is`("Email must be valid")) }
                    content { jsonPath("$.fieldErrors.password", `is`("Password must be at least 8 characters")) }
                    content { jsonPath("$.fieldErrors.firstName", `is`("First name is required")) }
                    content { jsonPath("$.fieldErrors.lastName", `is`("Last name is required")) }
                }
        }

        @Test
        fun `should return 409 when email already exists`() {
            // When & Then
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com",
                        "password": "password123",
                        "firstName": "Joe",
                        "lastName": "Doe"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isConflict() }
                    content { jsonPath("$.status", `is`(409)) }
                    content { jsonPath("$.error", `is`("Conflict")) }
                    content { jsonPath("$.message", `is`("Email already exists: user@test.com")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/register")) }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    inner class LoginTests {

        @Test
        fun `should login successfully with valid credentials`() {
            // When & Then
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                {
                    "email": "user@test.com",
                    "password": "letmein123456"
                }
            """.trimIndent()
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.accessToken", notNullValue()) }
                content { jsonPath("$.refreshToken", notNullValue()) }
                content { jsonPath("$.tokenType", `is`("Bearer")) }
                content { jsonPath("$.expiresIn", `is`(900)) }
                content { jsonPath("$.user.id", notNullValue()) }
                content { jsonPath("$.user.email", `is`("user@test.com")) }
                content { jsonPath("$.user.firstName", `is`("Kelly")) }
                content { jsonPath("$.user.lastName", `is`("Done")) }
                content { jsonPath("$.user.role", `is`(Role.USER.name)) }
                content { jsonPath("$.user.emailVerified", `is`(true)) }
            }
        }

        @Test
        fun `should return 400 when email is missing`() {
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "password": "password123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Validation Failed")) }
                    content { jsonPath("$.message", `is`("Validation failed for one or more fields")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/login")) }
                    content { jsonPath("$.fieldErrors.email", `is`("Email is required")) }
                }
        }

        @Test
        fun `should return 401 when email is invalid`() {
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "invalid@test.com",
                        "password": "password123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Authentication Failed")) }
                    content { jsonPath("$.message", `is`("Invalid email or password")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/login")) }
                }
        }

        @Test
        fun `should return 401 when password is invalid`() {
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com",
                        "password": "invalid-password"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Authentication Failed")) }
                    content { jsonPath("$.message", `is`("Invalid email or password")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/login")) }
                }
        }

        @Test
        fun `should return 401 when email is not verified`() {
            // Given
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "test@example.com",
                        "password": "password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                """.trimIndent()
            }.andReturn()

            // When & Then
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Account Disabled")) }
                    content { jsonPath("$.message", `is`("Your account has been disabled. Please contact support.")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/login")) }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    inner class RefreshTokenTests {

        @Test
        fun `should refresh token successfully`() {
            // Given
            val refreshToken = refreshToken("user")

            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                {
                    "refreshToken": "$refreshToken"
                }
            """.trimIndent()
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.accessToken", notNullValue()) }
                content { jsonPath("$.refreshToken", notNullValue()) }
                content { jsonPath("$.refreshToken", not(refreshToken)) }
                content { jsonPath("$.tokenType", `is`("Bearer")) }
                content { jsonPath("$.expiresIn", `is`(900)) }
                content { jsonPath("$.user.id", notNullValue()) }
                content { jsonPath("$.user.email", `is`("user@test.com")) }
                content { jsonPath("$.user.firstName", `is`("Kelly")) }
                content { jsonPath("$.user.lastName", `is`("Done")) }
                content { jsonPath("$.user.role", `is`(Role.USER.name)) }
                content { jsonPath("$.user.emailVerified", `is`(true)) }
            }
        }

        @Test
        fun `should return 400 when refresh token is blank`() {
            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                {
                    "refreshToken": ""
                }
            """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                content { jsonPath("$.status", `is`(400)) }
                content { jsonPath("$.error", `is`("Validation Failed")) }
                content { jsonPath("$.message", `is`("Validation failed for one or more fields")) }
                content { jsonPath("$.path", `is`("/api/v1/auth/refresh")) }
                content { jsonPath("$.fieldErrors.refreshToken", `is`("Refresh token is required")) }
            }
        }

        @Test
        fun `should return 401 when refresh token is invalid`() {
            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                {
                    "refreshToken": "${UUID.randomUUID()}"
                }
            """.trimIndent()
            }.andExpect {
                status { isUnauthorized() }
                content { jsonPath("$.status", `is`(401)) }
                content { jsonPath("$.error", `is`("Invalid Token")) }
                content { jsonPath("$.message", `is`("Invalid refresh token")) }
                content { jsonPath("$.path", `is`("/api/v1/auth/refresh")) }
            }
        }

        @Test
        fun `should return 401 when refresh token is expired`() {
            // Given
            val refreshToken = refreshToken("user")
            val refreshTokenObject = refreshTokenRepository.findByToken(refreshToken).get()
            refreshTokenObject.expiresAt = Instant.now().minus(1, ChronoUnit.MINUTES)
            refreshTokenRepository.save(refreshTokenObject)

            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                {
                    "refreshToken": "$refreshToken"
                }
            """.trimIndent()
            }.andExpect {
                status { isUnauthorized() }
                content { jsonPath("$.status", `is`(401)) }
                content { jsonPath("$.error", `is`("Token Expired")) }
                content { jsonPath("$.message", `is`("Refresh token has expired")) }
                content { jsonPath("$.path", `is`("/api/v1/auth/refresh")) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/verify-email")
    inner class VerifyEmailTests {

        @Test
        fun `should verify email successfully`() {
            // Given
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "verify@example.com",
                        "password": "password123",
                        "firstName": "Verify",
                        "lastName": "Test"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("verify@example.com").get()
            val verificationToken = emailVerificationTokenRepository.findByUser(user).get()

            // When & Then
            mockMvc.get("/api/v1/auth/verify-email") {
                param("token", verificationToken.token!!)
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.message", `is`("Email verified successfully. You can now log in.")) }
                    content { jsonPath("$.success", `is`(true)) }
                }
        }

        @Test
        fun `should return 401 when verification token is invalid`() {
            // Given
            val token = UUID.randomUUID().toString()

            // When & Then
            mockMvc.get("/api/v1/auth/verify-email") {
                param("token", token)
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Invalid Token")) }
                    content { jsonPath("$.message", `is`("Invalid verification token")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/verify-email")) }
                }
        }

        @Test
        fun `should return 401 when verification token is expired`() {
            // Given
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "expired@example.com",
                        "password": "password123",
                        "firstName": "Expired",
                        "lastName": "Test"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("expired@example.com").get()
            val verificationToken = emailVerificationTokenRepository.findByUser(user).get()
            verificationToken.expiresAt = Instant.now().minus(1, ChronoUnit.HOURS)
            emailVerificationTokenRepository.save(verificationToken)

            // When & Then
            mockMvc.get("/api/v1/auth/verify-email") {
                param("token", verificationToken.token!!)
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Token Expired")) }
                    content { jsonPath("$.message", `is`("Verification token has expired")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/verify-email")) }
                }
        }

        @Test
        fun `should return 400 when token has already been used`() {
            // Given
            mockMvc.post("/api/v1/auth/register") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "used@example.com",
                        "password": "password123",
                        "firstName": "Used",
                        "lastName": "Test"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("used@example.com").get()
            val verificationToken = emailVerificationTokenRepository.findByUser(user).get()
            verificationToken.used = true
            emailVerificationTokenRepository.save(verificationToken)

            // When & Then
            mockMvc.get("/api/v1/auth/verify-email") {
                param("token", verificationToken.token!!)
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Bad Request")) }
                    content { jsonPath("$.message", `is`("Token has already been used")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/verify-email")) }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    inner class ForgotPasswordTests {

        @Test
        fun `should send password reset email successfully`() {
            // When & Then
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.message", `is`("If the email exists, a password reset link has been sent.")) }
                    content { jsonPath("$.success", `is`(true)) }
                }
        }

        @Test
        fun `should return 400 when email is invalid`() {
            // When & Then
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "invalid-email"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Validation Failed")) }
                    content { jsonPath("$.message", `is`("Validation failed for one or more fields")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/forgot-password")) }
                    content { jsonPath("$.fieldErrors.email", `is`("Email must be valid")) }
                }
        }

        @Test
        fun `should return 200 even when user not found for security`() {
            // When & Then
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "nonexistent@example.com"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.message", `is`("If the email exists, a password reset link has been sent.")) }
                    content { jsonPath("$.success", `is`(true)) }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    inner class ResetPasswordTests {

        @Test
        fun `should reset password successfully`() {
            // Given
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("user@test.com").get()
            val resetToken = passwordResetTokenRepository.findByUser(user).get()

            // When & Then
            mockMvc.post("/api/v1/auth/reset-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "token": "${resetToken.token}",
                        "newPassword": "newpassword123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.message", `is`("Password reset successfully. You can now log in with your new password.")) }
                    content { jsonPath("$.success", `is`(true)) }
                }
        }

        @Test
        fun `should return 400 when new password is too short`() {
            // When & Then
            mockMvc.post("/api/v1/auth/reset-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "token": "valid-token",
                        "newPassword": "short"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Validation Failed")) }
                    content { jsonPath("$.message", `is`("Validation failed for one or more fields")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/reset-password")) }
                    content { jsonPath("$.fieldErrors.newPassword", `is`("Password must be at least 8 characters")) }
                }
        }

        @Test
        fun `should return 401 when reset token is invalid`() {
            // Given
            val token = UUID.randomUUID().toString()

            // When & Then
            mockMvc.post("/api/v1/auth/reset-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "token": "$token",
                        "newPassword": "newpassword123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Invalid Token")) }
                    content { jsonPath("$.message", `is`("Invalid password reset token")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/reset-password")) }
                }
        }

        @Test
        fun `should return 401 when reset token is expired`() {
            // Given
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("user@test.com").get()
            val resetToken = passwordResetTokenRepository.findByUser(user).get()
            resetToken.expiresAt = Instant.now().minus(1, ChronoUnit.HOURS)
            passwordResetTokenRepository.save(resetToken)

            // When & Then
            mockMvc.post("/api/v1/auth/reset-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "token": "${resetToken.token}",
                        "newPassword": "newpassword123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.status", `is`(401)) }
                    content { jsonPath("$.error", `is`("Token Expired")) }
                    content { jsonPath("$.message", `is`("Password reset token has expired")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/reset-password")) }
                }
        }

        @Test
        fun `should return 400 when reset token has already been used`() {
            // Given
            mockMvc.post("/api/v1/auth/forgot-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "email": "user@test.com"
                    }
                """.trimIndent()
            }.andReturn()

            val user = userRepository.findByEmail("user@test.com").get()
            val resetToken = passwordResetTokenRepository.findByUser(user).get()
            resetToken.used = true
            passwordResetTokenRepository.save(resetToken)

            // When & Then
            mockMvc.post("/api/v1/auth/reset-password") {
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "token": "${resetToken.token}",
                        "newPassword": "newpassword123"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.status", `is`(400)) }
                    content { jsonPath("$.error", `is`("Bad Request")) }
                    content { jsonPath("$.message", `is`("Token has already been used")) }
                    content { jsonPath("$.path", `is`("/api/v1/auth/reset-password")) }
                }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/health")
    inner class HealthTests {

        @Test
        fun `should return health status without authentication`() {
            // When & Then
            mockMvc.get("/api/v1/auth/health")
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.message", `is`("Auth service is running")) }
                    content { jsonPath("$.success", `is`(true)) }
                }
        }
    }
}

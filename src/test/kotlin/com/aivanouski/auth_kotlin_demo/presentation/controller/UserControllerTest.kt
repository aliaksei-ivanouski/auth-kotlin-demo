package com.aivanouski.auth_kotlin_demo.presentation.controller

import com.aivanouski.auth_kotlin_demo.BaseIntegrationTest
import com.aivanouski.auth_kotlin_demo.domain.entity.Role
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@DisplayName("UserController Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Nested
    @DisplayName("GET /api/v1/users/me")
    inner class GetCurrentUserTests {

        @Test
        fun `should return current user successfully`() {
            // Given
            val token = authToken("user")

            // When & Then
            mockMvc.get("/api/v1/users/me") {
                accept = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $token")
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.id", notNullValue()) }
                    content { jsonPath("$.email", `is`("user@test.com")) }
                    content { jsonPath("$.firstName", `is`("Kelly")) }
                    content { jsonPath("$.lastName", `is`("Done")) }
                    content { jsonPath("$.role", `is`(Role.USER.name)) }
                    content { jsonPath("$.emailVerified", `is`(true)) }
                }
        }

        @Test
        fun `should return admin user successfully`() {
            // Given
            val token = authToken("admin")

            // When & Then
            mockMvc.get("/api/v1/users/me") {
                accept = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $token")
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.id", notNullValue()) }
                    content { jsonPath("$.email", `is`("admin@test.com")) }
                    content { jsonPath("$.firstName", `is`("John")) }
                    content { jsonPath("$.lastName", `is`("Smith")) }
                    content { jsonPath("$.role", `is`(Role.ADMIN.name)) }
                    content { jsonPath("$.emailVerified", `is`(true)) }
                }
        }

        @Test
        fun `should return 401 when user is not authenticated`() {
            // When & Then
            mockMvc.get("/api/v1/users/me") {
                accept = MediaType.APPLICATION_JSON
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.error", `is`("Unauthorized")) }
                    content { jsonPath("$.message", `is`("Authentication required")) }
                }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/all")
    inner class GetAllUsersTests {

        @Test
        fun `should return all users when user is admin`() {
            // Given
            val token = authToken("admin")

            // When & Then
            mockMvc.get("/api/v1/users/all") {
                accept = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $token")
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$").isArray }
                    content { jsonPath("$.length()", `is`(3)) }
                    content { jsonPath("$[0].email", notNullValue()) }
                    content { jsonPath("$[1].email", notNullValue()) }
                    content { jsonPath("$[2].email", notNullValue()) }
                }
        }

        @Test
        fun `should return 403 when user is not admin`() {
            // Given
            val token = authToken("user")

            // When & Then
            mockMvc.get("/api/v1/users/all") {
                accept = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $token")
            }
                .andExpect {
                    status { isForbidden() }
                    content { jsonPath("$.status", `is`(403)) }
                    content { jsonPath("$.error", `is`("Forbidden")) }
                    content { jsonPath("$.message", `is`("Access denied")) }
                    content { jsonPath("$.path", `is`("/api/v1/users/all")) }
                }
        }

        @Test
        fun `should return 403 when user is moderator`() {
            // Given
            val token = authToken("moderator")

            // When & Then
            mockMvc.get("/api/v1/users/all") {
                accept = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $token")
            }
                .andExpect {
                    status { isForbidden() }
                    content { jsonPath("$.status", `is`(403)) }
                    content { jsonPath("$.error", `is`("Forbidden")) }
                    content { jsonPath("$.message", `is`("Access denied")) }
                    content { jsonPath("$.path", `is`("/api/v1/users/all")) }
                }
        }

        @Test
        fun `should return 401 when user is not authenticated`() {
            // When & Then
            mockMvc.get("/api/v1/users/all") {
                accept = MediaType.APPLICATION_JSON
            }
                .andExpect {
                    status { isUnauthorized() }
                    content { jsonPath("$.error", `is`("Unauthorized")) }
                    content { jsonPath("$.message", `is`("Authentication required")) }
                }
        }
    }
}

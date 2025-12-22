package com.aivanouski.auth_kotlin_demo.application.dto.response

import com.aivanouski.auth_kotlin_demo.domain.entity.Role
import com.aivanouski.auth_kotlin_demo.domain.entity.User

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role,
    val emailVerified: Boolean
) {
    companion object {
        fun fromEntity(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email!!,
                firstName = user.firstName!!,
                lastName = user.lastName!!,
                role = user.role,
                emailVerified = user.emailVerified
            )
        }
    }
}

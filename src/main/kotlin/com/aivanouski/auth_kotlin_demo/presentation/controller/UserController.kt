package com.aivanouski.auth_kotlin_demo.presentation.controller

import com.aivanouski.auth_kotlin_demo.application.dto.response.UserResponse
import com.aivanouski.auth_kotlin_demo.infrastructure.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRepository: UserRepository
) {

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        val user = userRepository.findByEmail(authentication.name)
            .orElseThrow { RuntimeException("User not found") }
        return ResponseEntity.ok(UserResponse.fromEntity(user))
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userRepository.findAll()
        val userResponses = users.map { UserResponse.fromEntity(it) }
        return ResponseEntity.ok(userResponses)
    }
}

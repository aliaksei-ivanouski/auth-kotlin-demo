package com.aivanouski.auth_kotlin_demo.infrastructure.repository

import com.aivanouski.auth_kotlin_demo.domain.entity.EmailVerificationToken
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): Optional<EmailVerificationToken>
    fun findByUser(user: User): Optional<EmailVerificationToken>
    fun deleteByUser(user: User): Int
}
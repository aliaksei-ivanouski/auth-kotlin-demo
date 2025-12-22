package com.aivanouski.auth_kotlin_demo.infrastructure.repository

import com.aivanouski.auth_kotlin_demo.domain.entity.PasswordResetToken
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>
    fun findByUser(user: User): Optional<PasswordResetToken>
    fun deleteByUser(user: User): Int
}
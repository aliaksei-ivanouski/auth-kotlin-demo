package com.aivanouski.auth_kotlin_demo.infrastructure.repository

import com.aivanouski.auth_kotlin_demo.domain.entity.RefreshToken
import com.aivanouski.auth_kotlin_demo.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    fun revokeAllByUser(user: User): Int

    fun deleteByUser(user: User): Int
}
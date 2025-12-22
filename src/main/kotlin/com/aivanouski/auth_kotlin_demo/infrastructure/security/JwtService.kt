package com.aivanouski.auth_kotlin_demo.infrastructure.security

import com.aivanouski.auth_kotlin_demo.domain.entity.User
import com.aivanouski.auth_kotlin_demo.infrastructure.config.SecurityProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val securityProperties: SecurityProperties
) {

    fun generateAccessToken(user: User): String {
        return generateToken(user, securityProperties.jwt.accessTokenExpiration)
    }

    fun generateRefreshToken(user: User): String {
        return generateToken(user, securityProperties.jwt.refreshTokenExpiration)
    }

    private fun generateToken(user: User, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(user.email)
            .claim("userId", user.id)
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(securityProperties.jwt.secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
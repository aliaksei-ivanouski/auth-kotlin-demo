package com.aivanouski.auth_kotlin_demo.application.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.from}") private val fromEmail: String
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendVerificationEmail(to: String, firstName: String, verificationUrl: String) {
        val subject = "Verify Your Email Address"
        val text = buildVerificationEmailBody(firstName, verificationUrl)
        sendEmail(to, subject, text)
    }

    fun sendPasswordResetEmail(to: String, firstName: String, resetUrl: String) {
        val subject = "Reset Your Password"
        val text = buildPasswordResetEmailBody(firstName, resetUrl)
        sendEmail(to, subject, text)
    }

    private fun sendEmail(to: String, subject: String, text: String) {
        try {
            val message = SimpleMailMessage().apply {
                from = fromEmail
                setTo(to)
                setSubject(subject)
                setText(text)
            }
            mailSender.send(message)
            logger.info("Email sent successfully to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email to: $to", e)
            throw RuntimeException("Failed to send email", e)
        }
    }

    private fun buildVerificationEmailBody(firstName: String, verificationUrl: String): String {
        return """
            Hello $firstName,

            Thank you for registering with us!

            Please verify your email address by clicking the link below:
            $verificationUrl

            This link will expire in 24 hours.

            If you did not create an account, please ignore this email.

            Best regards,
            Auth Demo Team
        """.trimIndent()
    }

    private fun buildPasswordResetEmailBody(firstName: String, resetUrl: String): String {
        return """
            Hello $firstName,

            We received a request to reset your password.

            Please click the link below to reset your password:
            $resetUrl

            This link will expire in 1 hour.

            If you did not request a password reset, please ignore this email.

            Best regards,
            Auth Demo Team
        """.trimIndent()
    }
}

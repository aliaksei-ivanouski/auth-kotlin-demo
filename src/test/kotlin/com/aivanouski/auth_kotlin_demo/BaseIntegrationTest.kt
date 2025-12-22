package com.aivanouski.auth_kotlin_demo

import com.aivanouski.auth_kotlin_demo.application.dto.response.AuthResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.ObjectMapper

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = ["classpath:sql/run_before_tests.sql"]
)
@Sql(
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    scripts = ["classpath:sql/run_after_tests.sql"]
)
abstract class BaseIntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    companion object {
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)

        private val mailhog = GenericContainer(DockerImageName.parse("mailhog/mailhog:v1.0.1"))
            .withExposedPorts(1025, 8025)
            .withReuse(true)

        init {
            postgres.start()
            mailhog.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            // Configure PostgreSQL datasource
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)

            // Configure MailHog for tests
            registry.add("spring.mail.host") { mailhog.host }
            registry.add("spring.mail.port") { mailhog.getMappedPort(1025) }
            registry.add("spring.mail.username") { "" }
            registry.add("spring.mail.password") { "" }
            registry.add("spring.mail.properties.mail.smtp.auth") { "false" }
            registry.add("spring.mail.properties.mail.smtp.starttls.enable") { "false" }
        }

        fun getMailhogApiUrl(): String {
            return "http://${mailhog.host}:${mailhog.getMappedPort(8025)}"
        }
    }

    final fun authToken(prefix: String): String {
        val response = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "email": "$prefix@test.com",
                    "password": "letmein123456"
                }
            """.trimIndent()
        }.andReturn()

        val auth = objectMapper.readValue(
            response.response.contentAsByteArray,
            AuthResponse::class.java
        )
        return auth.accessToken
    }

    final fun refreshToken(prefix: String): String {
        val response = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "email": "$prefix@test.com",
                    "password": "letmein123456"
                }
            """.trimIndent()
        }.andReturn()

        val auth = objectMapper.readValue(
            response.response.contentAsByteArray,
            AuthResponse::class.java
        )
        return auth.refreshToken
    }
}

package com.subtracker

import com.subtracker.dto.LoginRequest
import com.subtracker.dto.RegisterRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class AuthControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should register user successfully`() {
        val registerRequest = RegisterRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.post("/api/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${registerRequest.username}",
                    "email": "${registerRequest.email}",
                    "password": "${registerRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }
    }

    @Test
    fun `should login user successfully`() {
        // First register a user
        val registerRequest = RegisterRequest(
            username = "testuser2",
            email = "test2@example.com",
            password = "password123"
        )

        mockMvc.post("/api/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${registerRequest.username}",
                    "email": "${registerRequest.email}",
                    "password": "${registerRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }

        // Then login
        val loginRequest = LoginRequest(
            username = "testuser2",
            password = "password123"
        )

        mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${loginRequest.username}",
                    "password": "${loginRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }
    }

    @Test
    fun `should fail login with invalid credentials`() {
        val loginRequest = LoginRequest(
            username = "nonexistent",
            password = "wrongpassword"
        )

        mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${loginRequest.username}",
                    "password": "${loginRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isUnauthorized
        }
    }
}
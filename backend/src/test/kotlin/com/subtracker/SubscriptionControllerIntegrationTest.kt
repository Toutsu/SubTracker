package com.subtracker

import com.subtracker.dto.RegisterRequest
import com.subtracker.dto.LoginRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should create subscription successfully`() {
        // First register and login a user
        val registerRequest = RegisterRequest(
            username = "subuser",
            email = "sub@example.com",
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

        // Login to get token
        val loginRequest = LoginRequest(
            username = "subuser",
            password = "password123"
        )

        val loginResult = mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${loginRequest.username}",
                    "password": "${loginRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }.andReturn()

        val token = loginResult.response.contentAsString
            .substringAfter("\"token\":\"")
            .substringBefore("\"")

        // Create subscription
        mockMvc.post("/api/subscriptions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "userId": "1",
                    "name": "Netflix",
                    "price": "15.99",
                    "currency": "USD",
                    "billingCycle": "MONTHLY",
                    "nextPaymentDate": "2023-12-01"
                }
            """.trimIndent()
            header("Authorization", "Bearer $token")
        }.andExpect {
            status().isCreated
        }
    }

    @Test
    fun `should get all subscriptions`() {
        // First register and login a user
        val registerRequest = RegisterRequest(
            username = "subuser2",
            email = "sub2@example.com",
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

        // Login to get token
        val loginRequest = LoginRequest(
            username = "subuser2",
            password = "password123"
        )

        val loginResult = mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${loginRequest.username}",
                    "password": "${loginRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }.andReturn()

        val token = loginResult.response.contentAsString
            .substringAfter("\"token\":\"")
            .substringBefore("\"")

        // Get all subscriptions
        mockMvc.get("/api/subscriptions") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status().isOk
        }
    }

    @Test
    fun `should delete subscription`() {
        // First register and login a user
        val registerRequest = RegisterRequest(
            username = "subuser3",
            email = "sub3@example.com",
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

        // Login to get token
        val loginRequest = LoginRequest(
            username = "subuser3",
            password = "password123"
        )

        val loginResult = mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "${loginRequest.username}",
                    "password": "${loginRequest.password}"
                }
            """.trimIndent()
        }.andExpect {
            status().isOk
        }.andReturn()

        val token = loginResult.response.contentAsString
            .substringAfter("\"token\":\"")
            .substringBefore("\"")

        // Create subscription first
        val createResult = mockMvc.post("/api/subscriptions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "userId": "1",
                    "name": "Spotify",
                    "price": "9.99",
                    "currency": "USD",
                    "billingCycle": "MONTHLY",
                    "nextPaymentDate": "2023-12-01"
                }
            """.trimIndent()
            header("Authorization", "Bearer $token")
        }.andExpect {
            status().isCreated
        }.andReturn()

        // Extract subscription ID from response
        val subscriptionId = createResult.response.contentAsString
            .substringAfter("\"id\":\"")
            .substringBefore("\"")

        // Delete subscription
        mockMvc.delete("/api/subscriptions/$subscriptionId") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status().isNoContent
        }
    }
}
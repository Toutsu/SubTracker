package com.subtracker.controller

import com.subtracker.dto.AuthResponse
import com.subtracker.dto.LoginRequest
import com.subtracker.dto.RegisterRequest
import com.subtracker.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.slf4j.LoggerFactory

@RestController
class AuthController(
    private val userService: UserService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info("Received registration request for username: ${request.username}")
        try {
            val user = userService.registerUser(request.username, request.email, request.password)
            
            return if (user != null) {
                ResponseEntity.ok(
                    AuthResponse(
                        success = true,
                        message = "Пользователь успешно зарегистрирован"
                    )
                )
            } else {
                ResponseEntity.badRequest().body(
                    AuthResponse(
                        success = false,
                        message = "Пользователь с таким именем или email уже существует"
                    )
                )
            }
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(
                AuthResponse(
                    success = false,
                    message = "Ошибка регистрации: ${e.message}"
                )
            )
        }
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        try {
            val user = userService.authenticateUser(request.username, request.password)
            
            return if (user != null) {
                val token = userService.generateToken(user)
                ResponseEntity.ok(
                    AuthResponse(
                        success = true,
                        message = "Вход выполнен успешно",
                        token = token
                    )
                )
            } else {
                ResponseEntity.status(401).body(
                    AuthResponse(
                        success = false,
                        message = "Неверное имя пользователя или пароль"
                    )
                )
            }
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(
                AuthResponse(
                    success = false,
                    message = "Ошибка входа: ${e.message}"
                )
            )
        }
    }
}
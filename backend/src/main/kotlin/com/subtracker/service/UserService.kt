package com.subtracker.service

import com.subtracker.model.User
import com.subtracker.repository.UserRepository
import com.subtracker.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    fun registerUser(username: String, email: String, password: String): User? {
        // Check if user already exists
        val existingUser = userRepository.findByUsernameOrEmail(username, email)
        if (existingUser.isPresent) {
            return null
        }
        
        // Create new user
        val user = User(
            username = username,
            email = email,
            passwordHash = passwordEncoder.encode(password),
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )
        
        return userRepository.save(user)
    }
    
    fun authenticateUser(username: String, password: String): User? {
        val user = userRepository.findByUsername(username).orElse(null)
            ?: return null
            
        return if (passwordEncoder.matches(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }
    
    fun generateToken(user: User): String {
        return jwtTokenProvider.createToken(user.username, user.id)
    }
}
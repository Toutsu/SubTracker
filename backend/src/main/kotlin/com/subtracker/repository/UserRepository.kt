package com.subtracker.repository

import com.subtracker.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, String> {
    
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    fun findByUsernameOrEmail(@Param("username") username: String, @Param("email") email: String): Optional<User>
    
    fun findByUsername(username: String): Optional<User>
    
    fun findByEmail(email: String): Optional<User>
}
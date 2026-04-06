package com.ovulation.health.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class UserRole { ADMIN, SUBJECT }

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,                        // UUID
    val email: String,
    val passwordHash: String,              // bcrypt / SHA-256 hash
    val role: UserRole,
    val displayName: String,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null,
    val isActive: Boolean = true,

    // SUBJECT-specific fields
    val cycleLength: Int = 28,
    val lastPeriodStart: LocalDateTime? = null,
    val assignedAdminId: String? = null,   // which admin manages this subject

    // ADMIN-specific fields
    val adminCode: String? = null          // invitation code for admin registration
)

@Entity(tableName = "auth_sessions")
data class AuthSession(
    @PrimaryKey
    val sessionToken: String,
    val userId: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isValid: Boolean = true
)

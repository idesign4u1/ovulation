package com.ovulation.health.data.db

import androidx.room.*
import com.ovulation.health.data.model.AuthSession
import com.ovulation.health.data.model.User
import com.ovulation.health.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE role = 'SUBJECT' AND assignedAdminId = :adminId")
    fun getSubjectsForAdmin(adminId: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    fun getAllAdmins(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isActive = 1 AND role = :role")
    fun getActiveByRole(role: UserRole): Flow<List<User>>

    @Query("UPDATE users SET lastLoginAt = :time WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, time: LocalDateTime)

    @Query("UPDATE users SET isActive = :active WHERE id = :userId")
    suspend fun setActive(userId: String, active: Boolean)
}

@Dao
interface AuthSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AuthSession)

    @Query("SELECT * FROM auth_sessions WHERE sessionToken = :token AND isValid = 1 LIMIT 1")
    suspend fun getValidSession(token: String): AuthSession?

    @Query("UPDATE auth_sessions SET isValid = 0 WHERE userId = :userId")
    suspend fun invalidateAllForUser(userId: String)

    @Query("DELETE FROM auth_sessions WHERE expiresAt < :now")
    suspend fun deleteExpired(now: LocalDateTime)
}

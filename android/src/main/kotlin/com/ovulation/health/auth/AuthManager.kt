package com.ovulation.health.auth

import android.content.Context
import android.content.SharedPreferences
import com.ovulation.health.data.db.OvulationDatabase
import com.ovulation.health.data.model.AuthSession
import com.ovulation.health.data.model.User
import com.ovulation.health.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

/**
 * Single source of truth for the currently logged-in user.
 * Persists the session token in SharedPreferences so the user
 * stays logged in across app restarts.
 */
class AuthManager(
    private val context: Context,
    private val database: OvulationDatabase
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val isLoggedIn get() = _currentUser.value != null
    val isAdmin    get() = _currentUser.value?.role == UserRole.ADMIN
    val isSubject  get() = _currentUser.value?.role == UserRole.SUBJECT

    // -------------------------------------------------------------------------
    // Session restore
    // -------------------------------------------------------------------------

    /** Call once at app start (from OvulationHealthApp.onCreate). */
    suspend fun restoreSession() {
        val token = prefs.getString(PREF_SESSION_TOKEN, null) ?: return
        val session = database.authSessionDao().getValidSession(token) ?: run {
            clearLocalSession()
            return
        }
        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            clearLocalSession()
            return
        }
        val user = database.userDao().getById(session.userId) ?: return
        _currentUser.value = user
        database.userDao().updateLastLogin(user.id, LocalDateTime.now())
        Timber.d("Session restored for ${user.email} (${user.role})")
    }

    // -------------------------------------------------------------------------
    // Login / Logout
    // -------------------------------------------------------------------------

    suspend fun login(email: String, password: String): AuthResult {
        val user = database.userDao().getByEmail(email.trim().lowercase())
            ?: return AuthResult.Error("Email not found")

        if (!user.isActive)
            return AuthResult.Error("Account is deactivated")

        if (user.passwordHash != hashPassword(password))
            return AuthResult.Error("Incorrect password")

        val session = createSession(user)
        database.authSessionDao().insert(session)
        prefs.edit().putString(PREF_SESSION_TOKEN, session.sessionToken).apply()

        _currentUser.value = user
        database.userDao().updateLastLogin(user.id, LocalDateTime.now())

        Timber.d("Login successful: ${user.email} (${user.role})")
        return AuthResult.Success(user)
    }

    suspend fun logout() {
        val token = prefs.getString(PREF_SESSION_TOKEN, null)
        if (token != null) {
            database.authSessionDao().invalidateAllForUser(_currentUser.value?.id ?: "")
        }
        clearLocalSession()
        _currentUser.value = null
        Timber.d("Logged out")
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /** Register a new SUBJECT (requires no special code). */
    suspend fun registerSubject(
        email: String,
        password: String,
        displayName: String,
        cycleLength: Int = 28,
        assignedAdminId: String
    ): AuthResult {
        if (database.userDao().getByEmail(email.trim().lowercase()) != null)
            return AuthResult.Error("Email already registered")

        val user = User(
            id             = UUID.randomUUID().toString(),
            email          = email.trim().lowercase(),
            passwordHash   = hashPassword(password),
            role           = UserRole.SUBJECT,
            displayName    = displayName,
            createdAt      = LocalDateTime.now(),
            cycleLength    = cycleLength,
            assignedAdminId = assignedAdminId
        )
        database.userDao().insert(user)
        Timber.d("Subject registered: ${user.email}")
        return login(email, password)
    }

    /** Register a new ADMIN (requires a secret admin code). */
    suspend fun registerAdmin(
        email: String,
        password: String,
        displayName: String,
        adminCode: String
    ): AuthResult {
        if (adminCode != ADMIN_REGISTRATION_CODE)
            return AuthResult.Error("Invalid admin registration code")

        if (database.userDao().getByEmail(email.trim().lowercase()) != null)
            return AuthResult.Error("Email already registered")

        val user = User(
            id           = UUID.randomUUID().toString(),
            email        = email.trim().lowercase(),
            passwordHash = hashPassword(password),
            role         = UserRole.ADMIN,
            displayName  = displayName,
            createdAt    = LocalDateTime.now(),
            adminCode    = adminCode
        )
        database.userDao().insert(user)
        Timber.d("Admin registered: ${user.email}")
        return login(email, password)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun createSession(user: User) = AuthSession(
        sessionToken = UUID.randomUUID().toString(),
        userId       = user.id,
        createdAt    = LocalDateTime.now(),
        expiresAt    = LocalDateTime.now().plusDays(SESSION_DAYS)
    )

    private fun clearLocalSession() {
        prefs.edit().remove(PREF_SESSION_TOKEN).apply()
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes  = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREF_SESSION_TOKEN   = "session_token"
        private const val SESSION_DAYS         = 30L
        // In production: store this server-side and deliver via a secure channel
        const val ADMIN_REGISTRATION_CODE = "OVU-RESEARCH-2026"
    }

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}

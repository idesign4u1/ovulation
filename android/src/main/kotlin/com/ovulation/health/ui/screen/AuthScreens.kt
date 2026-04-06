package com.ovulation.health.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.auth.AuthManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRegister by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    if (showRegister) {
        RegisterScreen(authManager, onLoginSuccess) { showRegister = false }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MonitorHeart,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text("Ovulation Health", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Research Platform", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton({ showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill all fields"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    val result = authManager.login(email.trim(), password)
                    isLoading = false
                    when (result) {
                        is AuthManager.AuthResult.Success ->
                            onLoginSuccess(result.user.role == com.ovulation.health.data.model.UserRole.ADMIN)
                        is AuthManager.AuthResult.Error ->
                            errorMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White)
            else Text("Login", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { showRegister = true }) {
            Text("New user? Register here")
        }
    }
}

@Composable
fun RegisterScreen(
    authManager: AuthManager,
    onSuccess: (isAdmin: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var adminCode by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("28") }
    var adminId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Register", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        // Role toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !isAdmin,
                onClick = { isAdmin = false },
                label = { Text("Subject / Participant") },
                leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(16.dp)) }
            )
            Spacer(Modifier.width(12.dp))
            FilterChip(
                selected = isAdmin,
                onClick = { isAdmin = true },
                label = { Text("Admin / Researcher") },
                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(16.dp)) }
            )
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it },
            label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))

        if (!isAdmin) {
            OutlinedTextField(value = cycleLength, onValueChange = { cycleLength = it },
                label = { Text("Avg. cycle length (days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = adminId, onValueChange = { adminId = it },
                label = { Text("Assigned researcher ID") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        } else {
            OutlinedTextField(value = adminCode, onValueChange = { adminCode = it },
                label = { Text("Admin registration code") },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (password != confirmPassword) { errorMessage = "Passwords do not match"; return@Button }
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill all required fields"; return@Button
                }
                isLoading = true
                scope.launch {
                    val result = if (isAdmin)
                        authManager.registerAdmin(email, password, name, adminCode)
                    else
                        authManager.registerSubject(email, password, name,
                            cycleLength.toIntOrNull() ?: 28, adminId.trim())
                    isLoading = false
                    when (result) {
                        is AuthManager.AuthResult.Success ->
                            onSuccess(result.user.role == com.ovulation.health.data.model.UserRole.ADMIN)
                        is AuthManager.AuthResult.Error -> errorMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White)
            else Text("Create account")
        }
    }
}

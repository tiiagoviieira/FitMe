package com.example.fitme.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitme.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: AuthViewModel, onSaveComplete: () -> Unit, onCancel: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registo") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ElevatedButton(onClick = onCancel, shape = RoundedCornerShape(50)) { Text("Cancelar") }
                ElevatedButton(
                    onClick = {
                        viewModel.register(User(username = username, emailOrPhone = emailOrPhone, passwordHash = password)) { onSaveComplete() }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text("Aplicar") }
            }

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = emailOrPhone, onValueChange = { emailOrPhone = it }, label = { Text("Email ou Telemóvel") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        }
    }
}
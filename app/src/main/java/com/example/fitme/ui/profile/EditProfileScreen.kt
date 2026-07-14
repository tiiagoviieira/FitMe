package com.example.fitme.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fitme.model.User
import com.example.fitme.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    currentUser: User,
    onSaveComplete: () -> Unit,
    onCancel: () -> Unit
) {
    // Como a conta já existe, carregamos os dados logo de início
    var username by remember { mutableStateOf(currentUser.username) }
    var email by remember { mutableStateOf(currentUser.email) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var password by remember { mutableStateOf(currentUser.passwordHash) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Perfil") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ElevatedButton(onClick = onCancel, shape = RoundedCornerShape(50)) { Text("Cancelar") }

                ElevatedButton(
                    onClick = {
                        // Mantemos o ID atual para garantir que substituímos na BD e não criamos um novo
                        val updatedUser = currentUser.copy(
                            username = username,
                            email = email,
                            phone = phone,
                            passwordHash = password
                        )
                        viewModel.register(updatedUser) { onSaveComplete() }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Guardar")
                }
            }

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Número de Telemóvel") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        }
    }
}
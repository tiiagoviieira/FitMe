package com.example.fitme.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val activeUsers by viewModel.activeUsers.collectAsState(initial = emptyList())
    var showOptions by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    // ==========================================
    // ECRÃ PRINCIPAL
    // ==========================================
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Título Gigante com fonte diferente
        Text(
            text = "FIT ME",
            fontSize = 72.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!showOptions) {
            Button(
                onClick = { showOptions = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Entrar", fontSize = 18.sp)
            }
        }

        AnimatedVisibility(visible = showOptions) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalButton(
                    onClick = { showLoginDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Entrar com Credenciais")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.loginAsGuest()
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Continuar como Convidado")
                }

                TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Criar Conta")
                }
            }
        }
    }

    // JANELA FLUTUANTE DE LOGIN
    if (showLoginDialog) {
        var inputUsername by remember { mutableStateOf("") }
        var inputPassword by remember { mutableStateOf("") }

        Dialog(onDismissRequest = {
            showLoginDialog = false
            loginError = false
        }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Iniciar Sessão",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Divider()


                    // LISTA DE CONTAS COM SESSÃO ATIVA

                    if (activeUsers.isNotEmpty()) {
                        Text("Contas guardadas:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                        LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                            items(activeUsers) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            // Ao clicar, faz login automático sem password!
                                            viewModel.switchUser(user.id)
                                            showLoginDialog = false
                                            onLoginSuccess()
                                        }
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(user.username, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Ou entra com outra conta:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    // ==========================================

                    if (loginError) {
                        Text(
                            text = "Credenciais incorretas. Tenta novamente.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = {
                            inputUsername = it
                            loginError = false
                        },
                        label = { Text("Username / Email / Telemóvel") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = {
                            inputPassword = it
                            loginError = false
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                showLoginDialog = false
                                loginError = false
                            }
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                viewModel.login(
                                    username = inputUsername,
                                    pass = inputPassword,
                                    onSuccess = {
                                        showLoginDialog = false
                                        onLoginSuccess()
                                    },
                                    onError = {
                                        loginError = true
                                    }
                                )
                            },
                            enabled = inputUsername.isNotBlank() && inputPassword.isNotBlank()
                        ) {
                            Text("Entrar")
                        }
                    }
                }
            }
        }
    }
}
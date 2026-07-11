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

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel, // Recebemos o ViewModel aqui para tratar do login
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) } // Para mostrar erro se a pass estiver errada

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

    // ==========================================
    // JANELA FLUTUANTE DE LOGIN
    // ==========================================
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
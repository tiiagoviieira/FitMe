package com.example.fitme.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitme.model.Outfit
import com.example.fitme.ui.auth.AuthViewModel
import com.example.fitme.model.User
import androidx.compose.ui.window.Dialog
import com.example.fitme.ui.theme.ClothingViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    socialViewModel: SocialViewModel,
    clothingViewModel: ClothingViewModel,
    username: String,
    outfits: List<Outfit>,
    allUsers: List<User>,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(emptySet<Outfit>()) }
    var showSwitchAccountDialog by remember { mutableStateOf(false) }
    var showFriendRequestsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedItems.size} selecionados") },
                    navigationIcon = {
                        IconButton(onClick = { isSelectionMode = false; selectedItems = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        // NOVO: Botão de Eliminar
                        IconButton(onClick = {
                            authViewModel.deleteOutfits(selectedItems.toList())
                            isSelectionMode = false
                            selectedItems = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }

                        // Botões que já tinhas
                        IconButton(onClick = {
                            authViewModel.toggleOutfitsVisibility(selectedItems.toList(), makePublic = true)
                            isSelectionMode = false; selectedItems = emptySet()
                        }) { Icon(Icons.Default.Visibility, contentDescription = "Público") }

                        IconButton(onClick = {
                            authViewModel.toggleOutfitsVisibility(selectedItems.toList(), makePublic = false)
                            isSelectionMode = false; selectedItems = emptySet()
                        }) { Icon(Icons.Default.VisibilityOff, contentDescription = "Privado") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                TopAppBar(
                    title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showFriendRequestsDialog = true }) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Pedidos de Amizade")
                        }
                        IconButton(onClick = { onNavigateToChat() }) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Mensagens")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isMenuExpanded) {
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; onNavigateHome() }) { Icon(Icons.Default.Home, "Início") }
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; onLogout() }) { Icon(Icons.Default.Logout, "Logout") }
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; showSwitchAccountDialog = true }) { Icon(Icons.Default.SwapHoriz, "Switch Account") }
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; onEditProfile() }) { Icon(Icons.Default.Edit, "Edit Profile") }
                    }
                    FloatingActionButton(onClick = { isMenuExpanded = !isMenuExpanded }, shape = CircleShape) {
                        Icon(if (isMenuExpanded) Icons.Default.Close else Icons.Default.MoreVert, "Menu")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Zona Superior: Foto de Perfil e Nome
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }

            Divider()

            // Zona Inferior: Grelha de Outfits
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(outfits) { outfit ->
                    val isSelected = selectedItems.contains(outfit)
                    Box(
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (isSelected) selectedItems - outfit else selectedItems + outfit
                                        if (selectedItems.isEmpty()) isSelectionMode = false
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) { isSelectionMode = true; selectedItems = setOf(outfit) }
                                }
                            )
                    ) {
                        AsyncImage(model = outfit.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

                        // Icon de Privacidade
                        Icon(
                            imageVector = if (outfit.isPublic) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(16.dp)
                        )

                        if (isSelected) {
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                        }
                    }
                }


            }
        }
    }


    // JANELA: PEDIDOS DE AMIZADE

    if (showFriendRequestsDialog) {
        Dialog(onDismissRequest = { showFriendRequestsDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f), shape = RoundedCornerShape(28.dp)) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                    Text("Pedidos de Amizade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Adicionar por número
                    var phoneInput by remember { mutableStateOf("") }
                    var feedbackMsg by remember { mutableStateOf("") }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Nº Telemóvel") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            socialViewModel.sendFriendRequestByPhone(authViewModel.currentUserId.value ?: "", phoneInput) { success, msg ->
                                feedbackMsg = msg
                                if(success) phoneInput = ""
                            }
                        }) { Icon(Icons.Default.PersonAdd, contentDescription = "Adicionar") }
                    }
                    if (feedbackMsg.isNotBlank()) Text(feedbackMsg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pendentes:", fontWeight = FontWeight.Bold)

                    // NOVO: Lê os pedidos pendentes da Base de Dados
                    val pendingRequests by socialViewModel.getPendingRequests(authViewModel.currentUserId.value ?: "").collectAsState(initial = emptyList())

                    if (pendingRequests.isEmpty()) {
                        Text("Não tens pedidos pendentes.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f, fill = false).padding(top = 8.dp)) {
                            items(pendingRequests) { request ->
                                val sender = allUsers.find { it.id == request.senderId }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sender?.username ?: "Desconhecido", fontWeight = FontWeight.Medium)
                                    Row {
                                        IconButton(onClick = { socialViewModel.respondToRequest(request.id, true) }) {
                                            Icon(Icons.Default.Check, contentDescription = "Aceitar", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { socialViewModel.respondToRequest(request.id, false) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Recusar", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showFriendRequestsDialog = false }, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
                }
            }
        }
    }


    // JANELA: SWITCH ACCOUNT

    if (showSwitchAccountDialog) {
        val currentUserId = authViewModel.currentUserId.collectAsState().value

        Dialog(onDismissRequest = { showSwitchAccountDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Trocar de Conta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    if (allUsers.isEmpty()) {
                        Text("Não existem outras contas registadas no dispositivo.")
                    } else {
                        // Usamos LazyColumn caso existam muitas contas
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(allUsers) { user ->
                                val isCurrentUser = user.id == currentUserId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            // Se não for a conta atual, faz a troca
                                            if (!isCurrentUser) {
                                                authViewModel.switchUser(user.id)
                                            }
                                            // Fecha sempre a janela independentemente da escolha
                                            showSwitchAccountDialog = false
                                        }
                                        .background(if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ícone de Perfil
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Nome da Conta
                                    Text(
                                        text = if (isCurrentUser) "${user.username} (Atual)" else user.username,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showSwitchAccountDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
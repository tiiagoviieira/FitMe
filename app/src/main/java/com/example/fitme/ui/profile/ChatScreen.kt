package com.example.fitme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.fitme.model.ClothingItem
import com.example.fitme.model.User
import com.example.fitme.ui.auth.AuthViewModel
import com.example.fitme.ui.theme.ClothingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    authViewModel: AuthViewModel,
    socialViewModel: SocialViewModel,
    clothingViewModel: ClothingViewModel,
    allUsers: List<User>,
    onNavigateBack: () -> Unit
) {
    var activeChatUserId by remember { mutableStateOf<String?>(null) }
    val currentUserId = authViewModel.currentUserId.collectAsState().value ?: ""
    val allClothingItems by clothingViewModel.allClothing.collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (activeChatUserId == null) {
                        Text("Mensagens", fontWeight = FontWeight.Bold)
                    } else {
                        val activeUser = allUsers.find { it.id == activeChatUserId }
                        Text(activeUser?.username ?: "Chat", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeChatUserId != null) activeChatUserId = null
                        else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            val friendsRequests by socialViewModel.getFriends(currentUserId).collectAsState(initial = emptyList())
            val friendIds = friendsRequests.map { if (it.senderId == currentUserId) it.receiverId else it.senderId }
            val friendUsers = allUsers.filter { it.id in friendIds }


            // CAIXA DE ENTRADA (Lista de Amigos)
            if (activeChatUserId == null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (friendUsers.isEmpty()) {
                        item {
                            Text(
                                text = "Ainda não tens amigos. Adiciona-os no teu perfil!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(friendUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeChatUserId = user.id }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
            // CONVERSA ATIVA
            else {
                var chatMsg by remember { mutableStateOf("") }
                var showAttachmentPicker by remember { mutableStateOf(false) }

                // Variáveis para as novas janelas de inspeção de roupa
                var selectedAttachments by remember { mutableStateOf(emptySet<ClothingItem>()) }
                var showDetailsItem by remember { mutableStateOf<ClothingItem?>(null) }
                var showFullScreenImageUri by remember { mutableStateOf<String?>(null) }

                val chatMessages by socialViewModel.getChatMessages(currentUserId, activeChatUserId!!).collectAsState(initial = emptyList())

                // ZONA DAS MENSAGENS (COM CARTÕES)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chatMessages.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("A tua conversa começa aqui...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        items(chatMessages) { msg ->
                            val isMe = msg.senderId == currentUserId
                            val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                            val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {

                                // Se for um Anexo (Cartão de Roupa)
                                if (msg.attachedClothingId != null) {
                                    val attachedItem = allClothingItems.find { it.id == msg.attachedClothingId }

                                    if (attachedItem != null) {
                                        Card(
                                            modifier = Modifier
                                                .width(220.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { showDetailsItem = attachedItem }, // Abre o pop-up
                                            colors = CardDefaults.cardColors(containerColor = bubbleColor)
                                        ) {
                                            Column {
                                                AsyncImage(
                                                    model = attachedItem.imageUri,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                                    contentScale = ContentScale.Crop
                                                )
                                                if (msg.text.isNotBlank()) {
                                                    Text(
                                                        text = msg.text,
                                                        color = textColor,
                                                        modifier = Modifier.padding(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text("Peça indisponível", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                                    }
                                }
                                // Se for apenas Texto normal
                                else {
                                    Text(
                                        text = msg.text,
                                        color = textColor,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bubbleColor)
                                            .padding(12.dp)
                                            .widthIn(max = 280.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ZONA DE INPUT (Botão Anexar e Enviar)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAttachmentPicker = true }) {
                            Icon(Icons.Default.Checkroom, contentDescription = "Anexar Roupa", tint = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = chatMsg,
                            onValueChange = { chatMsg = it },
                            placeholder = { Text("Escreve uma mensagem...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                if (chatMsg.isNotBlank()) {
                                    socialViewModel.sendMessage(currentUserId, activeChatUserId!!, chatMsg)
                                    chatMsg = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                // JANELA: GRELHA PARA ESCOLHER ANEXOS

                if (showAttachmentPicker) {
                    Dialog(onDismissRequest = { showAttachmentPicker = false; selectedAttachments = emptySet() }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Selecionar Peças", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(16.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(allClothingItems) { item ->
                                        val isSelected = selectedAttachments.contains(item)
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedAttachments = if (isSelected) selectedAttachments - item else selectedAttachments + item
                                                }
                                        ) {
                                            AsyncImage(
                                                model = item.imageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            if (isSelected) {
                                                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.Center))
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { showAttachmentPicker = false; selectedAttachments = emptySet() }) {
                                        Text("Cancelar")
                                    }
                                    Button(
                                        onClick = {
                                            // Envia uma mensagem por cada peça selecionada
                                            selectedAttachments.forEach { item ->
                                                socialViewModel.sendMessage(currentUserId, activeChatUserId!!, chatMsg.ifBlank { "Vê esta peça: ${item.name}" }, attachedClothingId = item.id)
                                            }
                                            chatMsg = ""
                                            showAttachmentPicker = false
                                            selectedAttachments = emptySet()
                                        },
                                        enabled = selectedAttachments.isNotEmpty()
                                    ) {
                                        Text("Enviar (${selectedAttachments.size})")
                                    }
                                }
                            }
                        }
                    }
                }

                // JANELA: DETALHES DA PEÇA NO CHAT
                if (showDetailsItem != null) {
                    val item = showDetailsItem!!
                    Dialog(onDismissRequest = { showDetailsItem = null }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                                // Clicar na imagem abre ecrã inteiro
                                AsyncImage(
                                    model = item.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showFullScreenImageUri = item.imageUri },
                                    contentScale = ContentScale.Crop
                                )

                                Text("Categoria: ${item.category}", fontWeight = FontWeight.SemiBold)
                                Text("Estações: ${item.weatherTag}")
                                if (!item.referenceLink.isNullOrBlank()) {
                                    Text("Link/Referência: ${item.referenceLink}", color = MaterialTheme.colorScheme.primary)
                                }

                                Button(
                                    onClick = { showDetailsItem = null },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Fechar")
                                }
                            }
                        }
                    }
                }

                // JANELA: IMAGEM EM ECRÃ INTEIRO
                if (showFullScreenImageUri != null) {
                    Dialog(
                        onDismissRequest = { showFullScreenImageUri = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false) // Permite ocupar o ecrã todo
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.95f))
                        ) {
                            AsyncImage(
                                model = showFullScreenImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            IconButton(
                                onClick = { showFullScreenImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example.fitme.ui.theme

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.fitme.model.ClothingItem
import com.example.fitme.utils.processOutfitMatching
import com.example.fitme.ui.auth.AuthViewModel
import com.example.fitme.model.Outfit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitOfTheDayScreen(viewModel: ClothingViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // Estados para o Diálogo Flutuante
    var showDialog by remember { mutableStateOf(false) }
    var rankedItems by remember { mutableStateOf<List<ClothingItem>>(emptyList()) }
    var selectedItems by remember { mutableStateOf<Set<ClothingItem>>(emptySet()) }
    var isProcessing by remember { mutableStateOf(false) }

    // O "Lançador" da Câmara
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedUri = tempUri
        }
    }

    // Lançador de Permissões
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outfit Of The Day", fontWeight = FontWeight.Bold) }, // Substitui o nome
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // BOTÕES DE TOPO
            // ==========================================
            if (capturedUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ElevatedButton(
                        onClick = { capturedUri = null },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancelar", modifier = Modifier.padding(horizontal = 8.dp))
                    }

                    ElevatedButton(
                        onClick = {
                            if (capturedUri != null) {
                                isProcessing = true // Mostra o utilizador que está a pensar
                                processOutfitMatching(context, capturedUri!!, viewModel, coroutineScope) { sortedList ->
                                    // Quando o algoritmo termina, recebemos a lista ordenada
                                    rankedItems = sortedList
                                    selectedItems = emptySet()
                                    isProcessing = false
                                    showDialog = true // Abre o ecrã flutuante
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 8.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Text("A Analisar...")
                        } else {
                            Text("Aplicar", modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            // ==========================================
            // ZONA DA FOTO
            // ==========================================
            Card(
                onClick = {
                    if (capturedUri == null) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (capturedUri != null) {
                        AsyncImage(
                            model = capturedUri.toString(),
                            contentDescription = "Foto do Outfit",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Tirar Foto", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Toque para fotografar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // ECRÃ FLUTUANTE DE SELEÇÃO (GRELHA)
    // ==========================================
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f), // Ocupa 70% da altura do ecrã
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                    Text("O que tens vestido?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grelha de Roupas
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 3 quadrados por linha
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rankedItems) { item ->
                            val isSelected = selectedItems.contains(item)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f) // Mantém os itens quadrados
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    }
                                    .border(
                                        width = if (isSelected) 4.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = item.imageUri,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Overlay escuro e ícone se estiver selecionado
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selecionado",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botões do Diálogo
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                // Atualiza a data das roupas
                                selectedItems.forEach { item ->
                                    viewModel.updateLastWornDate(item.id)
                                }

                                // GUARDA A FOTO DO OUTFIT NA BASE DE DADOS
                                val currentUserId = authViewModel.currentUserId.value ?: "guest"
                                authViewModel.saveOutfit(
                                    Outfit(
                                        userId = currentUserId,
                                        imageUri = capturedUri.toString(),
                                        isPublic = false // Podes alterar a privacidade depois no Perfil
                                    )
                                )

                                showDialog = false
                                capturedUri = null // Limpa o ecrã
                            }

                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}
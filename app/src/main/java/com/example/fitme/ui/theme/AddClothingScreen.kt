package com.example.fitme.ui.theme

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import com.example.fitme.model.ClothingItem
import androidx.compose.foundation.clickable
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothingScreen(viewModel: ClothingViewModel, itemId: String? = null, currentUserId: String, onSaveComplete: () -> Unit) {
    val context = LocalContext.current
    var existingItem by remember { mutableStateOf<ClothingItem?>(null) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedSeasons by remember { mutableStateOf(setOf<String>()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var referenceLink by remember { mutableStateOf("") }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(itemId) {
        if (itemId != null) {
            val item = viewModel.getClothingById(itemId)
            if (item != null) {
                existingItem = item
                name = item.name
                category = item.category
                selectedSeasons = item.weatherTag.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                imageUri = Uri.parse(item.imageUri)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        // Só atualiza a imagem no ecrã se a foto for tirada com sucesso!
        if (success) {
            imageUri = tempUri
        }
    }

    // Lançador de Permissões da Câmara
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            tempUri = uri // Guarda no temporário em vez do principal
            cameraLauncher.launch(uri)
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 24.dp, vertical = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ElevatedButton(onClick = { onSaveComplete() }, shape = RoundedCornerShape(50)) { Text("Cancelar") }
                ElevatedButton(
                    onClick = {
                        if (currentUserId.isBlank()) return@ElevatedButton
                        val newItem = existingItem?.copy(
                            name = name,
                            category = category,
                            weatherTag = selectedSeasons.joinToString(", "),
                            imageUri = imageUri.toString()
                        ) ?: ClothingItem(
                            id = UUID.randomUUID().toString(),
                            userId = currentUserId,
                            name = name,
                            category = category,
                            weatherTag = selectedSeasons.joinToString(", "),
                            imageUri = imageUri.toString()
                        )
                        viewModel.addClothing(newItem)
                        onSaveComplete()
                    },
                    shape = RoundedCornerShape(50),
                    enabled = imageUri != null && name.isNotBlank() && category.isNotBlank() && selectedSeasons.isNotEmpty()
                ) { Text("Aplicar") }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .clickable {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri.toString(),
                            contentDescription = "Foto da Peça",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Foto",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Descrição", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoria", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp))
            }

            OutlinedTextField(
                value = referenceLink,
                onValueChange = { referenceLink = it },
                label = { Text("Link ou Ref. da Peça (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Seçao Estacoes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estação", fontWeight = FontWeight.Bold)
                val seasons = listOf("Primavera", "Verão", "Outono", "Inverno")
                seasons.chunked(2).forEach { rowSeasons ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowSeasons.forEach { season ->
                            FilterChip(selected = selectedSeasons.contains(season), onClick = {
                                selectedSeasons = if (selectedSeasons.contains(season)) selectedSeasons - season else selectedSeasons + season
                            }, label = { Text(season) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


fun createImageUri(context: Context): Uri {
    val imagePath = File(context.filesDir, "my_images").apply { mkdirs() }
    val newFile = File(imagePath, "roupa_${System.currentTimeMillis()}.jpg")
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, newFile)
}
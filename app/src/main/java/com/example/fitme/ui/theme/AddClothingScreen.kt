package com.example.fitme.ui.theme

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import com.example.fitme.model.ClothingItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothingScreen(viewModel: ClothingViewModel, onSaveComplete: () -> Unit) {
    val context = LocalContext.current

    // Variáveis de estado
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var weatherTag by remember { mutableStateOf("") }

    // Variável para guardar o caminho da foto que a câmara vai tirar
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // O "Lançador" da Câmara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // A foto foi tirada com sucesso! imageUri já tem o caminho.
                // Num projeto real, podes mostrar um Toast ou feedback aqui.
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Peça", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
// Este lançador pede a permissão e, se for aceite, abre a câmara
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    val uri = createImageUri(context)
                    imageUri = uri
                    cameraLauncher.launch(uri)
                }
            }

            // Botão que abre a Câmara
            ElevatedButton(
                onClick = {
                    // Pedimos permissão de câmara ao sistema
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.size(140.dp).padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(if (imageUri != null) "✅ Foto Pronta" else "📷 Tirar Foto")
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Peça (ex: T-Shirt Branca)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoria (ex: Camisolas, Calças)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = weatherTag,
                onValueChange = { weatherTag = it },
                label = { Text("Clima ideal (ex: Calor, Frio, Chuva)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val newItem = ClothingItem(
                        name = name,
                        category = category,
                        weatherTag = weatherTag,
                        imageUri = imageUri.toString()
                    )
                    viewModel.addClothing(newItem)
                    onSaveComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = imageUri != null && name.isNotBlank() // Só deixa guardar se houver foto e nome!
            ) {
                Text("Guardar no Roupeiro", style = MaterialTheme.typography.titleMedium)
            }
        }


    }
}

// Função auxiliar para gerar um caminho seguro para a foto
fun createImageUri(context: Context): Uri {
    // Usamos filesDir em vez de cacheDir
    val imagePath = File(context.filesDir, "my_images").apply { mkdirs() }
    val newFile = File(imagePath, "roupa_${System.currentTimeMillis()}.jpg")
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, newFile)
}
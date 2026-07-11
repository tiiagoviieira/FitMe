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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    username: String,
    outfits: List<Outfit>,
    allUsers: List<User>,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit,
    onEditProfile: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(emptySet<Outfit>()) }
    var showSwitchAccountDialog by remember { mutableStateOf(false) }

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
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; onLogout() }) { Icon(Icons.Default.Logout, "Logout") }
                        SmallFloatingActionButton(onClick = { isMenuExpanded = false; onSwitchAccount() }) { Icon(Icons.Default.SwapHoriz, "Switch Account") }
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
}
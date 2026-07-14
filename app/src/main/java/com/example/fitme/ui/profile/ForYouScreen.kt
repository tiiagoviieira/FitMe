package com.example.fitme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitme.model.User
import com.example.fitme.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.Flow
import com.example.fitme.model.FriendRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouScreen(
    authViewModel: AuthViewModel,
    socialViewModel: SocialViewModel,
    allUsers: List<User>
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val allPublicOutfits by authViewModel.allPublicOutfits.collectAsState(initial = emptyList())

    // Colecionar a lista de pedidos de amizade aceites
    val friendsRequests by socialViewModel.getFriends(currentUserId ?: "").collectAsState(initial = emptyList())

    // Extrair os IDs dos amigos
    val friendIds = friendsRequests.map {
        if (it.senderId == currentUserId) it.receiverId else it.senderId
    }

    // Filtrar os outfits para mostrar APENAS os que pertencem aos amigos
    val feedOutfits = allPublicOutfits.filter { it.userId in friendIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Para Ti", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (feedOutfits.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ainda não há outfits dos teus amigos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(feedOutfits) { outfit ->
                    val creator = allUsers.find { it.id == outfit.userId }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            // Cabeçalho (Quem postou)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(creator?.username ?: "Desconhecido", fontWeight = FontWeight.Bold)
                            }

                            // Imagem do Outfit
                            AsyncImage(
                                model = outfit.imageUri,
                                contentDescription = "Outfit",
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
package com.example.fitme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.fitme.data.AppDatabase
import com.example.fitme.ui.auth.*
import com.example.fitme.ui.profile.*
import com.example.fitme.ui.theme.*
import com.example.fitme.ui.ai.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "fitme-db"
        ).fallbackToDestructiveMigration().build()

        setContent {
            FitMeTheme {
                val navController = rememberNavController()


                // ==========================================
                // 1. VIEWMODELS PARTILHADOS (Scope Global)
                // Criados aqui para que toda a aplicação partilhe a mesma memória e estado
                // ==========================================
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db.userDao(), db.outfitDao()))
                val clothingViewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                val currentUserId by authViewModel.currentUserId.collectAsState()

                LaunchedEffect(currentUserId) {
                    currentUserId?.let {
                        clothingViewModel.setUserId(it)
                    }
                }

                val aiViewModel: AiViewModel = viewModel(factory = AiViewModelFactory(db.clothingDao()))

                val currentUser by db.userDao().getUserFlow(currentUserId ?: "").collectAsState(initial = null)
                val userOutfits by db.outfitDao().getOutfitsByUser(currentUserId ?: "").collectAsState(initial = emptyList())
                val allUsers by authViewModel.allUsers.collectAsState(initial = emptyList())

                // Instancia o SocialViewModel
                val socialViewModel: SocialViewModel = viewModel(factory = SocialViewModelFactory(db.socialDao(), db.userDao()))
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Definimos as rotas onde a barra inferior deve aparecer
                val mainRoutes = listOf("inventory_screen", "outfit_day", "ai_stylist", "for_you", "profile_screen")
                val showBottomBar = mainRoutes.contains(currentRoute)

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "inventory_screen",
                                    onClick = { navController.navigate("inventory_screen") },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Roupeiro") },
                                    label = { Text("Roupeiro") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "outfit_day",
                                    onClick = { navController.navigate("outfit_day") },
                                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Outfit do Dia") },
                                    label = { Text("Outfit") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "ai_stylist",
                                    onClick = { navController.navigate("ai_stylist") },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Estilista") },
                                    label = { Text("Estilista") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "for_you",
                                    onClick = { navController.navigate("for_you") },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Para Ti") },
                                    label = { Text("Para Ti") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile_screen",
                                    onClick = { navController.navigate("profile_screen") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                    label = { Text("Perfil") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("welcome_screen") {
                            // Agora usamos o authViewModel global!
                            WelcomeScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = { navController.navigate("register_screen") },
                                onLoginSuccess = { navController.navigate("inventory_screen") { popUpTo(0) } }
                            )
                        }

                        composable("register_screen") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onSaveComplete = {
                                    // Após criar conta, vai para o inventário
                                    navController.navigate("inventory_screen") { popUpTo(0) }
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        composable("edit_profile_screen") {
                            val currentUserId by authViewModel.currentUserId.collectAsState()
                            val currentUser by db.userDao().getUserFlow(currentUserId ?: "").collectAsState(initial = null)

                            // Só renderiza o ecrã se o utilizador já tiver sido carregado da base de dados
                            currentUser?.let { user ->
                                EditProfileScreen(
                                    viewModel = authViewModel,
                                    currentUser = user,
                                    onSaveComplete = { navController.popBackStack() }, // Guarda e volta ao Perfil
                                    onCancel = { navController.popBackStack() }
                                )
                            }
                        }

                        composable("inventory_screen") {
                            InventoryScreen(
                                viewModel = clothingViewModel,
                                onNavigateToAdd = { navController.navigate("add_clothing_screen") },
                                onNavigateToEdit = { itemId -> navController.navigate("add_clothing_screen?itemId=$itemId") }
                            )
                        }

                        composable("outfit_day") {
                            OutfitOfTheDayScreen(viewModel = clothingViewModel, authViewModel = authViewModel)
                        }

                        composable("ai_stylist") {
                            AiStylistScreen(viewModel = aiViewModel)
                        }

                        composable("profile_screen") {
                            val currentUserId by authViewModel.currentUserId.collectAsState()
                            val currentUser by db.userDao().getUserFlow(currentUserId ?: "").collectAsState(initial = null)
                            val userOutfits by db.outfitDao().getOutfitsByUser(currentUserId ?: "").collectAsState(initial = emptyList())

                            // 1. Mudamos de allUsers para activeUsers
                            val activeUsers by authViewModel.activeUsers.collectAsState(initial = emptyList())

                            ProfileScreen(
                                authViewModel = authViewModel,
                                socialViewModel = socialViewModel,
                                clothingViewModel = clothingViewModel,
                                username = currentUser?.username ?: "Convidado",
                                outfits = userOutfits,
                                allUsers = activeUsers, // 2. Passamos a nova lista filtrada aqui!
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("welcome_screen") { popUpTo(0) }
                                },
                                onEditProfile = { navController.navigate("edit_profile_screen") },
                                onNavigateToChat = { navController.navigate("chat_screen") },
                                onNavigateHome = { navController.navigate("welcome_screen") }
                            )
                        }

                        composable("chat_screen") {
                            val allUsers by authViewModel.allUsers.collectAsState(initial = emptyList())

                            ChatScreen(
                                authViewModel = authViewModel,
                                socialViewModel = socialViewModel,
                                clothingViewModel = clothingViewModel,
                                allUsers = allUsers,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("for_you") {
                            val allUsers by authViewModel.allUsers.collectAsState(initial = emptyList())

                            ForYouScreen(
                                authViewModel = authViewModel,
                                socialViewModel = socialViewModel,
                                allUsers = allUsers
                            )
                        }

                        composable(
                            route = "add_clothing_screen?itemId={itemId}",
                            arguments = listOf(androidx.navigation.navArgument("itemId") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId")
                            AddClothingScreen(
                                viewModel = clothingViewModel,
                                itemId = itemId,
                                currentUserId = currentUserId ?: "",
                                onSaveComplete = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
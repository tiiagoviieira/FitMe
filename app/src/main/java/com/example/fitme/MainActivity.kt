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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.OutfitDao
import com.example.fitme.data.UserDao
import com.example.fitme.ui.auth.*
import com.example.fitme.ui.profile.*
import com.example.fitme.ui.theme.*

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
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Definimos as rotas onde a barra inferior deve aparecer
                val mainRoutes = listOf("inventory_screen", "outfit_day", "profile_screen")
                val showBottomBar = mainRoutes.contains(currentRoute)

                Scaffold(
                    bottomBar = {
                        // Usa a variável showBottomBar em vez da verificação manual antiga
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "inventory_screen",
                                    onClick = { navController.navigate("inventory_screen") },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Roupeiro") },
                                    label = { Text("Roupeiro") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "outfit_day",
                                    onClick = { navController.navigate("outfit_day") },
                                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Outfit do Dia") },
                                    label = { Text("Outfit") }
                                )
                                // A nova aba de Perfil
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
                        // ==========================================
                        // ROTAS DE AUTENTICAÇÃO
                        // ==========================================
                        composable("welcome_screen") {
                            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db.userDao(), db.outfitDao()))
                            WelcomeScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = { navController.navigate("register_screen") },
                                onLoginSuccess = { navController.navigate("inventory_screen") { popUpTo(0) } }
                            )
                        }

                        composable("register_screen") {
                            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db.userDao(), db.outfitDao()))
                            RegisterScreen(
                                viewModel = authViewModel,
                                onSaveComplete = { navController.navigate("inventory_screen") { popUpTo(0) } },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        // ==========================================
                        // ROTAS PRINCIPAIS
                        // ==========================================
                        composable("inventory_screen") {
                            val viewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                            InventoryScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { navController.navigate("add_clothing_screen") },
                                onNavigateToEdit = { itemId -> navController.navigate("add_clothing_screen?itemId=$itemId") }
                            )
                        }

                        composable("outfit_day") {
                            val viewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db.userDao(), db.outfitDao()))

                            // Agora passamos os dois ViewModels!
                            OutfitOfTheDayScreen(viewModel = viewModel, authViewModel = authViewModel)
                        }
                        composable("profile_screen") {
                            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db.userDao(), db.outfitDao()))
                            val currentUserId by authViewModel.currentUserId.collectAsState()

                            val currentUser by db.userDao().getUserFlow(currentUserId ?: "").collectAsState(initial = null)
                            val userOutfits by db.outfitDao().getOutfitsByUser(currentUserId ?: "").collectAsState(initial = emptyList())
                            val allUsers by authViewModel.allUsers.collectAsState(initial = emptyList())

                            ProfileScreen(
                                authViewModel = authViewModel,
                                username = currentUser?.username ?: "Convidado",
                                outfits = userOutfits,
                                allUsers = allUsers,
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("welcome_screen") { popUpTo(0) }
                                },
                                onSwitchAccount = { /* Esta função foi removida do ProfileScreen pois agora o diálogo está lá dentro */ },
                                onEditProfile = { navController.navigate("register_screen") }
                            )
                        }

                        // ==========================================
                        // ROTA DE ADICIONAR/EDITAR
                        // ==========================================
                        composable(
                            route = "add_clothing_screen?itemId={itemId}",
                            arguments = listOf(androidx.navigation.navArgument("itemId") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId")
                            val viewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                            AddClothingScreen(
                                viewModel = viewModel,
                                itemId = itemId,
                                onSaveComplete = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


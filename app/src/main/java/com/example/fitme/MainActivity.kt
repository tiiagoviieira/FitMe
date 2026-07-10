package com.example.fitme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.fitme.data.AppDatabase
import com.example.fitme.ui.theme.* // Importa tudo da tua pasta UI

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
                NavHost(navController = navController, startDestination = "inventory_screen") {
                    composable("inventory_screen") {
                        val viewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                        InventoryScreen(viewModel = viewModel, onNavigateToAdd = { navController.navigate("add_clothing_screen") })
                    }
                    composable("add_clothing_screen") {
                        val viewModel: ClothingViewModel = viewModel(factory = ClothingViewModelFactory(db.clothingDao()))
                        AddClothingScreen(viewModel = viewModel, onSaveComplete = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
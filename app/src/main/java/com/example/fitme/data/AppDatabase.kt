package com.example.fitme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitme.model.ClothingItem
import com.example.fitme.model.Outfit
import com.example.fitme.model.User

// 1. Adicionamos o User e o Outfit à lista de entidades
// 2. Subimos a versão para 2
@Database(entities = [ClothingItem::class, User::class, Outfit::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Ligação ao nosso DAO antigo
    abstract fun clothingDao(): ClothingDao

    // NOVO: Ligações aos novos DAOs para que a MainActivity os possa encontrar
    abstract fun userDao(): UserDao
    abstract fun outfitDao(): OutfitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitme_database"
                )
                // Adicionamos isto para que o Room apague a BD antiga e crie a nova com as novas tabelas
                // sem dar erro (ideal durante a fase de desenvolvimento)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
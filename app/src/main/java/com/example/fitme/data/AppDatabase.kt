package com.example.fitme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitme.model.ClothingItem

// Aqui dizemos ao Room quais são as tabelas (entities) e a versão da base de dados
@Database(entities = [ClothingItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Ligação ao nosso DAO (os "comandos" da base de dados)
    abstract fun clothingDao(): ClothingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Esta função garante que apenas criamos UMA instância da base de dados (Padrão Singleton)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitme_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.example.fitme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.fitme.model.ClothingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {

    // Insere uma roupa nova. Se já existir, substitui (útil para atualizações)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothing(item: ClothingItem)

    // Lê todas as roupas e devolve um Flow (uma "corrente" que atualiza a UI automaticamente)
    @Query("SELECT * FROM clothing_items")
    fun getAllClothing(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :itemId")
    suspend fun getClothingById(itemId: String): ClothingItem?

    @Query("UPDATE clothing_items SET lastWornDate = :date WHERE id = :id")
    suspend fun updateLastWornDate(id: String, date: Long)

    @Delete
    suspend fun deleteClothingList(items: List<ClothingItem>)
}
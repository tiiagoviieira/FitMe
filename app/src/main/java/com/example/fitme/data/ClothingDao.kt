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

    // Insere uma roupa nova. Se já existir, substitui
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothing(item: ClothingItem)

    // Lê todas as roupas e devolve
    @Query("SELECT * FROM clothing_items")
    fun getAllClothing(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    suspend fun getClothingById(id: String): ClothingItem?

    @Query("UPDATE clothing_items SET lastWornDate = :date WHERE id = :id")
    suspend fun updateLastWornDate(id: String, date: Long)

    @Query("DELETE FROM clothing_items WHERE id IN (:itemIds)")
    suspend fun deleteByIds(itemIds: List<String>)

    @Query("SELECT * FROM clothing_items WHERE userId = :userId")
    fun getClothingByUserId(userId: String): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items")
    fun getAllClothingGlobal(): Flow<List<ClothingItem>>
}
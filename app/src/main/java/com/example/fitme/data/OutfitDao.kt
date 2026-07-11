package com.example.fitme.data

import androidx.room.*
import com.example.fitme.model.Outfit
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Insert
    suspend fun insertOutfit(outfit: Outfit)

    @Update
    suspend fun updateOutfits(outfits: List<Outfit>)

    @Query("SELECT * FROM outfits WHERE userId = :userId ORDER BY date DESC")
    fun getOutfitsByUser(userId: String): Flow<List<Outfit>>
}
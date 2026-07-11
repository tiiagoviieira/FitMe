package com.example.fitme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String, // Para associar ao utilizador correto
    val imageUri: String,
    val date: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false
)
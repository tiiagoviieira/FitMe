package com.example.fitme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val imageUri: String, // path to file
    val weatherTag: String, // Estacao
    val lastWornDate: Long? = null, // Data da última utilização
    val createdAt: Long = System.currentTimeMillis()
)
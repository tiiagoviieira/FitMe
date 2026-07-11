package com.example.fitme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val emailOrPhone: String,
    val passwordHash: String, // Numa app real usaríamos Hash, aqui guardamos a string para o MVP
    val profileImageUri: String? = null
)
package com.example.fitme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val profileImageUri: String? = null,
    val hasActiveSession: Boolean = true
)
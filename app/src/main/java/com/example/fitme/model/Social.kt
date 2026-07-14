package com.example.fitme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val status: String = "PENDING"
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val text: String,
    val attachedClothingId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
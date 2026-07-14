package com.example.fitme.data

import androidx.room.*
import com.example.fitme.model.FriendRequest
import com.example.fitme.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {
    // Amizades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(request: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE receiverId = :userId AND status = 'PENDING'")
    fun getPendingRequests(userId: String): Flow<List<FriendRequest>>

    @Query("SELECT * FROM friend_requests WHERE (senderId = :userId OR receiverId = :userId) AND status = 'ACCEPTED'")
    fun getFriendsRequests(userId: String): Flow<List<FriendRequest>>

    @Query("UPDATE friend_requests SET status = :status WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String)

    // Mensagens
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getChatMessages(user1: String, user2: String): Flow<List<Message>>
}
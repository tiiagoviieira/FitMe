package com.example.fitme.data

import androidx.room.*
import com.example.fitme.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    // Atualiza o estado da sessão (true = ativa, false = terminada)
    @Query("UPDATE users SET hasActiveSession = :status WHERE id = :userId")
    suspend fun updateSessionStatus(userId: String, status: Boolean)

    // Devolve apenas os utilizadores que não terminaram a sessão
    @Query("SELECT * FROM users WHERE hasActiveSession = 1")
    fun getActiveUsersFlow(): Flow<List<User>>
}
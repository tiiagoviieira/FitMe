package com.example.fitme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.SocialDao
import com.example.fitme.data.UserDao
import com.example.fitme.model.FriendRequest
import com.example.fitme.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SocialViewModel(private val socialDao: SocialDao, private val userDao: UserDao) : ViewModel() {

    fun sendFriendRequestByPhone(senderId: String, targetPhone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val targetUser = userDao.getUserByPhone(targetPhone)
            if (targetUser != null && targetUser.id != senderId) {
                socialDao.insertFriendRequest(FriendRequest(senderId = senderId, receiverId = targetUser.id))
                onResult(true, "Pedido enviado a ${targetUser.username}!")
            } else {
                onResult(false, "Utilizador não encontrado.")
            }
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) "ACCEPTED" else "REJECTED"
            socialDao.updateRequestStatus(requestId, status)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String, attachedClothingId: String? = null) {
        viewModelScope.launch {
            socialDao.insertMessage(Message(senderId = senderId, receiverId = receiverId, text = text, attachedClothingId = attachedClothingId))
        }
    }

    fun getFriends(userId: String): Flow<List<FriendRequest>> {
        return socialDao.getFriendsRequests(userId)
    }

    fun getPendingRequests(userId: String): Flow<List<FriendRequest>> {
        return socialDao.getPendingRequests(userId)
    }

    fun getChatMessages(user1: String, user2: String): Flow<List<Message>> {
        return socialDao.getChatMessages(user1, user2)
    }
}

class SocialViewModelFactory(private val socialDao: SocialDao, private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) return SocialViewModel(socialDao, userDao) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
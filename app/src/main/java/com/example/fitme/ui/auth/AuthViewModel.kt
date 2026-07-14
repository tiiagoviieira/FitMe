package com.example.fitme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.OutfitDao
import com.example.fitme.data.UserDao
import com.example.fitme.model.Outfit
import com.example.fitme.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDao, private val outfitDao: OutfitDao) : ViewModel() {

    // Guarda o ID do utilizador com sessão iniciada (null = convidado)
    private val _currentUserId = MutableStateFlow<String?>(null)

    val allPublicOutfits = outfitDao.getAllPublicOutfits()
    val currentUserId = _currentUserId.asStateFlow()

    val activeUsers = userDao.getActiveUsersFlow()

    fun login(username: String, pass: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user != null && user.passwordHash == pass) {
                userDao.updateSessionStatus(user.id, true)
                _currentUserId.value = user.id
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun logout() {
        val currentId = _currentUserId.value
        if (currentId != null && currentId != "guest") {
            viewModelScope.launch {
                userDao.updateSessionStatus(currentId, false)
                _currentUserId.value = null
            }
        } else {
            _currentUserId.value = null
        }
    }

    fun register(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            userDao.insertUser(user)
            _currentUserId.value = user.id
            onSuccess()
        }
    }

    val allUsers = userDao.getAllUsersFlow()

    // Função para trocar de utilizador rapidamente
    fun switchUser(userId: String) {
        _currentUserId.value = userId
    }

    fun loginAsGuest() {
        _currentUserId.value = "guest"
    }

    // Funções para os Outfits
    fun saveOutfit(outfit: Outfit) {
        viewModelScope.launch { outfitDao.insertOutfit(outfit) }
    }

    fun toggleOutfitsVisibility(outfits: List<Outfit>, makePublic: Boolean) {
        viewModelScope.launch {
            val updated = outfits.map { it.copy(isPublic = makePublic) }
            outfitDao.updateOutfits(updated)
        }
    }

    fun deleteOutfits(outfits: List<Outfit>) {
        viewModelScope.launch {
            outfitDao.deleteOutfits(outfits)
        }
    }
}
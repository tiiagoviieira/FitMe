package com.example.fitme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitme.data.OutfitDao
import com.example.fitme.data.UserDao

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val outfitDao: OutfitDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, outfitDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
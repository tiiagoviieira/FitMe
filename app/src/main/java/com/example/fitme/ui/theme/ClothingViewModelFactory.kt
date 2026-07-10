package com.example.fitme.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitme.data.ClothingDao

class ClothingViewModelFactory(private val dao: ClothingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClothingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClothingViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.fitme.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.ClothingDao
import com.example.fitme.model.ClothingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
class ClothingViewModel(private val dao: ClothingDao) : ViewModel() {

    // Estados dos filtros e ordenação
    private val _selectedWeatherFilter = MutableStateFlow<String?>(null)
    val selectedWeatherFilter = _selectedWeatherFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    // Tipos de ordenação: "DEFAULT", "MOST_RECENT", "LAST_WORN"
    private val _currentSort = MutableStateFlow("DEFAULT")
    val currentSort = _currentSort.asStateFlow()

    // Fluxo principal de dados combinado com filtros e ordenação
    val processedClothing = combine(
        dao.getAllClothing(),
        _selectedWeatherFilter,
        _selectedCategoryFilter,
        _currentSort
    ) { originalList, weather, category, sort ->
        var list = originalList

        // 1. Aplicar Filtro de Clima
        if (weather != null) {
            list = list.filter { it.weatherTag.equals(weather, ignoreCase = true) }
        }

        // 2. Aplicar Filtro de Categoria
        if (category != null) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 3. Aplicar Ordenação
        when (sort) {
            "MOST_RECENT" -> list.sortedByDescending { it.createdAt } // Criadas mais recentemente primeiro
            "LAST_WORN" -> list.sortedByDescending { it.lastWornDate ?: 0L } // Usadas mais recentemente
            else -> list.sortedBy { it.createdAt } // Default: Ordem de criação original
        }
    }

    // Listas dinâmicas para o ecrã de filtros (extraídas do que já existe na BD)
    val availableWeathers = dao.getAllClothing().map { list ->
        list.map { it.weatherTag.trim() }.distinct().filter { it.isNotBlank() }
    }

    val availableCategories = dao.getAllClothing().map { list ->
        list.map { it.category.trim() }.distinct().filter { it.isNotBlank() }
    }

    fun applyFiltersAndSort(weather: String?, category: String?, sort: String) {
        _selectedWeatherFilter.value = weather
        _selectedCategoryFilter.value = category
        _currentSort.value = sort
    }

    fun addClothing(item: ClothingItem) {
        viewModelScope.launch { dao.insertClothing(item) }
    }

    fun deleteMultipleClothing(items: List<ClothingItem>) {
        viewModelScope.launch { dao.deleteClothingList(items) }
    }

    fun updateLastWornDate(id: String) {
        viewModelScope.launch {
            dao.updateLastWornDate(id, System.currentTimeMillis())
        }
    }

    suspend fun getClothingById(id: String): ClothingItem? {
        return dao.getClothingById(id)
    }
}
package com.example.fitme.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.ClothingDao
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Uma estrutura simples para guardar quem disse o quê
data class ChatMessage(val text: String, val isUser: Boolean)

class AiViewModel(private val clothingDao: ClothingDao) : ViewModel() {

    private val apiKey = ""

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash", // O modelo mais rápido e eficiente
        apiKey = apiKey
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        // Mensagem de boas vindas automática
        _messages.value = listOf(
            ChatMessage("Olá! Sou o teu estilista pessoal. O que queres vestir hoje?", isUser = false)
        )
    }

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return

        // 1. Adiciona a mensagem do utilizador ao ecrã
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(userPrompt, isUser = true))
        _messages.value = currentMessages
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 2. Obtém as roupas da base de dados
                val inventory = clothingDao.getAllClothing().first()

                // 3. Traduz o roupeiro para texto
                val inventoryText = inventory.joinToString("\n") { item ->
                    val daysSinceWorn = item.lastWornDate?.let {
                        val diff = System.currentTimeMillis() - it
                        (diff / (1000 * 60 * 60 * 24)).toString() + " dias"
                    } ?: "Nunca usada"

                    "- ${item.name} (Categoria: ${item.category}, Clima: ${item.weatherTag}, Última vez: $daysSinceWorn)"
                }

                // 4. Cria o "Super Prompt" com contexto
                val systemContext = """
                    Tu és um estilista de moda pessoal. O utilizador tem o seguinte roupeiro:
                    $inventoryText
                    
                    Responde de forma curta, amigável e em português ao seguinte pedido do utilizador usando apenas as peças acima.
                    Pedido: "$userPrompt"
                """.trimIndent()

                // 5. Envia para o Gemini
                val response = generativeModel.generateContent(systemContext)

                // 6. Adiciona a resposta da IA ao ecrã
                response.text?.let { aiResponse ->
                    _messages.value = _messages.value + ChatMessage(aiResponse, isUser = false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Desculpa, a minha ligação falhou. Tenta novamente.", isUser = false)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Factory para ligar a Base de Dados ao ViewModel
class AiViewModelFactory(private val clothingDao: ClothingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiViewModel(clothingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
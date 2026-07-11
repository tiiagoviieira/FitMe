package com.example.fitme.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fitme.model.ClothingItem
import com.example.fitme.ui.theme.ClothingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun processOutfitMatching(
    context: Context,
    capturedUri: Uri,
    viewModel: ClothingViewModel,
    coroutineScope: CoroutineScope,
    onResult: (List<ClothingItem>) -> Unit // NOVO: Callback para devolver a lista ordenada
) {
    coroutineScope.launch(Dispatchers.IO) {
        try {
            // 1. Converter a foto capturada
            val capturedBitmap = uriToBitmap(context, capturedUri)
            if (capturedBitmap == null) {
                withContext(Dispatchers.Main) { onResult(emptyList()) }
                return@launch
            }

            // 2. Calcular o Histograma de Cores da foto tirada
            val capturedHist = calculateColorHistogram(capturedBitmap)

            // 3. Obter todas as peças do roupeiro
            val inventory = viewModel.processedClothing.first()
            val scoredItems = mutableListOf<Pair<ClothingItem, Double>>()

            // 4. Comparar com cada peça do inventário
            for (item in inventory) {
                if (item.imageUri.isNotBlank()) {
                    val itemUri = Uri.parse(item.imageUri)
                    val itemBitmap = uriToBitmap(context, itemUri)

                    if (itemBitmap != null) {
                        val itemHist = calculateColorHistogram(itemBitmap)

                        // Calcula a percentagem de cores iguais
                        val similarity = compareHistograms(capturedHist, itemHist)
                        scoredItems.add(Pair(item, similarity))
                    }
                }
            }

            // 5. Ordenar as roupas (maior probabilidade de correspondência primeiro)
            scoredItems.sortByDescending { it.second }
            val rankedList = scoredItems.map { it.first }

            // 6. Devolver a lista ao ecrã principal
            withContext(Dispatchers.Main) {
                onResult(rankedList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { onResult(emptyList()) }
        }
    }
}

// --- FUNÇÕES DE HISTOGRAMA DE CORES ---

private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        // Reduzimos o tamanho para poupar memória e acelerar os cálculos
        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
        BitmapFactory.decodeStream(inputStream, null, options)
    } catch (e: Exception) {
        null
    }
}

private fun calculateColorHistogram(bitmap: Bitmap): FloatArray {
    // Escala para uma grelha rápida de 64x64 píxeis
    val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, false)
    val hist = FloatArray(64)
    var totalPixels = 0

    for (x in 0 until 64) {
        for (y in 0 until 64) {
            val pixel = scaled.getPixel(x, y)
            // Divide os 255 tons de cor em apenas 4 blocos (4 Vermelhos, 4 Verdes, 4 Azuis = 64 blocos)
            val r = android.graphics.Color.red(pixel) / 64
            val g = android.graphics.Color.green(pixel) / 64
            val b = android.graphics.Color.blue(pixel) / 64

            val binIndex = (r * 16) + (g * 4) + b
            hist[binIndex]++
            totalPixels++
        }
    }

    // Normalizar para obter percentagens
    for (i in 0 until 64) {
        hist[i] = hist[i] / totalPixels
    }
    return hist
}

private fun compareHistograms(hist1: FloatArray, hist2: FloatArray): Double {
    // Intersecção de Histogramas: Soma o mínimo de cada bloco de cor entre as duas fotos
    var intersection = 0.0
    for (i in 0 until 64) {
        intersection += Math.min(hist1[i].toDouble(), hist2[i].toDouble())
    }
    return intersection // Devolve um valor até 1.0 (100% igual)
}
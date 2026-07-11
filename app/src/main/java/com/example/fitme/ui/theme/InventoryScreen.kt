package com.example.fitme.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.fitme.model.ClothingItem
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    viewModel: ClothingViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val clothingList by viewModel.processedClothing.collectAsState(initial = emptyList())
    val weathersOptions by viewModel.availableWeathers.collectAsState(initial = emptyList())
    val categoriesOptions by viewModel.availableCategories.collectAsState(initial = emptyList())

    // Estados do Modo de Seleção (Apagar múltiplos)
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(emptySet<ClothingItem>()) }

    // Estados para o Menu Flutuante e Diálogos
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedItems.size}") }, // Apenas o número para dar espaço
                    navigationIcon = {
                        IconButton(onClick = { isSelectionMode = false; selectedItems = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        // O botão Editar SÓ aparece se houver 1 item selecionado
                        if (selectedItems.size == 1) {
                            IconButton(onClick = {
                                val itemToEdit = selectedItems.first()
                                isSelectionMode = false
                                selectedItems = emptySet()
                                onNavigateToEdit(itemToEdit.id) // Envia o ID para editar
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }

                        TextButton(onClick = { selectedItems = if (selectedItems.size == clothingList.size) emptySet() else clothingList.toSet() }) {
                            Text(if (selectedItems.size == clothingList.size) "Nenhum" else "Todos")
                        }

                        IconButton(onClick = {
                            viewModel.deleteMultipleClothing(selectedItems.toList())
                            isSelectionMode = false
                            selectedItems = emptySet()
                        }) {
                            // Removida a cor "error" para ficar uniforme com os outros ícones
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                TopAppBar(
                    title = { Text("Roupeiro", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isMenuExpanded) {
                        // 1. Botão de Filtrar (Em cima)
                        SmallFloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                showFilterDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Filtrar")
                        }

                        // 2. Botão de Ordenar (No meio)
                        SmallFloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                showSortDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Ordenar")
                        }

                        // 3. Botão de Adicionar Peça (Em baixo)
                        SmallFloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                onNavigateToAdd()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Peça")
                        }
                    }

                    // Botão Principal de 3 Pontos
                    FloatingActionButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        containerColor = if (isMenuExpanded) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Listagem de Roupas
        if (clothingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhuma peça encontrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clothingList, key = { it.id }) { item ->
                    val isSelected = selectedItems.contains(item)

                    // ==========================================
                    // 1. LÓGICA DE TEMPERATURA (0 a 30 dias)
                    // ==========================================
                    val currentTime = System.currentTimeMillis()
                    val lastWorn = item.lastWornDate ?: 0L
                    val oneMonthMillis = 30L * 24 * 60 * 60 * 1000 // 30 dias em milissegundos

                    // Calcula a percentagem de uso (1.0 = Hoje, 0.0 = Há mais de 1 mês ou nunca)
                    val temperatureFraction = if (lastWorn <= 0L) {
                        0f
                    } else {
                        val elapsed = currentTime - lastWorn
                        if (elapsed > oneMonthMillis) {
                            0f
                        } else {
                            1f - (elapsed.toFloat() / oneMonthMillis.toFloat())
                        }
                    }.coerceIn(0f, 1f) // Garante que fica estritamente entre 0 e 1

                    // Cria um degradê dinâmico entre Azul (Frio) e Vermelho (Quente)
                    val tempColor = androidx.compose.ui.graphics.lerp(
                        start = androidx.compose.ui.graphics.Color(0xFF4FC3F7), // Azul Gelo
                        stop = androidx.compose.ui.graphics.Color(0xFFFF5252),  // Vermelho Fogo
                        fraction = temperatureFraction
                    )
                    // ==========================================

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                        if (selectedItems.isEmpty()) isSelectionMode = false
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) { isSelectionMode = true; selectedItems = setOf(item) }
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Imagem da Peça
                            AsyncImage(model = item.imageUri, contentDescription = null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)

                            Spacer(modifier = Modifier.width(16.dp))

                            // Textos e Barra de Temperatura
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${item.category} • ${item.weatherTag}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))

                                // O DESENHO DA BARRA
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Emoji Frio
                                    Text(text = "❄️", style = MaterialTheme.typography.labelSmall)

                                    LinearProgressIndicator(
                                        progress = temperatureFraction,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 6.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = tempColor,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )

                                    // Emoji Quente
                                    Text(text = "🔥", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            if (isSelectionMode) { Checkbox(checked = isSelected, onCheckedChange = null) }
                        }
                    }
                }
            }
        }

        // ==========================================
        // ECRÃ 1: DIÁLOGO APENAS DE FILTROS
        // ==========================================
        if (showFilterDialog) {
            val initialWeather by viewModel.selectedWeatherFilter.collectAsState()
            val initialCategory by viewModel.selectedCategoryFilter.collectAsState()
            val currentSort by viewModel.currentSort.collectAsState() // Precisamos do sort atual para não o perder

            var tempWeather by remember { mutableStateOf(initialWeather) }
            var tempCategory by remember { mutableStateOf(initialCategory) }

            Dialog(onDismissRequest = { showFilterDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Filtrar Peças", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            // SECÇÃO: FILTRO DE CLIMA
                            if (weathersOptions.isNotEmpty()) {
                                item { Text("Clima:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.primary) }
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { tempWeather = null }).padding(vertical = 4.dp)) {
                                        RadioButton(selected = tempWeather == null, onClick = { tempWeather = null })
                                        Text("Todos", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                items(weathersOptions) { weather ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { tempWeather = weather }).padding(vertical = 4.dp)) {
                                        RadioButton(selected = tempWeather == weather, onClick = { tempWeather = weather })
                                        Text(weather, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            // SECÇÃO: FILTRO DE CATEGORIA
                            if (categoriesOptions.isNotEmpty()) {
                                item { Text("Categoria:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary) }
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { tempCategory = null }).padding(vertical = 4.dp)) {
                                        RadioButton(selected = tempCategory == null, onClick = { tempCategory = null })
                                        Text("Todas", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                items(categoriesOptions) { category ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { tempCategory = category }).padding(vertical = 4.dp)) {
                                        RadioButton(selected = tempCategory == category, onClick = { tempCategory = category })
                                        Text(category, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ElevatedButton(
                                onClick = { showFilterDialog = false },
                                shape = RoundedCornerShape(50),
                                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) { Text("Cancelar", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }

                            ElevatedButton(
                                onClick = {
                                    viewModel.applyFiltersAndSort(tempWeather, tempCategory, currentSort)
                                    showFilterDialog = false
                                },
                                shape = RoundedCornerShape(50),
                                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) { Text("Aplicar", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                        }
                    }
                }
            }
        }

        // ==========================================
        // ECRÃ 2: DIÁLOGO APENAS DE ORDENAÇÃO
        // ==========================================
        if (showSortDialog) {
            val currentWeather by viewModel.selectedWeatherFilter.collectAsState() // Precisamos dos filtros atuais para não os perder
            val currentCategory by viewModel.selectedCategoryFilter.collectAsState()
            val initialSort by viewModel.currentSort.collectAsState()

            var tempSort by remember { mutableStateOf(initialSort) }

            Dialog(onDismissRequest = { showSortDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Ordenar Peças", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            item { Text("Ordenar por:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.primary) }
                            val sortOptions = listOf("DEFAULT" to "Ordem de Criação", "MOST_RECENT" to "Mais recente", "LAST_WORN" to "Última utilização")
                            items(sortOptions) { (key, label) ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { tempSort = key }).padding(vertical = 4.dp)) {
                                    RadioButton(selected = tempSort == key, onClick = { tempSort = key })
                                    Text(label, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ElevatedButton(
                                onClick = { showSortDialog = false },
                                shape = RoundedCornerShape(50),
                                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) { Text("Cancelar", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }

                            ElevatedButton(
                                onClick = {
                                    viewModel.applyFiltersAndSort(currentWeather, currentCategory, tempSort)
                                    showSortDialog = false
                                },
                                shape = RoundedCornerShape(50),
                                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) { Text("Aplicar", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.combinedClickable(onClick: () -> Unit, onLongClick: () -> Unit) = this
package com.example.macaveavin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.R
import com.example.macaveavin.data.CellarConfig
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    cellars: List<CellarConfig>,
    wineCounts: List<Int> = emptyList(),
    onOpenCellar: (index: Int) -> Unit,
    onAddCellar: () -> Unit,
    onQuickAdd: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onDeleteCellar: ((index: Int) -> Unit)? = null,
    onRenameCellar: ((index: Int, newName: String) -> Unit)? = null,
    onMoveCellar: ((from: Int, to: Int) -> Unit)? = null
) {
    val showConfirm = remember { mutableStateOf<Int?>(null) }
    val showRename = remember { mutableStateOf<Int?>(null) }
    val renameText = remember { mutableStateOf("") }
    val expandedMenu = remember { mutableStateOf<Int?>(null) }
    val pullState = rememberSwipeRefreshState(isRefreshing)
    val draggingFrom = remember { mutableStateOf<Int?>(null) }
    val dropIndex = remember { mutableStateOf<Int?>(null) }

    SwipeRefresh(
        state = pullState,
        onRefresh = { onRefresh?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist header and empty state (replaces large icon)
            val isEmpty = cellars.isEmpty()
            if (isEmpty) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Organisez votre cave",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Créez votre première cave pour ajouter vos bouteilles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
                androidx.compose.material3.FilledTonalButton(
                    onClick = onAddCellar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .animateContentSize()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Créer une cave", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text(text = "Vos caves", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(Modifier.height(12.dp))
            }

            // List: full-width rectangular items of existing caves
            cellars.forEachIndexed { index, cfg ->
                if (draggingFrom.value != null && dropIndex.value == index && dropIndex.value != draggingFrom.value) {
                    Spacer(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.height(8.dp))
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .animateContentSize()
                        .clickable { onOpenCellar(index) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val density = LocalDensity.current
                            val slotPx = with(density) { (72.dp + 12.dp).toPx() }
                            Icon(
                                Icons.Filled.DragHandle,
                                contentDescription = "Réorganiser",
                                modifier = Modifier.pointerInput(index, cellars.size) {
                                    var totalDy = 0f
                                    detectDragGestures(
                                        onDragStart = {
                                            totalDy = 0f
                                            draggingFrom.value = index
                                            dropIndex.value = index
                                        },
                                        onDrag = { change, dragAmount ->
                                            totalDy += dragAmount.y
                                            val moved = (totalDy / slotPx).roundToInt()
                                            val target = (index + moved).coerceIn(0, cellars.size)
                                            dropIndex.value = target
                                            change.consume()
                                        },
                                        onDragCancel = {
                                            draggingFrom.value = null
                                            dropIndex.value = null
                                        },
                                        onDragEnd = {
                                            val from = draggingFrom.value
                                            val target = dropIndex.value
                                            if (from != null && target != null) {
                                                val lastIndex = (cellars.size - 1).coerceAtLeast(0)
                                                val clamped = target.coerceIn(0, lastIndex)
                                                if (clamped != from) {
                                                    onMoveCellar?.invoke(from, clamped)
                                                }
                                            }
                                            draggingFrom.value = null
                                            dropIndex.value = null
                                        }
                                    )
                                }
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val count = wineCounts.getOrNull(index) ?: 0
                                val totalSlots = cfg.enabledCells?.size ?: (cfg.rows * cfg.cols)
                                val pct = if (totalSlots > 0) ((count * 100f) / totalSlots).roundToInt().coerceIn(0, 100) else 0
                                Text(
                                    text = cfg.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.WineBar,
                                        contentDescription = "$count bouteille${if (count > 1) "s" else ""}",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                expandedMenu.value = index
                                renameText.value = cfg.name
                            }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Plus d'actions")
                            }
                            DropdownMenu(
                                expanded = expandedMenu.value == index,
                                onDismissRequest = { expandedMenu.value = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Renommer") },
                                    onClick = {
                                        expandedMenu.value = null
                                        showRename.value = index
                                        renameText.value = cfg.name
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer") },
                                    onClick = {
                                        expandedMenu.value = null
                                        showConfirm.value = index
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // End-of-list drop indicator when targeting after last item
            if (draggingFrom.value != null && dropIndex.value == cellars.size && dropIndex.value != draggingFrom.value) {
                Spacer(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.height(8.dp))
            }

            if (!isEmpty) {
                // Add new cellar primary action: outlined button (border only, no fill)
                androidx.compose.material3.OutlinedButton(
                    onClick = onAddCellar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .animateContentSize()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Ajouter une cave", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (onQuickAdd != null) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onQuickAdd, modifier = Modifier.fillMaxWidth()) { Text("Ajouter une bouteille") }
            }
        }

        // Delete confirmation dialog
        val pending = showConfirm.value
        if (pending != null && onDeleteCellar != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConfirm.value = null },
                confirmButton = {
                    Button(onClick = {
                        onDeleteCellar.invoke(pending)
                        showConfirm.value = null
                    }) { Text("Confirmer") }
                },
                dismissButton = {
                    Button(onClick = { showConfirm.value = null }) { Text("Annuler") }
                },
                title = { Text("Supprimer cette cave ?") },
                text = { Text("Cette action est irréversible.") }
            )
        }

        // Rename dialog
        val renameIdx = showRename.value
        if (renameIdx != null && onRenameCellar != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showRename.value = null },
                confirmButton = {
                    Button(onClick = {
                        val newName = renameText.value.trim()
                        if (newName.isNotEmpty()) {
                            onRenameCellar.invoke(renameIdx, newName)
                        }
                        showRename.value = null
                    }) { Text("Renommer") }
                },
                dismissButton = {
                    Button(onClick = { showRename.value = null }) { Text("Annuler") }
                },
                title = { Text("Renommer la cave") },
                text = {
                    Column { 
                        Text("Entrez le nouveau nom de la cave :")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = renameText.value,
                            onValueChange = { renameText.value = it },
                            singleLine = true,
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

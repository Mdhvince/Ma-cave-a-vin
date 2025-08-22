package com.example.macaveavin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.Wine
import androidx.compose.foundation.gestures.detectDragGestures

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CellarScreen(
    rows: Int,
    cols: Int,
    wines: List<Wine>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCellClick: (row: Int, col: Int) -> Unit,
    onAdd: (row: Int, col: Int) -> Unit,
    onMoveWine: (wineId: String, row: Int, col: Int) -> Unit,
    onOpenSetup: () -> Unit
) {
    val wineByPos = remember(wines) { wines.associateBy { it.row to it.col } }
    val highlightIds = remember(query, wines) {
        val q = query.trim().lowercase()
        if (q.isBlank()) emptySet() else wines.filter {
            it.name.lowercase().contains(q) ||
                    (it.vintage?.lowercase()?.contains(q) == true) ||
                    (it.comment?.lowercase()?.contains(q) == true)
        }.map { it.id }.toSet()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Ma cave à vin") }, actions = {
            IconButton(onClick = onOpenSetup) { Text("Config") }
        })
        Column(Modifier.padding(16.dp)) {
            Text("Appui long pour saisir, faites glisser vers une autre case pour déplacer. Appuyez sur une case vide pour ajouter.")
            Spacer(Modifier.height(8.dp))

            // Drag & drop state
            val draggingId = remember { mutableStateOf<String?>(null) }
            val targetCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }

            val hSpacing = 8.dp
            val vSpacing = 8.dp
            val cellHeight = 88.dp

            var containerX = 0f
            var containerY = 0f
            var containerW = 0f
            var containerH = 0f

            val density = androidx.compose.ui.platform.LocalDensity.current
            val hSpacingPx = with(density) { hSpacing.toPx() }
            val vSpacingPx = with(density) { vSpacing.toPx() }
            val cellHeightPx = with(density) { cellHeight.toPx() }

            Column(
                verticalArrangement = Arrangement.spacedBy(vSpacing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInWindow()
                        containerX = pos.x
                        containerY = pos.y
                        containerW = coords.size.width.toFloat()
                        containerH = coords.size.height.toFloat()
                    }
                    .pointerInput(rows, cols, wines) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Start dragging only if started over a wine cell
                                val (r, c) = pointToCell(
                                    offset.x - containerX,
                                    offset.y - containerY,
                                    containerW,
                                    cols,
                                    rows,
                                    hSpacingPx,
                                    cellHeightPx,
                                    vSpacingPx
                                ) ?: return@detectDragGestures
                                val w = wineByPos[r to c]
                                if (w != null) draggingId.value = w.id
                            },
                            onDrag = { change, _ ->
                                val x = change.position.x - containerX
                                val y = change.position.y - containerY
                                targetCell.value = pointToCell(x, y, containerW, cols, rows, hSpacingPx, cellHeightPx, vSpacingPx)
                            },
                            onDragEnd = {
                                val id = draggingId.value
                                val tgt = targetCell.value
                                if (id != null && tgt != null) {
                                    onMoveWine(id, tgt.first, tgt.second)
                                }
                                draggingId.value = null
                                targetCell.value = null
                            },
                            onDragCancel = {
                                draggingId.value = null
                                targetCell.value = null
                            }
                        )
                    }
            ) {
                repeat(rows) { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(hSpacing), modifier = Modifier.fillMaxWidth()) {
                        repeat(cols) { c ->
                            val wine = wineByPos[r to c]
                            val isTarget = targetCell.value?.first == r && targetCell.value?.second == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cellHeight)
                                    .border(BorderStroke(2.dp, if (isTarget) Color(0xFF00C853) else Color.Gray))
                                    .combinedClickable(
                                        onClick = { onCellClick(r, c) },
                                        onLongClick = {
                                            if (wine != null) draggingId.value = wine.id
                                        }
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (wine == null) {
                                    Text("+")
                                } else {
                                    if (wine.photoUri != null) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(wine.photoUri),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("Bouteille")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun pointToCell(
    x: Float,
    y: Float,
    containerWidthPx: Float,
    cols: Int,
    rows: Int,
    hSpacingPx: Float,
    cellHeightPx: Float,
    vSpacingPx: Float
): Pair<Int, Int>? {
    if (cols <= 0) return null
    val totalHSpacing = hSpacingPx * (cols - 1)
    val cellWidth = ((containerWidthPx - totalHSpacing) / cols).coerceAtLeast(1f)
    if (x < 0f || y < 0f) return null
    val col = (x / (cellWidth + hSpacingPx)).toInt()
    val row = (y / (cellHeightPx + vSpacingPx)).toInt()
    if (col !in 0 until cols) return null
    if (row !in 0 until rows) return null
    return row to col
}

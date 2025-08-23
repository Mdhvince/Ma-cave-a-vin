package com.example.macaveavin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.CellarConfig
import com.example.macaveavin.data.Wine
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import com.example.macaveavin.ui.HexagonShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CellarScreen(
    config: CellarConfig,
    wines: List<Wine>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCellClick: (row: Int, col: Int) -> Unit,
    onAdd: (row: Int, col: Int) -> Unit,
    onMoveWine: (wineId: String, row: Int, col: Int) -> Unit,
    onOpenSetup: () -> Unit,
    onMoveCompartment: (srcRow: Int, srcCol: Int, dstRow: Int, dstCol: Int) -> Unit,
    cellars: List<CellarConfig>,
    onSelectCellar: (Int) -> Unit
) {
    val wineByPos = remember(wines) { wines.associateBy { it.row to it.col } }
    val enabledSet: Set<Pair<Int, Int>> = remember(config) {
        config.enabledCells ?: buildSet {
            for (r in 0 until config.rows) for (c in 0 until config.cols) add(r to c)
        }
    }

    Column(Modifier.fillMaxSize()) {
        run {
            var expanded = remember { mutableStateOf(false) }
            val selectedIndex = remember(config, cellars) { cellars.indexOf(config).coerceAtLeast(0) }
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { expanded.value = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(cellars.getOrNull(selectedIndex)?.name ?: config.name)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                            cellars.forEachIndexed { idx, c ->
                                DropdownMenuItem(text = { Text(c.name) }, onClick = { expanded.value = false; onSelectCellar(idx) })
                            }
                        }
                    }
                },
                actions = { TextButton(onClick = onOpenSetup) { Text("Paramètres") } }
            )
        }
        Column(Modifier
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(bottom = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Drag & drop state
            val draggingWineId = remember { mutableStateOf<String?>(null) }
            val draggingCompartmentSrc = remember { mutableStateOf<Pair<Int, Int>?>(null) }
            val targetCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }

            val hSpacing = 8.dp
            val vSpacing = 12.dp

            var containerW = 0f

            val density = androidx.compose.ui.platform.LocalDensity.current
            val hSpacingPx = with(density) { hSpacing.toPx() }
            val vSpacingPx = with(density) { vSpacing.toPx() }

            val rowsCount = config.rows
            val maxCols = config.cols

            Column(
                verticalArrangement = Arrangement.spacedBy(vSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .onGloballyPositioned { coords ->
                        containerW = coords.size.width.toFloat()
                    }
                    .pointerInput(config, wines, enabledSet) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                val hit = pointToCell(
                                    offset.x,
                                    offset.y,
                                    containerW,
                                    maxCols,
                                    rowsCount,
                                    hSpacingPx,
                                    vSpacingPx
                                ) ?: return@detectDragGesturesAfterLongPress
                                val (r, c) = hit
                                val w = wineByPos[r to c]
                                if (w != null) {
                                    draggingWineId.value = w.id
                                } else if ((r to c) in enabledSet) {
                                    draggingCompartmentSrc.value = r to c
                                }
                            },
                            onDrag = { change, _ ->
                                val x = change.position.x
                                val y = change.position.y
                                val tgt = pointToCell(x, y, containerW, maxCols, rowsCount, hSpacingPx, vSpacingPx)
                                targetCell.value = tgt
                            },
                            onDragEnd = {
                                val tgt = targetCell.value
                                if (tgt != null) {
                                    val (tr, tc) = tgt
                                    val wineId = draggingWineId.value
                                    val compSrc = draggingCompartmentSrc.value
                                    if (wineId != null && (tr to tc) in enabledSet) {
                                        onMoveWine(wineId, tr, tc)
                                    } else if (compSrc != null && (tr to tc) !in enabledSet && wineByPos[tgt] == null) {
                                        onMoveCompartment(compSrc.first, compSrc.second, tr, tc)
                                    }
                                }
                                draggingWineId.value = null
                                draggingCompartmentSrc.value = null
                                targetCell.value = null
                            },
                            onDragCancel = {
                                draggingWineId.value = null
                                draggingCompartmentSrc.value = null
                                targetCell.value = null
                            }
                        )
                    }
            ) {
                repeat(rowsCount) { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(hSpacing), modifier = Modifier.fillMaxWidth()) {
                        repeat(maxCols) { c ->
                            val wine = wineByPos[r to c]
                            val enabled = (r to c) in enabledSet
                            val isTarget = targetCell.value?.first == r && targetCell.value?.second == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .let { base ->
                                        val targetBorderColor = when {
                                            isTarget -> Color(0xFF00C853)
                                            wine != null -> MaterialTheme.colorScheme.primary
                                            enabled -> MaterialTheme.colorScheme.outline
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        }
                                        val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "cellBorder")
                                        val bg = when {
                                            wine != null -> base
                                            enabled -> base.background(
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                shape = HexagonShape()
                                            )
                                            else -> base
                                        }
                                        bg.border(BorderStroke(2.dp, borderColor), shape = HexagonShape())
                                    }
                                    .animateContentSize()
                                    .combinedClickable(
                                        enabled = enabled,
                                        onClick = { if (enabled) onCellClick(r, c) }
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (wine == null) {
                                    if (enabled) Text("+")
                                } else {
                                    if (wine.photoUri != null) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(wine.photoUri),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(HexagonShape())
                                        )
                                    } else {
                                        Text(wine.name)
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
    vSpacingPx: Float
): Pair<Int, Int>? {
    if (cols <= 0) return null
    val totalHSpacing = hSpacingPx * (cols - 1)
    val cellWidth = ((containerWidthPx - totalHSpacing) / cols).coerceAtLeast(1f)
    val cellHeight = cellWidth
    if (x < 0f || y < 0f) return null
    val col = (x / (cellWidth + hSpacingPx)).toInt()
    val row = (y / (cellHeight + vSpacingPx)).toInt()
    if (col !in 0 until cols) return null
    if (row !in 0 until rows) return null
    return row to col
}

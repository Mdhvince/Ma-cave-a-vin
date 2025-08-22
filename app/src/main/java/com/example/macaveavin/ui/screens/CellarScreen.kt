package com.example.macaveavin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.data.Wine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarScreen(
    rows: Int,
    cols: Int,
    wines: List<Wine>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCellClick: (row: Int, col: Int) -> Unit,
    onAdd: (row: Int, col: Int) -> Unit
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
        TopAppBar(title = { Text("Ma cave à vin") })
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "search_field" },
                placeholder = { Text("Rechercher (nom, millésime, commentaire)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (query.isBlank()) "Appuyez sur une case vide pour ajouter un vin" else "${'$'}{highlightIds.size} résultat(s) mis en évidence",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val itemsList = List(rows * cols) { index -> index }
                items(itemsList) { index ->
                    val r = index / cols
                    val c = index % cols
                    val wine = wineByPos[r to c]
                    val isHighlighted = wine != null && wine.id in highlightIds
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .border(
                                BorderStroke(2.dp, if (isHighlighted) Color(0xFF00C853) else Color.Gray)
                            )
                            .clickable { onCellClick(r, c) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (wine == null) {
                            Text("+", color = Color.Gray)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(wine.name)
                                Row { Text(wine.vintage ?: "") }
                            }
                        }
                    }
                }
            }
        }
    }
}

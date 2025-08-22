package com.example.macaveavin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.data.CellarConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    config: CellarConfig,
    onSelectPreset: (rows: Int, cols: Int) -> Unit,
    onContinue: () -> Unit
) {
    val rowsState = remember(config.rows) { mutableIntStateOf(config.rows) }
    val colsState = remember(config.cols) { mutableIntStateOf(config.cols) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(title = { Text("Configuration de la cave") })
        Spacer(Modifier.height(24.dp))
        Text("Choisissez la taille de la grille")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { rowsState.intValue = 3; colsState.intValue = 3 }) { Text("3x3") }
            Button(onClick = { rowsState.intValue = 4; colsState.intValue = 4 }) { Text("4x4") }
            Button(onClick = { rowsState.intValue = 5; colsState.intValue = 5 }) { Text("5x5") }
        }
        Spacer(Modifier.height(16.dp))
        Text("Lignes: ${'$'}{rowsState.intValue}")
        Slider(
            value = rowsState.intValue.toFloat(),
            onValueChange = { rowsState.intValue = it.toInt().coerceIn(1, 12) },
            valueRange = 1f..12f,
            steps = 10,
            modifier = Modifier.semantics { contentDescription = "slider_lignes" }
        )
        Text("Colonnes: ${'$'}{colsState.intValue}")
        Slider(
            value = colsState.intValue.toFloat(),
            onValueChange = { colsState.intValue = it.toInt().coerceIn(1, 12) },
            valueRange = 1f..12f,
            steps = 10,
            modifier = Modifier.semantics { contentDescription = "slider_colonnes" }
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                onSelectPreset(rowsState.intValue, colsState.intValue)
                onContinue()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuer", textAlign = TextAlign.Center)
        }
    }
}

package com.example.macaveavin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.data.CellarConfig
import com.example.macaveavin.ui.HexagonShape
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    config: CellarConfig,
    onSetName: (String) -> Unit,
    onSetRows: (Int) -> Unit,
    onSetCols: (Int) -> Unit,
    onContinue: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Configuration de la cave") },
            navigationIcon = {
                if (onCancel != null) {
                    IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour") }
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = config.name, onValueChange = onSetName, label = { Text("Nom de la cave") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth()) {
            Text("Rangées : ${config.rows}")
            Slider(
                value = config.rows.toFloat(),
                onValueChange = { v -> onSetRows(v.roundToInt().coerceIn(1, 24)) },
                valueRange = 1f..24f,
                steps = 22
            )
            Spacer(Modifier.height(8.dp))
            Text("Colonnes : ${config.cols}")
            Slider(
                value = config.cols.toFloat(),
                onValueChange = { v -> onSetCols(v.roundToInt().coerceIn(1, 24)) },
                valueRange = 1f..24f,
                steps = 22
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Aperçu")
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(config.rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(config.cols) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(BorderStroke(2.dp, Color.Gray), shape = HexagonShape())
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (onCancel != null) {
                Button(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Annuler") }
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Mise à jour", textAlign = TextAlign.Center)
            }
        }
    }
}

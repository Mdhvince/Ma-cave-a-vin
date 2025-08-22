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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.data.CaveShape
import com.example.macaveavin.data.CellarConfig
import com.example.macaveavin.ui.HexagonShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    config: CellarConfig,
    onSetName: (String) -> Unit,
    onSelectShape: (CaveShape) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(title = { Text("Configuration de la cave") })
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = config.name, onValueChange = onSetName, label = { Text("Nom de la cave") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Text("Forme de la cave")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSelectShape(CaveShape.SIMPLE) }, modifier = Modifier.weight(1f)) { Text("Simple") }
            Button(onClick = { onSelectShape(CaveShape.PYRAMID) }, modifier = Modifier.weight(1f)) { Text("Pyramidale") }
        }
        Spacer(Modifier.height(16.dp))
        // Aperçu en temps réel en nid d'abeille (simple approximation)
        Text("Aperçu")
        Spacer(Modifier.height(8.dp))
        val previewRows = if (config.shape == CaveShape.SIMPLE) config.simpleSize else config.pyramidBase
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(previewRows) { r ->
                val colsInRow = if (config.shape == CaveShape.SIMPLE) config.simpleSize else (r + 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    val leading = if (config.shape == CaveShape.SIMPLE) 0 else (previewRows - colsInRow)
                    repeat(leading) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                    repeat(colsInRow) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(BorderStroke(2.dp, Color.Gray), shape = HexagonShape())
                        )
                    }
                    repeat(leading) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuer", textAlign = TextAlign.Center)
        }
    }
}

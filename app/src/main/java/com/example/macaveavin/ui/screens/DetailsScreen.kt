package com.example.macaveavin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.Wine
import com.example.macaveavin.ui.StarRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    wine: Wine,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMove: (row: Int, col: Int) -> Unit,
    onDelete: () -> Unit
) {
    val rowState = remember(wine.row) { mutableIntStateOf(wine.row) }
    val colState = remember(wine.col) { mutableIntStateOf(wine.col) }
    val showConfirm = remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Fiche d'identité du vin") })
        Spacer(Modifier.height(12.dp))
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                if (wine.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(wine.photoUri),
                        contentDescription = "Photo de l'étiquette",
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(text = if (wine.name.isNotBlank()) wine.name else "Vin")
                if (!wine.vintage.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Millésime: ${wine.vintage}")
                }
                if (!wine.comment.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Commentaire: ${wine.comment}")
                }
                if (wine.rating != null) {
                    Spacer(Modifier.height(4.dp))
                    StarRating(rating = wine.rating ?: 0f, onRatingChange = null)
                }
                Spacer(Modifier.height(8.dp))
                Text("Emplacement: Ligne ${wine.row + 1}, Colonne ${wine.col + 1}")
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Retour") }
            Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Modifier") }
        }
        Spacer(Modifier.height(16.dp))
        Text("Déplacer vers…")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rowState.intValue.toString(),
                onValueChange = { s -> rowState.intValue = s.filter { it.isDigit() }.toIntOrNull() ?: rowState.intValue },
                label = { Text("Ligne") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = colState.intValue.toString(),
                onValueChange = { s -> colState.intValue = s.filter { it.isDigit() }.toIntOrNull() ?: colState.intValue },
                label = { Text("Colonne") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onMove(rowState.intValue, colState.intValue) }, modifier = Modifier.fillMaxWidth()) { Text("Déplacer") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { showConfirm.value = true }, modifier = Modifier.fillMaxWidth()) { Text("Supprimer") }

        if (showConfirm.value) {
            AlertDialog(
                onDismissRequest = { showConfirm.value = false },
                confirmButton = {
                    Button(onClick = { showConfirm.value = false; onDelete() }) { Text("Confirmer") }
                },
                dismissButton = {
                    Button(onClick = { showConfirm.value = false }) { Text("Annuler") }
                },
                title = { Text("Supprimer ce vin ?") },
                text = { Text("Cette action est irréversible.") }
            )
        }
    }
}

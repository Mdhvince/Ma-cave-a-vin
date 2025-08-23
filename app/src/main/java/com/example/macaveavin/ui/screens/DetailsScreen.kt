package com.example.macaveavin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.macaveavin.data.Wine
import com.example.macaveavin.data.WineType
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
    val showConfirm = remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Fiche du vin") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour") } }
        )
        Spacer(Modifier.height(12.dp))
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Column(Modifier.padding(12.dp)) {
                if (wine.photoUri != null) {
                    SubcomposeAsyncImage(
                        model = wine.photoUri,
                        contentDescription = "Photo de l'étiquette",
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(text = if (wine.name.isNotBlank()) wine.name else "Vin", style = MaterialTheme.typography.headlineSmall)
                if (!wine.vintage.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Millésime : ${wine.vintage}")
                }
                run {
                    val typeLabel = when (wine.type) {
                        WineType.RED -> "Rouge"
                        WineType.WHITE -> "Blanc"
                        WineType.ROSE -> "Rosé"
                        WineType.SPARKLING -> "Pétillant"
                        WineType.OTHER -> "Autre"
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Type : $typeLabel")
                }
                if (!wine.comment.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Commentaire : ${wine.comment}")
                }
                if (wine.rating != null) {
                    Spacer(Modifier.height(4.dp))
                    StarRating(rating = wine.rating ?: 0f, onRatingChange = null)
                }
                Spacer(Modifier.height(8.dp))
                Text("Emplacement : Rangée ${wine.row + 1}, Colonne ${wine.col + 1}")
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Modifier") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { showConfirm.value = true }, modifier = Modifier.fillMaxWidth()) { Text("Supprimer") }

        if (showConfirm.value) {
            AlertDialog(
                onDismissRequest = { showConfirm.value = false },
                confirmButton = {
                    Button(onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); showConfirm.value = false; onDelete() }) { Text("Confirmer") }
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

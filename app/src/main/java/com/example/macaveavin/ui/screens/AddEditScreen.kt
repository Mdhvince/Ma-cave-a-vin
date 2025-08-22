package com.example.macaveavin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.Wine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    initialRow: Int,
    initialCol: Int,
    initialWine: Wine?,
    onSave: (Wine) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(initialWine) { mutableStateOf(initialWine?.name ?: "") }
    var vintage by remember(initialWine) { mutableStateOf(initialWine?.vintage ?: "") }
    var comment by remember(initialWine) { mutableStateOf(initialWine?.comment ?: "") }
    var ratingText by remember(initialWine) { mutableStateOf(initialWine?.rating?.toString() ?: "") }
    var photoUri by remember(initialWine) { mutableStateOf(initialWine?.photoUri?.let(Uri::parse)) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text(if (initialWine == null) "Ajouter un vin" else "Modifier le vin") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
            Text(if (photoUri == null) "Ajouter une photo" else "Changer la photo")
        }
        Spacer(Modifier.height(8.dp))
        if (photoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Photo de l'étiquette",
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = vintage,
                onValueChange = { vintage = it },
                label = { Text("Millésime") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ratingText,
                onValueChange = { ratingText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(4) },
                label = { Text("Note (/10)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Commentaire") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(
                onClick = {
                    val rating = ratingText.toFloatOrNull()?.coerceIn(0f, 10f)
                    val wine = Wine(
                        id = initialWine?.id ?: Wine(name = "tmp", row = 0, col = 0).id, // generate UUID if null
                        name = name.ifBlank { "Sans nom" },
                        vintage = vintage.ifBlank { null },
                        comment = comment.ifBlank { null },
                        rating = rating,
                        photoUri = photoUri?.toString(),
                        row = initialWine?.row ?: initialRow,
                        col = initialWine?.col ?: initialCol,
                        createdAt = initialWine?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(wine)
                },
                modifier = Modifier.weight(1f)
            ) { Text("Enregistrer") }
        }
    }
}

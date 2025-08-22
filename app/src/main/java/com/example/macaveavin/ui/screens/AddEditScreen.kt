package com.example.macaveavin.ui.screens

import android.content.Context
import android.graphics.Bitmap
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
import com.example.macaveavin.ui.StarRating
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.Wine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    initialRow: Int,
    initialCol: Int,
    initialWine: Wine?,
    onSave: (Wine) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var photoUri by remember(initialWine) { mutableStateOf(initialWine?.photoUri?.let(Uri::parse)) }
    var name by remember(initialWine) { mutableStateOf(initialWine?.name ?: "") }
    var vintage by remember(initialWine) { mutableStateOf(initialWine?.vintage ?: "") }
    var comment by remember(initialWine) { mutableStateOf(initialWine?.comment ?: "") }
    var rating by remember(initialWine) { mutableStateOf(initialWine?.rating ?: 0f) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            photoUri = saveBitmapToCache(context, bitmap)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text(if (initialWine == null) "Ajouter une bouteille" else "Modifier la bouteille") })
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) { Text("Prendre une photo") }
            Button(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) { Text("Galerie") }
        }
        Spacer(Modifier.height(12.dp))
        if (photoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Photo de l'étiquette",
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du vin") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = vintage, onValueChange = { vintage = it }, label = { Text("Millésime") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text("Note")
        StarRating(rating = rating, onRatingChange = { rating = it })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Commentaire") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(
                onClick = {
                    val wine = Wine(
                        id = initialWine?.id ?: Wine(name = name.ifBlank { "Bouteille" }, row = 0, col = 0).id,
                        name = name.ifBlank { "Bouteille" },
                        vintage = vintage.ifBlank { null } ,
                        comment = comment.ifBlank { null },
                        rating = if (rating <= 0f) null else rating,
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

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "photo_$timeStamp.jpg"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return Uri.fromFile(file)
}

package com.example.macaveavin.ui.screens

import android.content.Context
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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.macaveavin.ui.StarRating
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.macaveavin.data.Wine
import com.example.macaveavin.data.WineType
import java.io.File
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
    onCancel: () -> Unit,
    allCellars: List<com.example.macaveavin.data.CellarConfig>? = null,
    activeCellarIndex: Int = 0,
    onSelectCellar: ((Int) -> Unit)? = null,
    autoOpenCamera: Boolean = false
) {
    val context = LocalContext.current
    var photoUri by remember(initialWine) { mutableStateOf(initialWine?.photoUri?.let(Uri::parse)) }
    var name by remember(initialWine) { mutableStateOf(initialWine?.name ?: "") }
    var vintage by remember(initialWine) { mutableStateOf(initialWine?.vintage ?: "") }
    var comment by remember(initialWine) { mutableStateOf(initialWine?.comment ?: "") }
    var rating by remember(initialWine) { mutableStateOf(initialWine?.rating ?: 0f) }
    var type by remember(initialWine) { mutableStateOf(initialWine?.type ?: WineType.RED) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri = cameraImageUri
        } else {
            cameraImageUri = null
        }
    }

    // Auto-open camera once when requested from quick action
    var hasAutoLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(autoOpenCamera) {
        if (autoOpenCamera && !hasAutoLaunched) {
            val uri = createImageUri(context)
            cameraImageUri = uri
            hasAutoLaunched = true
            takePicture.launch(uri)
        }
    }

    // Selection state for optional quick add flow
    val hasSelection = allCellars != null && allCellars.isNotEmpty()
    var selectedCellarIndex by remember(allCellars, activeCellarIndex) { mutableStateOf(activeCellarIndex.coerceIn(0, (allCellars?.lastIndex ?: 0))) }
    val currentConfig = if (hasSelection) allCellars!![selectedCellarIndex] else null
    var selRow by remember(initialWine, initialRow, currentConfig) { mutableStateOf(initialWine?.row ?: initialRow) }
    var selCol by remember(initialWine, initialCol, currentConfig) { mutableStateOf(initialWine?.col ?: initialCol) }

    fun clampPosition() {
        currentConfig?.let { cfg ->
            selRow = selRow.coerceIn(0, (cfg.rows - 1).coerceAtLeast(0))
            selCol = selCol.coerceIn(0, (cfg.cols - 1).coerceAtLeast(0))
        }
    }

    Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        TopAppBar(title = { Text(if (initialWine == null) "Ajouter une bouteille" else "Modifier la bouteille") })
        Spacer(Modifier.height(12.dp))
        // Direct actions: fewer taps, better UX
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = com.example.macaveavin.R.string.gallery))
            }
            Button(onClick = {
                val uri = createImageUri(context)
                cameraImageUri = uri
                takePicture.launch(uri)
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = com.example.macaveavin.R.string.camera))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (photoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Photo de l'étiquette",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
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
        Text("Type de vin")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.FilterChip(
                selected = type == WineType.RED,
                onClick = { type = WineType.RED },
                label = { Text("Rouge") }
            )
            androidx.compose.material3.FilterChip(
                selected = type == WineType.WHITE,
                onClick = { type = WineType.WHITE },
                label = { Text("Blanc") }
            )
            androidx.compose.material3.FilterChip(
                selected = type == WineType.ROSE,
                onClick = { type = WineType.ROSE },
                label = { Text("Rosé") }
            )
        }
        Spacer(Modifier.height(8.dp))
        if (hasSelection) {
            Text("Cave et emplacement")
            Spacer(Modifier.height(8.dp))
            var cellarMenuExpanded by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { cellarMenuExpanded = true }) {
                    Text(currentConfig?.name ?: "")
                }
                DropdownMenu(expanded = cellarMenuExpanded, onDismissRequest = { cellarMenuExpanded = false }) {
                    allCellars!!.forEachIndexed { idx, cfg ->
                        DropdownMenuItem(text = { Text(cfg.name) }, onClick = {
                            selectedCellarIndex = idx
                            onSelectCellar?.invoke(idx)
                            clampPosition()
                            cellarMenuExpanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = (selRow + 1).toString(),
                    onValueChange = { v ->
                        val n = v.filter { it.isDigit() }.toIntOrNull()
                        if (n != null) { selRow = (n - 1).coerceAtLeast(0); clampPosition() }
                    },
                    label = { Text("Ligne (1-${currentConfig?.rows ?: 1})") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = (selCol + 1).toString(),
                    onValueChange = { v ->
                        val n = v.filter { it.isDigit() }.toIntOrNull()
                        if (n != null) { selCol = (n - 1).coerceAtLeast(0); clampPosition() }
                    },
                    label = { Text("Colonne (1-${currentConfig?.cols ?: 1})") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Commentaire") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text("Note")
        StarRating(rating = rating, onRatingChange = { rating = it })
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
                        type = type,
                        photoUri = photoUri?.toString(),
                        row = initialWine?.row ?: if (hasSelection) selRow else initialRow,
                        col = initialWine?.col ?: if (hasSelection) selCol else initialCol,
                        createdAt = initialWine?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(wine)
                },
                modifier = Modifier.weight(1f)
            ) { Text("Enregistrer") }
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "photo_$timeStamp.jpg"
    val file = File(context.cacheDir, fileName)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

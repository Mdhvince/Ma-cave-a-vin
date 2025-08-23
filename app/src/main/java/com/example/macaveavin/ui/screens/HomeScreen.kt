package com.example.macaveavin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.macaveavin.R
import com.example.macaveavin.data.CellarConfig
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    cellars: List<CellarConfig>,
    onOpenCellar: (index: Int) -> Unit,
    onAddCellar: () -> Unit,
    onQuickAdd: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onDeleteCellar: ((index: Int) -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current
    val showConfirm = remember { mutableStateOf<Int?>(null) }
    val pullState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(
        state = pullState,
        onRefresh = { onRefresh?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: App title and wine glass icon
            Text(
                text = "Ma Cave à Vin",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_wine_glass),
                contentDescription = "Illustration verre de vin",
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(24.dp))

            // List: full-width rectangular items of existing caves
            cellars.forEachIndexed { index, cfg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .animateContentSize()
                        .combinedClickable(
                            onClick = { onOpenCellar(index) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showConfirm.value = index
                            }
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.WineBar, contentDescription = null)
                            Text(text = cfg.name, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Add new cellar primary action: distinct filled tonal button
            androidx.compose.material3.FilledTonalButton(
                onClick = onAddCellar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .animateContentSize()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = "Ajouter une cave", style = MaterialTheme.typography.titleMedium)
            }

            if (onQuickAdd != null) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onQuickAdd, modifier = Modifier.fillMaxWidth()) { Text("Ajouter une bouteille") }
            }
        }

        // Confirmation dialog
        val pending = showConfirm.value
        if (pending != null && onDeleteCellar != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConfirm.value = null },
                confirmButton = {
                    Button(onClick = {
                        onDeleteCellar.invoke(pending)
                        showConfirm.value = null
                    }) { Text("Confirmer") }
                },
                dismissButton = {
                    Button(onClick = { showConfirm.value = null }) { Text("Annuler") }
                },
                title = { Text("Supprimer cette cave ?") },
                text = { Text("Cette action est irréversible.") }
            )
        }
    }
}

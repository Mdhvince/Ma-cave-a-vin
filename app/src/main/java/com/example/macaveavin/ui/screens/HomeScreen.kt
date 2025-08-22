package com.example.macaveavin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.macaveavin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cellarName: String,
    onOpenCellar: () -> Unit,
    onOpenSetup: () -> Unit,
    onQuickAdd: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(title = { Text("Ma Cave à Vin") })
        Spacer(Modifier.height(24.dp))
        Image(painter = painterResource(id = R.drawable.ic_wine_glass), contentDescription = null)
        Spacer(Modifier.height(12.dp))
        Text("Bienvenue dans la cave \"$cellarName\" !")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenCellar, modifier = Modifier.fillMaxWidth()) { Text("Voir la cave") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOpenSetup, modifier = Modifier.fillMaxWidth()) { Text("Configurer la cave") }
        if (onQuickAdd != null) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onQuickAdd, modifier = Modifier.fillMaxWidth()) { Text("Ajouter une bouteille") }
        }
    }
}

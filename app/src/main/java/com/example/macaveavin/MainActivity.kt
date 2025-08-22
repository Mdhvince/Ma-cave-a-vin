package com.example.macaveavin

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.macaveavin.ui.screens.AddEditScreen
import com.example.macaveavin.ui.screens.CellarScreen
import com.example.macaveavin.ui.screens.DetailsScreen
import com.example.macaveavin.ui.screens.SetupScreen
import com.example.macaveavin.ui.screens.HomeScreen
import com.example.macaveavin.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val nav = rememberNavController()
            val vm: AppViewModel = viewModel()

            NavHost(navController = nav, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        onOpenCellar = { nav.navigate("cellar") },
                        onOpenSetup = { nav.navigate("setup") }
                    )
                }
                composable("setup") {
                    val cfg by vm.config.collectAsState()
                    SetupScreen(
                        config = cfg,
                        onSelectPreset = { r, c -> vm.setConfig(r, c) },
                        onContinue = { nav.navigate("cellar") }
                    )
                }
                composable("cellar") {
                    val wines by vm.wines.collectAsState()
                    val cfg by vm.config.collectAsState()
                    val query by vm.query.collectAsState()
                    CellarScreen(
                        rows = cfg.rows,
                        cols = cfg.cols,
                        wines = wines,
                        query = query,
                        onQueryChange = { vm.query.value = it },
                        onCellClick = { row, col ->
                            val existing = vm.getWineAt(row, col)
                            if (existing == null) nav.navigate("addEdit?row=$row&col=$col")
                            else nav.navigate("details/${existing.id}")
                        },
                        onAdd = { row, col -> nav.navigate("addEdit?row=$row&col=$col") },
                        onMoveWine = { id, r, c -> vm.moveWine(id, r, c) },
                        onOpenSetup = { nav.navigate("setup") }
                    )
                }
                composable(
                    route = "addEdit?row={row}&col={col}&id={id}",
                    arguments = listOf(
                        navArgument("row") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("col") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val row = backStackEntry.arguments?.getInt("row") ?: 0
                    val col = backStackEntry.arguments?.getInt("col") ?: 0
                    val id = backStackEntry.arguments?.getString("id")
                    val wine = id?.let { vm.getWineById(it) }
                    AddEditScreen(
                        initialRow = row,
                        initialCol = col,
                        initialWine = wine,
                        onSave = { saved ->
                            if (wine == null) vm.addWine(saved) else vm.updateWine(saved)
                            nav.popBackStack()
                        },
                        onCancel = { nav.popBackStack() }
                    )
                }
                composable(
                    route = "details/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: return@composable
                    val w = vm.getWineById(id) ?: return@composable
                    DetailsScreen(
                        wine = w,
                        onBack = { nav.popBackStack() },
                        onEdit = { nav.navigate("addEdit?row=${w.row}&col=${w.col}&id=${w.id}") },
                        onMove = { r, c -> vm.moveWine(w.id, r, c) },
                        onDelete = { vm.deleteWine(w.id); nav.popBackStack() }
                    )
                }
            }
        }
    }
}

package com.example.macaveavin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.macaveavin.ui.screens.AddEditScreen
import com.example.macaveavin.ui.screens.CellarScreen
import com.example.macaveavin.ui.screens.DetailsScreen
import com.example.macaveavin.ui.screens.HomeScreen
import com.example.macaveavin.ui.screens.SetupScreen
import com.example.macaveavin.ui.theme.AppTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val vm: AppViewModel = viewModel()
    AppTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val nav = rememberNavController()
            val backStack by nav.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val allCellars by vm.allConfigs.collectAsState()

            val cfg by vm.config.collectAsState()

            val topLevelRoutes = setOf("home", "cellar")
            val showBottomBar = currentRoute in topLevelRoutes

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            val items = listOf("home" to "Accueil", "cellar" to "Cave")
                            val current = backStack?.destination
                            for ((route, label) in items) {
                                val selected = isRouteSelected(current, route)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (route == "home") {
                                            nav.navigate("home") {
                                                popUpTo(nav.graph.findStartDestination().id) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            nav.navigate(route) {
                                                popUpTo(nav.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (route == "home") Icons.Filled.Home else Icons.Filled.Favorite,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (currentRoute == "cellar") {
                        androidx.compose.material3.ExtendedFloatingActionButton(onClick = { vm.addCompartment() }) { Text("Ajouter un compartiment") }
                    }
                }
            ) { paddingValues ->
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(paddingValues)) {
                    composable("home") {
                        val configs by vm.allConfigs.collectAsState()
                        HomeScreen(
                            cellars = configs,
                            onOpenCellar = { index -> vm.setActiveCellar(index); nav.navigate("cellar") },
                            onAddCellar = {
                                nav.navigate("setup?isNew=true")
                            },
                            isRefreshing = vm.isRefreshing.collectAsState().value,
                            onRefresh = { vm.refreshCellars() },
                            onDeleteCellar = { idx -> vm.deleteCellar(idx) }
                        )
                    }
                    composable("cellar") {
                        val wines by vm.wines.collectAsState()
                        val cfg by vm.config.collectAsState()
                        val query by vm.query.collectAsState()
                        CellarScreen(
                            config = cfg,
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
                            onOpenSetup = { nav.navigate("setup") },
                            onAddCompartment = { vm.addCompartment() },
                            onMoveCompartment = { sr, sc, dr, dc -> vm.moveCompartment(sr, sc, dr, dc) }
                        )
                    }
                    composable(
                        route = "setup?isNew={isNew}",
                        arguments = listOf(navArgument("isNew") { type = NavType.BoolType; defaultValue = false })
                    ) { backStackEntry ->
                        val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
                        if (isNew) {
                            var draftConfig by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.example.macaveavin.data.CellarConfig()) }
                            SetupScreen(
                                config = draftConfig,
                                onSetName = { draftConfig = draftConfig.copy(name = it) },
                                onSetRows = { r -> draftConfig = draftConfig.copy(rows = r) },
                                onSetCols = { c -> draftConfig = draftConfig.copy(cols = c) },
                                onContinue = {
                                    vm.addCellar(draftConfig)
                                    nav.navigate("cellar")
                                },
                                onCancel = { nav.popBackStack() }
                            )
                        } else {
                            val cfg2 by vm.config.collectAsState()
                            SetupScreen(
                                config = cfg2,
                                onSetName = { vm.setName(it) },
                                onSetRows = { r -> vm.setConfig(r, cfg2.cols) },
                                onSetCols = { c -> vm.setConfig(cfg2.rows, c) },
                                onContinue = { nav.popBackStack() },
                                onCancel = { nav.popBackStack() }
                            )
                        }
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
}

@Composable
private fun isRouteSelected(destination: NavDestination?, route: String): Boolean {
    if (destination == null) return false
    var current: NavDestination? = destination
    while (current?.parent != null) {
        if (current.route == route) return true
        current = current.parent
    }
    return destination.route == route
}

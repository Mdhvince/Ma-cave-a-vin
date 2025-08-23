package com.example.macaveavin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize simple persistence
        com.example.macaveavin.data.Repository.init(applicationContext)
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
                        val items = listOf(
                            BottomNavItem("home", "Accueil", Icons.Outlined.Home, Icons.Filled.Home),
                            BottomNavItem("quickAdd", "Ajouter", Icons.Outlined.PhotoCamera, Icons.Filled.PhotoCamera),
                            BottomNavItem("cellar", "Cave", Icons.Outlined.Storefront, Icons.Filled.Storefront)
                        )
                        val current = backStack?.destination
                        FloatingBottomBar(
                            items = items,
                            isSelected = { route -> isRouteSelected(current, route) },
                            onItemClick = { route ->
                                if (route == "quickAdd") {
                                    nav.navigate("addEdit?row=0&col=0&select=true&autoCamera=true")
                                } else if (route == "home") {
                                    nav.navigate("home") {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    nav.navigate(route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                },
            ) { paddingValues ->
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(paddingValues)) {
                    composable("home") {
                        val configs by vm.allConfigs.collectAsState()
                        val counts by vm.allCounts.collectAsState()
                        HomeScreen(
                            cellars = configs,
                            wineCounts = counts,
                            onOpenCellar = { index -> vm.setActiveCellar(index); nav.navigate("cellar") },
                            onAddCellar = {
                                nav.navigate("setup?isNew=true")
                            },
                            isRefreshing = vm.isRefreshing.collectAsState().value,
                            onRefresh = { vm.refreshCellars() },
                            onDeleteCellar = { idx -> vm.deleteCellar(idx) },
                            onRenameCellar = { idx, newName -> vm.renameCellar(idx, newName) },
                            onMoveCellar = { from, to -> vm.moveCellar(from, to) }
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
                            onMoveCompartment = { sr, sc, dr, dc -> vm.moveCompartment(sr, sc, dr, dc) },
                            cellars = allCellars,
                            onSelectCellar = { idx -> vm.setActiveCellar(idx) }
                        )
                    }
                    composable(
                        route = "setup?isNew={isNew}",
                        arguments = listOf(navArgument("isNew") { type = NavType.BoolType; defaultValue = false })
                    ) { backStackEntry ->
                        val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
                        if (isNew) {
                            var draftConfig by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.example.macaveavin.data.CellarConfig(name = "")) }
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
                                onCancel = { nav.popBackStack() },
                                onDeleteCurrent = {
                                    vm.deleteActiveCellar()
                                    nav.popBackStack()
                                }
                            )
                        }
                    }
                    composable(
                        route = "addEdit?row={row}&col={col}&id={id}&select={select}&autoCamera={autoCamera}",
                        arguments = listOf(
                            navArgument("row") { type = NavType.IntType; defaultValue = 0 },
                            navArgument("col") { type = NavType.IntType; defaultValue = 0 },
                            navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("select") { type = NavType.BoolType; defaultValue = false },
                            navArgument("autoCamera") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { backStackEntry ->
                        val row = backStackEntry.arguments?.getInt("row") ?: 0
                        val col = backStackEntry.arguments?.getInt("col") ?: 0
                        val id = backStackEntry.arguments?.getString("id")
                        val select = backStackEntry.arguments?.getBoolean("select") ?: false
                        val autoCamera = backStackEntry.arguments?.getBoolean("autoCamera") ?: false
                        val wine = id?.let { vm.getWineById(it) }
                        val configs by vm.allConfigs.collectAsState()
                        val activeCfg by vm.config.collectAsState()
                        val activeIdx = configs.indexOf(activeCfg).let { if (it >= 0) it else 0 }
                        AddEditScreen(
                            initialRow = row,
                            initialCol = col,
                            initialWine = wine,
                            onSave = { saved ->
                                if (wine == null) vm.addWine(saved) else vm.updateWine(saved)
                                nav.popBackStack()
                            },
                            onCancel = { nav.popBackStack() },
                            allCellars = if (select) configs else null,
                            activeCellarIndex = activeIdx,
                            onSelectCellar = { idx -> vm.setActiveCellar(idx) },
                            autoOpenCamera = autoCamera
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

private fun isRouteSelected(destination: NavDestination?, route: String): Boolean {
    if (destination == null) return false
    var current: NavDestination? = destination
    while (current?.parent != null) {
        if (current.route == route) return true
        current = current.parent
    }
    return destination.route == route
}

@Composable
private fun FloatingBottomBar(
    items: List<BottomNavItem>,
    isSelected: (String) -> Boolean,
    onItemClick: (String) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    // Padding from edges and slightly detached from bottom for floating style
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(shape),
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 1.dp,
            shadowElevation = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val selected = isSelected(item.route)
                        val targetScale = if (selected) 1.08f else 1f
                        val scale by animateFloatAsState(targetValue = targetScale, animationSpec = tween(220), label = "iconScale")
                        val targetColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        val iconColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(220), label = "iconColor")

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { onItemClick(item.route) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.filled else item.outlined,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AnimatedVisibility(visible = selected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val outlined: ImageVector,
    val filled: ImageVector
)

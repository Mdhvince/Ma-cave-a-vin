package com.example.macaveavin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macaveavin.data.CellarConfig
import com.example.macaveavin.data.Repository
import com.example.macaveavin.data.Wine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    val config: StateFlow<CellarConfig> = Repository.config
        .stateIn(viewModelScope, SharingStarted.Eagerly, Repository.config.value)

    val wines: StateFlow<List<Wine>> = Repository.wines
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allConfigs: StateFlow<List<CellarConfig>> = Repository.allConfigs
        .stateIn(viewModelScope, SharingStarted.Eagerly, Repository.allConfigs.value)

    val allCounts: StateFlow<List<Int>> = Repository.allCounts
        .stateIn(viewModelScope, SharingStarted.Eagerly, Repository.allCounts.value)

    val query = MutableStateFlow("")

    // Debounced search for performance
    private val debouncedQuery = query.debounce(250)

    val filteredWines: StateFlow<List<Wine>> = combine(wines, debouncedQuery) { list: List<Wine>, q: String ->
        if (q.isBlank()) list else list.filter { wineMatches(it, q) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun wineMatches(w: Wine, q: String): Boolean {
        val t = q.trim().lowercase()
        return w.name.lowercase().contains(t) ||
                (w.vintage?.lowercase()?.contains(t) == true) ||
                (w.comment?.lowercase()?.contains(t) == true)
    }

    // Pull-to-refresh for Home screen (simulated)
    val isRefreshing = MutableStateFlow(false)
    fun refreshCellars() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            delay(600)
            // No-op for now; in the future, sync from persistence/network
            isRefreshing.value = false
        }
    }

    fun setConfig(rows: Int, cols: Int) = Repository.setConfig(rows, cols)

    fun setName(name: String) = Repository.setName(name)

    fun addWine(wine: Wine) = Repository.addWine(wine)
    fun updateWine(w: Wine) = Repository.updateWine(w)
    fun deleteWine(id: String) = Repository.deleteWine(id)
    fun moveWine(id: String, row: Int, col: Int) = Repository.moveWine(id, row, col)
    fun moveCompartment(srcRow: Int, srcCol: Int, dstRow: Int, dstCol: Int) = Repository.moveCompartment(srcRow, srcCol, dstRow, dstCol)

    fun getWineAt(row: Int, col: Int): Wine? = Repository.getWineAt(row, col)
    fun getWineById(id: String): Wine? = Repository.getWineById(id)
    fun isOccupied(row: Int, col: Int): Boolean = Repository.isOccupied(row, col)

    fun setActiveCellar(index: Int) = Repository.setActiveCellar(index)
    fun addCellar(name: String? = null) = Repository.addCellar(name)
    fun addCellar(config: CellarConfig) = Repository.addCellar(config)
    fun deleteCellar(index: Int) = Repository.deleteCellar(index)
    fun deleteActiveCellar() = Repository.deleteActiveCellar()
    fun renameCellar(index: Int, name: String) = Repository.renameCellar(index, name)
    fun moveCellar(from: Int, to: Int) = Repository.moveCellar(from, to)
}

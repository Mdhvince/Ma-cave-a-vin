package com.example.macaveavin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macaveavin.data.CaveShape
import com.example.macaveavin.data.CellarConfig
import com.example.macaveavin.data.Repository
import com.example.macaveavin.data.Wine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel : ViewModel() {
    val config: StateFlow<CellarConfig> = Repository.config
        .stateIn(viewModelScope, SharingStarted.Eagerly, Repository.config.value)

    val wines: StateFlow<List<Wine>> = Repository.wines
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val query = MutableStateFlow("")

    val filteredWines: StateFlow<List<Wine>> = combine(wines, query) { list, q ->
        if (q.isBlank()) list else list.filter { wineMatches(it, q) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun wineMatches(w: Wine, q: String): Boolean {
        val t = q.trim().lowercase()
        return w.name.lowercase().contains(t) ||
                (w.vintage?.lowercase()?.contains(t) == true) ||
                (w.comment?.lowercase()?.contains(t) == true)
    }

    fun setConfig(rows: Int, cols: Int) = Repository.setConfig(rows, cols)

    fun setName(name: String) = Repository.setName(name)
    fun setShape(shape: CaveShape) = Repository.setShape(shape)
    fun addCompartment() = Repository.addCompartment()

    fun addWine(wine: Wine) = Repository.addWine(wine)
    fun updateWine(w: Wine) = Repository.updateWine(w)
    fun deleteWine(id: String) = Repository.deleteWine(id)
    fun moveWine(id: String, row: Int, col: Int) = Repository.moveWine(id, row, col)

    fun getWineAt(row: Int, col: Int): Wine? = Repository.getWineAt(row, col)
    fun getWineById(id: String): Wine? = Repository.getWineById(id)
    fun isOccupied(row: Int, col: Int): Boolean = Repository.isOccupied(row, col)
}

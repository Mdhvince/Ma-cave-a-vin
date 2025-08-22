package com.example.macaveavin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Repository {
    private val _config = MutableStateFlow(CellarConfig())
    val config: StateFlow<CellarConfig> = _config.asStateFlow()

    private val _wines = MutableStateFlow<List<Wine>>(emptyList())
    val wines: StateFlow<List<Wine>> = _wines.asStateFlow()

    fun setConfig(rows: Int, cols: Int) {
        _config.value = CellarConfig(rows.coerceIn(1, 12), cols.coerceIn(1, 12))
        // Remove wines that are out of new bounds
        _wines.value = _wines.value.filter { it.row in 0 until _config.value.rows && it.col in 0 until _config.value.cols }
    }

    fun addWine(wine: Wine) {
        // prevent duplicates in same spot
        if (_wines.value.any { it.row == wine.row && it.col == wine.col }) {
            // replace existing
            _wines.value = _wines.value.map { if (it.row == wine.row && it.col == wine.col) wine.copy(id = it.id, createdAt = it.createdAt) else it }
        } else {
            _wines.value = _wines.value + wine
        }
    }

    fun updateWine(updated: Wine) {
        _wines.value = _wines.value.map { if (it.id == updated.id) updated else it }
    }

    fun deleteWine(id: String) {
        _wines.value = _wines.value.filterNot { it.id == id }
    }

    fun moveWine(id: String, newRow: Int, newCol: Int) {
        val existing = _wines.value.find { it.id == id } ?: return
        // If target is occupied, swap
        val target = _wines.value.find { it.row == newRow && it.col == newCol }
        _wines.value = if (target != null) {
            _wines.value.map {
                when (it.id) {
                    existing.id -> it.copy(row = newRow, col = newCol)
                    target.id -> it.copy(row = existing.row, col = existing.col)
                    else -> it
                }
            }
        } else {
            _wines.value.map { if (it.id == id) it.copy(row = newRow, col = newCol) else it }
        }
    }

    fun getWineAt(row: Int, col: Int): Wine? = _wines.value.find { it.row == row && it.col == col }
    fun getWineById(id: String): Wine? = _wines.value.find { it.id == id }

    fun isOccupied(row: Int, col: Int): Boolean = getWineAt(row, col) != null
}

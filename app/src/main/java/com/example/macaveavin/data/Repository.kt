package com.example.macaveavin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Repository {
    private val _config = MutableStateFlow(CellarConfig())
    val config: StateFlow<CellarConfig> = _config.asStateFlow()

    private val _wines = MutableStateFlow<List<Wine>>(emptyList())
    val wines: StateFlow<List<Wine>> = _wines.asStateFlow()

    // Backward-compatible setter (interpreted as SIMPLE NxN)
    fun setConfig(rows: Int, cols: Int) {
        val size = minOf(rows, cols).coerceIn(1, 12)
        setConfig(_config.value.copy(shape = CaveShape.SIMPLE, simpleSize = size))
    }

    fun setConfig(newConfig: CellarConfig) {
        _config.value = newConfig
        // Remove wines that are out of bounds in new config
        _wines.value = _wines.value.filter { isInBounds(it.row, it.col, newConfig) }
    }

    fun setName(name: String) {
        setConfig(_config.value.copy(name = name))
    }

    fun setShape(shape: CaveShape) {
        val current = _config.value
        val updated = when (shape) {
            CaveShape.SIMPLE -> current.copy(shape = shape, simpleSize = if (current.simpleSize < 1) 4 else current.simpleSize)
            CaveShape.PYRAMID -> current.copy(shape = shape, pyramidBase = if (current.pyramidBase < 1) 5 else current.pyramidBase)
        }
        setConfig(updated)
    }

    fun addCompartment() {
        val current = _config.value
        val updated = when (current.shape) {
            CaveShape.SIMPLE -> current.copy(simpleSize = (current.simpleSize + 1).coerceAtMost(12))
            CaveShape.PYRAMID -> current.copy(pyramidBase = (current.pyramidBase + 1).coerceAtMost(12))
        }
        setConfig(updated)
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
        if (!isInBounds(newRow, newCol, _config.value)) return
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

    private fun isInBounds(row: Int, col: Int, cfg: CellarConfig): Boolean {
        if (row < 0 || col < 0) return false
        return when (cfg.shape) {
            CaveShape.SIMPLE -> row < cfg.simpleSize && col < cfg.simpleSize
            CaveShape.PYRAMID -> {
                // rows: 0..(base-1), columns per row increase from top(1) to bottom(base)
                if (row >= cfg.pyramidBase) return false
                val colsInRow = row + 1
                col < colsInRow
            }
        }
    }
}

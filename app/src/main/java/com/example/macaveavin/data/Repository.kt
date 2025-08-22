package com.example.macaveavin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Repository {
    // Multiple cellars support
    private var _cellars = MutableStateFlow(listOf(Cellar()))
    private var _activeIndex = MutableStateFlow(0)

    // Expose active cellar's config and wines as StateFlows to keep current API stable
    private val _config = MutableStateFlow(_cellars.value[_activeIndex.value].config)
    val config: StateFlow<CellarConfig> = _config.asStateFlow()

    private val _wines = MutableStateFlow(_cellars.value[_activeIndex.value].wines)
    val wines: StateFlow<List<Wine>> = _wines.asStateFlow()

    // For Home screen: list of all cellar configs
    private val _allConfigs = MutableStateFlow(_cellars.value.map { it.config })
    val allConfigs: StateFlow<List<CellarConfig>> = _allConfigs.asStateFlow()

    private fun syncActiveSnapshots() {
        if (_cellars.value.isEmpty()) {
            _cellars.value = listOf(Cellar())
            _activeIndex.value = 0
        }
        val safeIndex = _activeIndex.value.coerceIn(0, _cellars.value.lastIndex)
        _activeIndex.value = safeIndex
        val active = _cellars.value[safeIndex]
        _config.value = active.config
        _wines.value = active.wines
        _allConfigs.value = _cellars.value.map { it.config }
    }

    fun setActiveCellar(index: Int) {
        if (index in _cellars.value.indices) {
            _activeIndex.value = index
            syncActiveSnapshots()
        }
    }

    fun addCellar(name: String? = null) {
        val nextIndex = _cellars.value.size
        val newName = name ?: "Cave ${nextIndex + 1}"
        val newCellar = Cellar(config = CellarConfig(name = newName), wines = emptyList())
        _cellars.value = _cellars.value + newCellar
        _activeIndex.value = nextIndex
        syncActiveSnapshots()
    }

    fun addCellar(config: CellarConfig) {
        val nextIndex = _cellars.value.size
        val newCellar = Cellar(config = config, wines = emptyList())
        _cellars.value = _cellars.value + newCellar
        _activeIndex.value = nextIndex
        syncActiveSnapshots()
    }

    fun deleteCellar(index: Int) {
        if (_cellars.value.size <= 1) return // keep at least one
        if (index !in _cellars.value.indices) return
        val newList = _cellars.value.toMutableList().also { it.removeAt(index) }
        _cellars.value = newList
        if (_activeIndex.value >= newList.size) {
            _activeIndex.value = newList.lastIndex
        }
        syncActiveSnapshots()
    }

    // Setter for active cellar rows/cols; clamps to reasonable bounds
    fun setConfig(rows: Int, cols: Int) {
        val r = rows.coerceIn(1, 24)
        val c = cols.coerceIn(1, 24)
        val current = _config.value
        setConfig(current.copy(rows = r, cols = c))
    }

    fun setConfig(newConfig: CellarConfig) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val filteredWines = current.wines.filter { isInBounds(it.row, it.col, newConfig) }
        val updated = current.copy(config = newConfig, wines = filteredWines)
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = updated }
        syncActiveSnapshots()
    }

    fun setName(name: String) {
        setConfig(_config.value.copy(name = name))
    }

    fun addCompartment() {
        val cfg = _config.value
        val baseSet: Set<Pair<Int, Int>> = cfg.enabledCells ?: buildSet {
            for (r in 0 until cfg.rows) {
                for (c in 0 until cfg.cols) add(r to c)
            }
        }
        // If there are disabled cells within current bounds, enable the first one
        val disabledWithin = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until cfg.rows) {
            for (c in 0 until cfg.cols) {
                val p = r to c
                if (p !in baseSet) disabledWithin.add(p)
            }
        }
        val newCfg = if (disabledWithin.isNotEmpty()) {
            val toEnable = disabledWithin.first()
            cfg.copy(enabledCells = baseSet + toEnable)
        } else {
            // Need to grow bounds but enable only one new cell
            val growCols = cfg.rows <= cfg.cols
            if (growCols) {
                // add a new column, enable only bottom cell of new column
                cfg.copy(cols = cfg.cols + 1, enabledCells = baseSet + (maxOf(0, cfg.rows - 1) to cfg.cols))
            } else {
                // add a new row, enable only first cell of new row
                cfg.copy(rows = cfg.rows + 1, enabledCells = baseSet + (cfg.rows to 0))
            }
        }
        setConfig(newCfg)
    }

    fun addWine(wine: Wine) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val list = current.wines
        if (!isInBounds(wine.row, wine.col, _config.value)) return
        val newList = if (list.any { it.row == wine.row && it.col == wine.col }) {
            list.map { if (it.row == wine.row && it.col == wine.col) wine.copy(id = it.id, createdAt = it.createdAt) else it }
        } else list + wine
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun updateWine(updated: Wine) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val newList = current.wines.map { if (it.id == updated.id) updated else it }
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun deleteWine(id: String) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val newList = current.wines.filterNot { it.id == id }
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun moveWine(id: String, newRow: Int, newCol: Int) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val list = current.wines
        val existing = list.find { it.id == id } ?: return
        if (!isInBounds(newRow, newCol, _config.value)) return
        val target = list.find { it.row == newRow && it.col == newCol }
        val newList = if (target != null) {
            list.map {
                when (it.id) {
                    existing.id -> it.copy(row = newRow, col = newCol)
                    target.id -> it.copy(row = existing.row, col = existing.col)
                    else -> it
                }
            }
        } else {
            list.map { if (it.id == id) it.copy(row = newRow, col = newCol) else it }
        }
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun moveCompartment(srcRow: Int, srcCol: Int, dstRow: Int, dstCol: Int) {
        val cfg = _config.value
        val set = cfg.enabledCells ?: buildSet {
            for (r in 0 until cfg.rows) for (c in 0 until cfg.cols) add(r to c)
        }
        val src = srcRow to srcCol
        val dst = dstRow to dstCol
        if (src !in set) return
        if (dst in set) return
        if (dstRow !in 0 until cfg.rows || dstCol !in 0 until cfg.cols) return
        // Prevent moving if wine occupies either source or destination
        if (isOccupied(srcRow, srcCol) || isOccupied(dstRow, dstCol)) return
        val newSet = set - src + dst
        setConfig(cfg.copy(enabledCells = newSet))
    }

    fun getWineAt(row: Int, col: Int): Wine? = _wines.value.find { it.row == row && it.col == col }
    fun getWineById(id: String): Wine? = _wines.value.find { it.id == id }

    fun isOccupied(row: Int, col: Int): Boolean = getWineAt(row, col) != null

    private fun isInBounds(row: Int, col: Int, cfg: CellarConfig): Boolean {
        if (row < 0 || col < 0) return false
        if (row >= cfg.rows || col >= cfg.cols) return false
        val enabled = cfg.enabledCells
        return enabled?.contains(row to col) ?: true
    }
}

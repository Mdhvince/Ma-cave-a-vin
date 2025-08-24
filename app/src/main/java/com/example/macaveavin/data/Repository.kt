package com.example.macaveavin.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import org.json.JSONArray
import org.json.JSONObject

object Repository {
    // File-based simple persistence
    private var storageFile: File? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        storageFile = File(context.filesDir, "cellars.json")
        loadFromDisk()
        syncActiveSnapshots()
    }

    private fun loadFromDisk() {
        val file = storageFile ?: return
        if (!file.exists()) return
        runCatching {
            val text = file.readText()
            val arr = JSONArray(text)
            val loaded = mutableListOf<Cellar>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val cfgObj = obj.getJSONObject("config")
                val name = cfgObj.optString("name", "Cave 1")
                val rows = cfgObj.optInt("rows", 4)
                val cols = cfgObj.optInt("cols", 4)
                val enabledCellsArray = cfgObj.optJSONArray("enabledCells")
                val enabledCells: Set<Pair<Int, Int>>? = if (enabledCellsArray != null) {
                    buildSet {
                        for (j in 0 until enabledCellsArray.length()) {
                            val p = enabledCellsArray.getJSONArray(j)
                            add(p.getInt(0) to p.getInt(1))
                        }
                    }
                } else null
                val cfg = CellarConfig(name = name, rows = rows, cols = cols, enabledCells = enabledCells)
                val winesArray = obj.optJSONArray("wines") ?: JSONArray()
                val wines = mutableListOf<Wine>()
                for (k in 0 until winesArray.length()) {
                    val w = winesArray.getJSONObject(k)
                    val typeStr = w.optString("type", WineType.RED.name)
                    val type = runCatching { WineType.valueOf(typeStr) }.getOrElse { WineType.RED }
                    wines += Wine(
                        id = w.optString("id"),
                        name = w.optString("name"),
                        vintage = w.optString("vintage").takeIf { it.isNotBlank() },
                        country = w.optString("country").takeIf { it.isNotBlank() },
                        region = w.optString("region").takeIf { it.isNotBlank() },
                        comment = w.optString("comment").takeIf { it.isNotBlank() },
                        rating = if (w.has("rating") && !w.isNull("rating")) w.getDouble("rating").toFloat() else null,
                        type = type,
                        photoUri = w.optString("photoUri").takeIf { it.isNotBlank() },
                        row = w.optInt("row"),
                        col = w.optInt("col"),
                        createdAt = if (w.has("createdAt")) w.optLong("createdAt") else System.currentTimeMillis()
                    )
                }
                loaded += Cellar(config = cfg, wines = wines)
            }
            if (loaded.isNotEmpty()) {
                _cellars.value = loaded
                _activeIndex.value = 0
            }
        }.onFailure {
            // ignore parse errors
        }
    }

    private fun persistSafe() {
        runCatching { saveToDisk() }
    }

    // region Photo persistence helpers
    private fun photosDir(): File? {
        val ctx = appContext ?: return null
        val dir = File(ctx.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun isManagedPhoto(pathOrUri: String): Boolean {
        val dir = photosDir() ?: return false
        return pathOrUri.startsWith(dir.absolutePath)
    }

    private fun extensionForMime(mime: String?): String {
        if (mime == null) return ".jpg"
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        return if (!ext.isNullOrBlank()) ".${ext}" else when {
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            else -> ".jpg"
        }
    }

    private fun persistPhotoForWine(wineId: String, src: String?): String? {
        if (src.isNullOrBlank()) return null
        val ctx = appContext ?: return src // cannot persist without context; keep original
        if (isManagedPhoto(src)) return src
        val dir = photosDir() ?: return src
        return runCatching {
            val uri = Uri.parse(src)
            val mime = ctx.contentResolver.getType(uri)
            val ext = extensionForMime(mime)
            val file = File(dir, "${wineId}_${System.currentTimeMillis()}${ext}")
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Unable to open input stream for $src")
            file.absolutePath
        }.getOrElse { src }
    }

    private fun deleteManagedPhotoIfAny(path: String?) {
        if (path.isNullOrBlank()) return
        if (!isManagedPhoto(path)) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
    // endregion

    private fun saveToDisk() {
        val file = storageFile ?: return
        val list = _cellars.value
        val arr = JSONArray()
        list.forEach { cellar ->
            val cfg = JSONObject().apply {
                put("name", cellar.config.name)
                put("rows", cellar.config.rows)
                put("cols", cellar.config.cols)
                val enabled = cellar.config.enabledCells
                if (enabled != null) {
                    val eArr = JSONArray()
                    enabled.forEach { (r, c) ->
                        eArr.put(JSONArray().put(r).put(c))
                    }
                    put("enabledCells", eArr)
                }
            }
            val winesArr = JSONArray()
            cellar.wines.forEach { w ->
                winesArr.put(JSONObject().apply {
                    put("id", w.id)
                    put("name", w.name)
                    if (w.vintage != null) put("vintage", w.vintage)
                    if (w.country != null) put("country", w.country)
                    if (w.region != null) put("region", w.region)
                    if (w.comment != null) put("comment", w.comment)
                    if (w.rating != null) put("rating", w.rating)
                    put("type", w.type.name)
                    if (w.photoUri != null) put("photoUri", w.photoUri)
                    put("row", w.row)
                    put("col", w.col)
                    put("createdAt", w.createdAt)
                })
            }
            val obj = JSONObject().apply {
                put("config", cfg)
                put("wines", winesArr)
            }
            arr.put(obj)
        }
        file.writeText(arr.toString())
    }

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

    private val _allCounts = MutableStateFlow(_cellars.value.map { it.wines.size })
    val allCounts: StateFlow<List<Int>> = _allCounts.asStateFlow()

    private fun ensureUniqueName(desired: String, ignoreIndex: Int? = null): String {
        val existing = _cellars.value.mapIndexed { idx, c -> idx to c.config.name }
        fun exists(name: String): Boolean = existing.any { (i, n) -> (ignoreIndex == null || i != ignoreIndex) && n.equals(name, ignoreCase = true) }
        if (!exists(desired)) return desired
        var i = 2
        while (true) {
            val candidate = "$desired ($i)"
            if (!exists(candidate)) return candidate
            i++
        }
    }

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
        _allCounts.value = _cellars.value.map { it.wines.size }
        // Persist snapshot (best-effort)
        persistSafe()
    }

    fun setActiveCellar(index: Int) {
        if (index in _cellars.value.indices) {
            _activeIndex.value = index
            syncActiveSnapshots()
        }
    }

    fun addCellar(name: String? = null) {
        val nextIndex = _cellars.value.size
        val base = name ?: "Cave ${nextIndex + 1}"
        val unique = ensureUniqueName(base)
        val newCellar = Cellar(config = CellarConfig(name = unique), wines = emptyList())
        _cellars.value = _cellars.value + newCellar
        _activeIndex.value = nextIndex
        syncActiveSnapshots()
    }

    fun addCellar(config: CellarConfig) {
        if (config.name.isBlank()) return
        val nextIndex = _cellars.value.size
        val unique = ensureUniqueName(config.name)
        val newCellar = Cellar(config = config.copy(name = unique), wines = emptyList())
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
        // Migrate wines into the new bounds/enabled cells so they don't disappear from the UI
        val migrated = migrateWinesForConfig(current.wines, newConfig)
        val updated = current.copy(config = newConfig, wines = migrated)
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = updated }
        syncActiveSnapshots()
    }

    fun setName(name: String) {
        val idx = _activeIndex.value
        val unique = ensureUniqueName(name, ignoreIndex = idx)
        setConfig(_config.value.copy(name = unique))
    }

    fun renameCellar(index: Int, name: String) {
        if (index !in _cellars.value.indices) return
        val unique = ensureUniqueName(name, ignoreIndex = index)
        val current = _cellars.value[index]
        val updated = current.copy(config = current.config.copy(name = unique))
        _cellars.value = _cellars.value.toMutableList().also { it[index] = updated }
        syncActiveSnapshots()
    }

    fun deleteActiveCellar() {
        deleteCellar(_activeIndex.value)
    }

    fun moveCellar(fromIndex: Int, toIndex: Int) {
        val list = _cellars.value
        if (list.isEmpty()) return
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        if (fromIndex == toIndex) return
        val mutable = list.toMutableList()
        val item = mutable.removeAt(fromIndex)
        // Insert at the desired final index. If moving downward to the end,
        // allow appending by using size as the insertion index.
        val target = minOf(toIndex, mutable.size)
        mutable.add(target, item)
        val prevActive = _activeIndex.value
        val newActive = when {
            prevActive == fromIndex -> toIndex
            fromIndex < prevActive && prevActive <= toIndex -> prevActive - 1
            toIndex <= prevActive && prevActive < fromIndex -> prevActive + 1
            else -> prevActive
        }.coerceIn(0, mutable.lastIndex)
        _cellars.value = mutable
        _activeIndex.value = newActive
        syncActiveSnapshots()
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
        // Persist photo if provided
        val persistedPhoto = persistPhotoForWine(wine.id, wine.photoUri)
        val toInsert = wine.copy(photoUri = persistedPhoto)
        val newList = if (list.any { it.row == wine.row && it.col == wine.col }) {
            list.map { if (it.row == wine.row && it.col == wine.col) toInsert.copy(id = it.id, createdAt = it.createdAt) else it }
        } else list + toInsert
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun updateWine(updated: Wine) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val old = current.wines.find { it.id == updated.id }
        // If photo changed, persist new and delete old managed
        val newPhoto = if (updated.photoUri != old?.photoUri) {
            val persisted = persistPhotoForWine(updated.id, updated.photoUri)
            if (persisted != old?.photoUri) deleteManagedPhotoIfAny(old?.photoUri)
            persisted
        } else updated.photoUri
        val patched = updated.copy(photoUri = newPhoto)
        val newList = current.wines.map { if (it.id == patched.id) patched else it }
        _cellars.value = _cellars.value.toMutableList().also { it[idx] = current.copy(wines = newList) }
        syncActiveSnapshots()
    }

    fun deleteWine(id: String) {
        val idx = _activeIndex.value
        val current = _cellars.value[idx]
        val target = current.wines.find { it.id == id }
        // Delete photo file if we manage it
        deleteManagedPhotoIfAny(target?.photoUri)
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

    // Repositions wines to fit within the new configuration bounds and enabled cells.
    // Strategy:
    // 1) Keep wines that are already valid and non-conflicting.
    // 2) For out-of-bounds/disabled/conflicting wines, try the clamped position (min(old, max)).
    // 3) If unavailable, scan forward row-major (then wrap) for the next free valid cell.
    // 4) If there are more wines than available cells, keep the extras unchanged (they won't be visible).
    private fun migrateWinesForConfig(wines: List<Wine>, newCfg: CellarConfig): List<Wine> {
        // Build row-major list of valid cells for the new configuration
        val validCells = buildList {
            for (r in 0 until newCfg.rows) {
                for (c in 0 until newCfg.cols) {
                    val cell = r to c
                    val enabledOk = newCfg.enabledCells?.contains(cell) ?: true
                    if (enabledOk) add(cell)
                }
            }
        }
        val capacity = validCells.size
        if (capacity == 0) return wines // nothing we can do
        val indexOfCell = validCells.withIndex().associate { it.value to it.index }
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val result = mutableListOf<Wine>()

        for (w in wines) {
            val desiredRow = w.row.coerceIn(0, newCfg.rows - 1)
            val desiredCol = w.col.coerceIn(0, newCfg.cols - 1)
            val desired = desiredRow to desiredCol
            var target: Pair<Int, Int>? = null

            if ((indexOfCell[desired] != null) && (desired !in occupied)) {
                target = desired
            } else {
                val start = indexOfCell[desired] ?: 0
                fun scan(from: Int, to: Int): Pair<Int, Int>? {
                    var i = from
                    while (i < to) {
                        val cell = validCells[i]
                        if (cell !in occupied) return cell
                        i++
                    }
                    return null
                }
                target = scan(start, validCells.size) ?: scan(0, start)
            }

            if (target != null) {
                occupied += target
                result += w.copy(row = target.first, col = target.second)
            } else {
                // No available cell (more wines than capacity): keep as-is
                result += w
            }
        }
        return result
    }
}

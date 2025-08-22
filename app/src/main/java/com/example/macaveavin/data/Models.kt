package com.example.macaveavin.data

import java.util.UUID

// Simplified model: rectangular grid with optional sparse enabled cells.
// When enabledCells is null, the entire rows x cols grid is considered enabled.
data class CellarConfig(
    val name: String = "Ma Cave",
    val rows: Int = 4,
    val cols: Int = 4,
    val enabledCells: Set<Pair<Int, Int>>? = null
)

data class Wine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val vintage: String? = null,
    val comment: String? = null,
    val rating: Float? = null,
    val photoUri: String? = null,
    val row: Int,
    val col: Int,
    val createdAt: Long = System.currentTimeMillis()
)

// Represents a single cellar with its configuration and wines
data class Cellar(
    val config: CellarConfig = CellarConfig(),
    val wines: List<Wine> = emptyList()
)

package com.example.macaveavin.data

import java.util.UUID

enum class CaveShape { SIMPLE, PYRAMID }

data class CellarConfig(
    val name: String = "Ma Cave",
    val shape: CaveShape = CaveShape.SIMPLE,
    val simpleSize: Int = 4, // Simple: NxN
    val pyramidBase: Int = 5 // Pyramid: base width
) {
    // Derived values for backward compatibility
    val rows: Int get() = when (shape) {
        CaveShape.SIMPLE -> simpleSize
        CaveShape.PYRAMID -> pyramidBase // number of rows
    }
    val cols: Int get() = when (shape) {
        CaveShape.SIMPLE -> simpleSize
        CaveShape.PYRAMID -> pyramidBase // max columns (base width)
    }
}

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

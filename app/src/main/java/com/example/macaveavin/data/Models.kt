package com.example.macaveavin.data

import java.util.UUID

data class CellarConfig(
    val rows: Int = 4,
    val cols: Int = 4
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

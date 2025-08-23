package com.example.macaveavin

import com.example.macaveavin.data.Repository
import com.example.macaveavin.data.Wine
import com.example.macaveavin.data.WineType
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryMigrationTest {
    @Test
    fun wine_is_migrated_when_shrinking_grid() {
        // Arrange: start from current state, add an isolated cellar and switch to it
        Repository.addCellar("Test Migration")
        val lastIndex = com.example.macaveavin.data.Repository.allConfigs.value.lastIndex
        Repository.setActiveCellar(lastIndex)

        // Wine at top-right of a 4x4 (row 0, col 3)
        val wine = Wine(name = "Test Wine", type = WineType.RED, row = 0, col = 3)
        Repository.addWine(wine)

        // Act: shrink to 3x3
        Repository.setConfig(3, 3)

        // Assert: migrated to (0,2)
        val migrated = Repository.wines.value.first()
        assertEquals(0, migrated.row)
        assertEquals(2, migrated.col)
    }
}

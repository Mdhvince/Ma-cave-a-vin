package com.example.macaveavin.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density

class HexagonShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val hw = w / 2f
        val hQuarter = h / 4f
        val path = Path().apply {
            moveTo(hw, 0f)
            lineTo(w, hQuarter)
            lineTo(w, h - hQuarter)
            lineTo(hw, h)
            lineTo(0f, h - hQuarter)
            lineTo(0f, hQuarter)
            close()
        }
        return Outline.Generic(path)
    }
}

package com.ttop.app.apex.ui.fragments

import androidx.annotation.LayoutRes
import com.ttop.app.apex.R

enum class GridStyle(
    @param:LayoutRes @field:LayoutRes val layoutResId: Int,
    val id: Int
) {
    Circular(R.layout.item_grid_circle, 0),
    Card(R.layout.item_card, 1),
    ColoredCard(R.layout.item_card_color, 2),
    Image(R.layout.image, 3),
    GradientImage(R.layout.item_image_gradient, 4)
}
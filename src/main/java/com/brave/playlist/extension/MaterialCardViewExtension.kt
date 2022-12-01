package com.brave.playlist.extension

import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.ShapeAppearanceModel

fun MaterialCardView.setTopCornersRounded(dp: Number) {
    val shapeAppearanceModel: ShapeAppearanceModel.Builder = ShapeAppearanceModel().toBuilder()
    val cornerSize = CornerSize { return@CornerSize dp.dpToPx }
    shapeAppearanceModel.setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
    shapeAppearanceModel.setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
    this.shapeAppearanceModel = shapeAppearanceModel.build()
}
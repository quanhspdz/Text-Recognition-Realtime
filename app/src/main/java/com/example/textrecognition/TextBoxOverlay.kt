package com.example.textrecognition

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil

class TextBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textRectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private fun getBoxRect(imageRectWidth: Float, imageRectHeight: Float, faceBoundingBox: Rect, isBackCam: Boolean): RectF {
        val scaleX = width.toFloat() / imageRectHeight
        val scaleY = height.toFloat() / imageRectWidth
        val scale = scaleX.coerceAtLeast(scaleY)

        val offsetX = (width.toFloat() - ceil(imageRectHeight * scale)) / 2.0f
        val offsetY = (height.toFloat() - ceil(imageRectWidth * scale)) / 2.0f

        val mappedBox = RectF().apply {
            left = faceBoundingBox.right * scale + offsetX
            top = faceBoundingBox.top * scale + offsetY
            right = faceBoundingBox.left * scale + offsetX
            bottom = faceBoundingBox.bottom * scale + offsetY
        }

        val centerX = width.toFloat() / 2

        return if (isBackCam) {
            mappedBox
        } else {
            mappedBox
                .apply {
                    left = centerX + (centerX - left)
                    right = centerX - (right - centerX)
                }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }
}
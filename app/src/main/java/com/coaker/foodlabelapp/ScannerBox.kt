package com.coaker.foodlabelapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class ScannerBox(context: Context?) : View(context) {
    private val paint: Paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.strokeWidth = 10F

        //center
        val x0: Int = width / 2
        val y0: Int = height / 2
        val dx: Int = width / 3
        val dy: Int = height / 3
        //draw guide box
        canvas.drawRect((x0 - dx).toFloat(), (y0 - dy).toFloat(),
            (x0 + dx).toFloat(), (y0 + dy).toFloat(), paint)
    }
}
package org.robotics.blinkworld.stories.photoeditor.util

import android.graphics.PointF

internal class Vector2D : PointF {
    constructor() : super() {}
    constructor(x: Float, y: Float) : super(x, y) {}

    private fun normalize() {
        val length = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        x /= length
        y /= length
    }

    companion object {
        @JvmStatic
        fun getAngle(vector1: Vector2D, vector2: Vector2D): Float {
            vector1.normalize()
            vector2.normalize()
            val degrees = 180.0 / Math.PI * (Math.atan2(
                vector2.y.toDouble(),
                vector2.x.toDouble()
            ) - Math.atan2(vector1.y.toDouble(), vector1.x.toDouble()))
            return degrees.toFloat()
        }
    }
}
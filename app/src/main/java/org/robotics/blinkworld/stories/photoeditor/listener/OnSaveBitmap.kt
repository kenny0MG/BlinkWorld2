package org.robotics.blinkworld.stories.photoeditor.listener

import android.graphics.Bitmap

interface OnSaveBitmap {
    fun onBitmapReady(saveBitmap: Bitmap?)
    fun onFailure(e: Exception?)
}
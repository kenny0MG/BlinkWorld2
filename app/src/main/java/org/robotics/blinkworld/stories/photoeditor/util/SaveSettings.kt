package org.robotics.blinkworld.stories.photoeditor.util

import android.graphics.Bitmap.CompressFormat
import androidx.annotation.IntRange

class SaveSettings private constructor(builder: Builder) {
    val isTransparencyEnabled: Boolean
    val isClearViewsEnabled: Boolean
    val compressFormat: CompressFormat
    val compressQuality: Int

    init {
        isClearViewsEnabled = builder.mClearViewsEnabled
        isTransparencyEnabled = builder.mTransparencyEnabled
        compressFormat = builder.mCompressFormat
        compressQuality = builder.mCompressQuality
    }

    class Builder {
        var mTransparencyEnabled = true
            private set
        var mClearViewsEnabled = true
            private set
        var mCompressFormat = CompressFormat.PNG
            private set
        var mCompressQuality = 100
            private set

        fun setTransparencyEnabled(transparencyEnabled: Boolean): Builder {
            this.mTransparencyEnabled = transparencyEnabled
            return this
        }

        fun setClearViewsEnabled(clearViewsEnabled: Boolean): Builder {
            this.mClearViewsEnabled = clearViewsEnabled
            return this
        }

        fun setCompressFormat(compressFormat: CompressFormat): Builder {
            this.mCompressFormat = compressFormat
            return this
        }

        fun setCompressQuality(@IntRange(from = 0, to = 100) compressQuality: Int): Builder {
            this.mCompressQuality = compressQuality
            return this
        }

        fun build(): SaveSettings {
            return SaveSettings(this)
        }
    }
}
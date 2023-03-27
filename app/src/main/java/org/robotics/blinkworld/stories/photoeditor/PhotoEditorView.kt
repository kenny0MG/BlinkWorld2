package org.robotics.blinkworld.stories.photoeditor

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import org.robotics.blinkworld.R
import org.robotics.blinkworld.stories.photoeditor.listener.OnSaveBitmap

class PhotoEditorView : RelativeLayout {
    private lateinit var mImgSource: FilterImageView

    constructor(context: Context?) : super(context) { init(null) }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(attrs) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(attrs) }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    @SuppressLint("Recycle")
    private fun init(attrs: AttributeSet?) {
        //Setup image attributes
        mImgSource = FilterImageView(context)
        mImgSource.id = imgSrcId
        mImgSource.scaleType = ImageView.ScaleType.CENTER_CROP
        val imgSrcParam = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        imgSrcParam.addRule(CENTER_IN_PARENT, TRUE)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PhotoEditorView)
            val imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src)
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable)
            }
        }

        //Add image source
        addView(mImgSource, imgSrcParam)
    }

    val source: ImageView
        get() = mImgSource

    fun saveFilter(onSaveBitmap: OnSaveBitmap) {
        onSaveBitmap.onBitmapReady(mImgSource.bitmap)
    }

    companion object {
        private const val imgSrcId = 1
    }
}
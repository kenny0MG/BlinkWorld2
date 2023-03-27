package org.robotics.blinkworld.stories.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.AsyncTask
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresPermission

import org.robotics.blinkworld.R
import org.robotics.blinkworld.stories.photoeditor.listener.MultiTouchListener
import org.robotics.blinkworld.stories.photoeditor.listener.OnPhotoEditorListener
import org.robotics.blinkworld.stories.photoeditor.listener.OnSaveBitmap
import org.robotics.blinkworld.stories.photoeditor.util.BitmapUtil.removeTransparency
import org.robotics.blinkworld.stories.photoeditor.util.SaveSettings
import java.io.File
import java.io.FileOutputStream

class PhotoEditor private constructor(builder: Builder) : MultiTouchListener.OnMultiTouchListener {
    private val mLayoutInflater: LayoutInflater
    private val context: Context
    private val parentView: PhotoEditorView?
    private val imageView: ImageView
    private val deleteView: View?
    private val addedViews: MutableList<View?>
    private val redoViews: MutableList<View?>
    private var mOnPhotoEditorListener: OnPhotoEditorListener? = null
    private val isTextPinchZoomable: Boolean

    init {
        context = builder.context
        parentView = builder.parentView
        imageView = builder.mImageView
        deleteView = builder.mDeleteView
        isTextPinchZoomable = builder.mTextPinchZoomable
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        addedViews = ArrayList()
        redoViews = ArrayList()
    }


    fun addText(textTypeface: Typeface?, text: String?, tag: Int) {
        addText(text, tag)
    }

    fun addText(text: String?, position: Int) {
        val textRootView = layout
        val textInputTv = textRootView.findViewById<TextView>(R.id.tvPhotoEditorText)
        textInputTv.tag = position
        textInputTv.text = text
        val multiTouchListener = multiTouchListener
        multiTouchListener.setOnGestureControl(object : MultiTouchListener.OnGestureControl {
            override fun onClick() {
                val textInput = textInputTv.text.toString()
                val currentTextColor = textInputTv.currentTextColor
                val position = textInputTv.tag as Int
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener!!.onEditTextChangeListener(
                        textRootView,
                        textInput,
                        currentTextColor,
                        position
                    )
                }
            }

            override fun onLongClick() {}
        })
        multiTouchListener.setOnMultiTouchListener(this)
        textRootView.setOnTouchListener(multiTouchListener)
        addViewToParent(textRootView)
    }

    fun editText(view: View, inputText: String?, position: Int) {
        val inputTextView = view.findViewById<TextView>(R.id.tvPhotoEditorText)
        inputTextView.tag = position
        if (addedViews.contains(view) && !TextUtils.isEmpty(inputText)) {
            inputTextView.text = inputText
            parentView!!.updateViewLayout(view, view.layoutParams)
            val i = addedViews.indexOf(view)
            if (i > -1) addedViews[i] = view
        }
    }

    private fun addViewToParent(rootView: View) {
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView!!.addView(rootView, params)
        addedViews.add(rootView)
    }

    private val multiTouchListener: MultiTouchListener
        get() = MultiTouchListener(
            deleteView,
            parentView!!,
            imageView,
            isTextPinchZoomable,
            mOnPhotoEditorListener!!
        )


    private val layout: View
        get() {
            val rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null)
            rootView.tag = "text"
            return rootView
        }

    private fun viewUndo(removedView: View?) {
        if (addedViews.size > 0) {
            if (addedViews.contains(removedView)) {
                parentView!!.removeView(removedView)
                addedViews.remove(removedView)
                redoViews.add(removedView)
            }
        }
    }

    fun clearAllViews() {
        for (i in addedViews.indices) {
            parentView!!.removeView(addedViews[i])
        }
        addedViews.clear()
        redoViews.clear()
    }

    override fun onEditTextClickListener(text: String?, colorCode: Int) {}
    override fun onRemoveViewListener(removedView: View?) {
        viewUndo(removedView)
    }


    interface OnSaveListener {
        fun onSuccess(imagePath: String)
        fun onFailure(exception: Exception)
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    fun saveAsFile(imagePath: String, onSaveListener: OnSaveListener) {
        saveAsFile(imagePath, SaveSettings.Builder().build(), onSaveListener)
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings,
        onSaveListener: OnSaveListener
    ) {
        parentView!!.saveFilter(object : OnSaveBitmap {
            override fun onBitmapReady(saveBitmap: Bitmap?) {
                object : AsyncTask<String?, String?, Exception?>() {
                    override fun onPreExecute() {
                        super.onPreExecute()
                        parentView.isDrawingCacheEnabled = false
                    }

                    @SuppressLint("MissingPermission")
                    protected override fun doInBackground(vararg strings: String?): Exception? {
                        // Create a media file name
                        val file = File(imagePath)
                        return try {
                            val out = FileOutputStream(file, false)
                            parentView.isDrawingCacheEnabled = true
                            val drawingCache =
                                if (saveSettings.isTransparencyEnabled) removeTransparency(
                                    parentView.drawingCache
                                ) else parentView.drawingCache
                            drawingCache.compress(
                                saveSettings.compressFormat,
                                saveSettings.compressQuality,
                                out
                            )
                            out.flush()
                            out.close()
                            null
                        } catch (e: Exception) {
                            e.printStackTrace()
                            e
                        }
                    }

                    override fun onPostExecute(e: Exception?) {
                        super.onPostExecute(e)
                        if (e == null) {
                            //Clear all views if its enabled in save settings
                            if (saveSettings.isClearViewsEnabled) clearAllViews()
                            onSaveListener.onSuccess(imagePath)
                        } else {
                            onSaveListener.onFailure(e)
                        }
                    }
                }.execute()
            }

            override fun onFailure(e: Exception?) {
                onSaveListener.onFailure(e!!)
            }
        })
    }


    @SuppressLint("StaticFieldLeak")
    fun saveAsBitmap(onSaveBitmap: OnSaveBitmap) {
        saveAsBitmap(SaveSettings.Builder().build(), onSaveBitmap)
    }

    @SuppressLint("StaticFieldLeak")
    fun saveAsBitmap(
        saveSettings: SaveSettings,
        onSaveBitmap: OnSaveBitmap
    ) {
        parentView!!.saveFilter(object : OnSaveBitmap {
            override fun onBitmapReady(saveBitmap: Bitmap?) {
                object : AsyncTask<String?, String?, Bitmap?>() {
                    override fun onPreExecute() {
                        super.onPreExecute()
                        parentView.isDrawingCacheEnabled = false
                    }

                    override fun doInBackground(vararg strings: String?): Bitmap? {
                        return run {
                            parentView.isDrawingCacheEnabled = true
                            if (saveSettings.isTransparencyEnabled) removeTransparency(parentView.drawingCache) else parentView.drawingCache
                        }
                    }

                    override fun onPostExecute(bitmap: Bitmap?) {
                        super.onPostExecute(bitmap)
                        if (bitmap != null) {
                            if (saveSettings.isClearViewsEnabled) clearAllViews()
                            onSaveBitmap.onBitmapReady(bitmap)
                        } else {
                            onSaveBitmap.onFailure(Exception("Failed to load the bitmap"))
                        }
                    }
                }.execute()
            }

            override fun onFailure(e: Exception?) {
                onSaveBitmap.onFailure(e)
            }
        })
    }

    fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener) {
        mOnPhotoEditorListener = onPhotoEditorListener
    }

    val isCacheEmpty: Boolean
        get() = addedViews.size == 0 && redoViews.size == 0

    class Builder(val context: Context, val parentView: PhotoEditorView) {
        val mImageView: ImageView = parentView.source
        var mDeleteView: View? = null
        var mTextPinchZoomable = true

        fun setDeleteView(deleteView: View?): Builder {
            this.mDeleteView = deleteView
            return this
        }

        fun setPinchTextScalable(isTextPinchZoomable: Boolean): Builder {
            this.mTextPinchZoomable = isTextPinchZoomable
            return this
        }

        fun build(): PhotoEditor {
            return PhotoEditor(this)
        }
    }
}
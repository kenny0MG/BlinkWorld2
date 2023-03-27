package org.robotics.blinkworld.stories.photoeditor.listener

import android.view.View

interface OnPhotoEditorListener {
    fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int, pos: Int)
}
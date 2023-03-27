package org.robotics.blinkworld.Utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecorator: RecyclerView.ItemDecoration() {
    private var mSpaceHeight = -15

    fun EqualSpaceItemDecoration(mSpaceHeight: Int) {
        this.mSpaceHeight = mSpaceHeight
    }

    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        outRect.bottom = mSpaceHeight
        outRect.top = mSpaceHeight
        outRect.left = mSpaceHeight
        outRect.right = mSpaceHeight
    }
}
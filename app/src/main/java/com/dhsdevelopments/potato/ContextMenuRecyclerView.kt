package com.dhsdevelopments.potato

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View

class ContextMenuRecyclerView : RecyclerView {
    private var contextMenuInfo: RecyclerContextMenuInfo? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View): Boolean {
        val longPressPosition = getChildAdapterPosition(originalView)
        if (longPressPosition >= 0) {
            val longPressId = adapter.getItemId(longPressPosition)
            contextMenuInfo = RecyclerContextMenuInfo(longPressPosition, longPressId)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class RecyclerContextMenuInfo(val position: Int, val id: Long) : ContextMenu.ContextMenuInfo
}

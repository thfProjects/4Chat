package com.thf.chat.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children

class MyLinearLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private val callbacks = mutableMapOf<View, () -> Unit>()

    fun addOnClickOutsideViewCallback(view: View, callback: () -> Unit) {
        callbacks[view] = callback
    }

    fun removeOnclickOutsideViewCallback (view: View) {
        callbacks.remove(view)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            callbacks.forEach { view, callback ->
                val out = IntArray(2)
                view.getLocationOnScreen(out)
                val bounds = Rect(out[0], out[1], out[0] + view.width, out[1] + view.height)
                if (!bounds.contains(it.rawX.toInt(), it.rawY.toInt())) callback()
            }
        }
        return false
    }
}
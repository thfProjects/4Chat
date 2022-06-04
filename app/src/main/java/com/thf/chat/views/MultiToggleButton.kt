package com.thf.chat.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.thf.chat.R


class MultiToggleButton(context: Context, attrs: AttributeSet? = null): LinearLayout(context, attrs), View.OnClickListener {

    private val label: TextView
    private val toggleContent: TextView

    var items = emptyArray<String>()
        set(value) {
            field = value
            if (value.isNotEmpty()){
                selectedItem = 0
                toggleContent.text = value[selectedItem]
                invalidate()
            }else {
                selectedItem = -1
                toggleContent.text = ""
                invalidate()
            }
        }

    var selectedItem = -1
        private set

    private var onToggle: (which: Int) -> Unit = {}

    init {
        inflate(context, R.layout.multi_toggle_button, this)

        label = findViewById(R.id.label)
        toggleContent = findViewById(R.id.toggleContent)

        val metrics: DisplayMetrics = Resources.getSystem().getDisplayMetrics()

        context.theme.obtainStyledAttributes(attrs, R.styleable.MultiToggleButton, 0, 0).apply {
            try {
                val textSize = getDimensionPixelSize(
                    R.styleable.MultiToggleButton_textSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, metrics).toInt()
                ).toFloat()
                label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                toggleContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                label.text = getString(R.styleable.MultiToggleButton_labelText)?:""
            } finally {
                recycle()
            }
        }

        setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        toggle()
    }

    fun onToggle (func: (Int) -> Unit) {
        onToggle = func
    }

    private fun toggle () {
        if (items.isEmpty()) selectedItem = -1
        else {
            if(items.lastIndex != selectedItem) selectedItem ++
            else selectedItem = 0
            toggleContent.text = items[selectedItem]
            onToggle(selectedItem)
            invalidate()
        }
    }
}
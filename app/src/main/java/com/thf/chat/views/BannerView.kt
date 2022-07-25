package com.thf.chat.views

import android.content.Context
import android.graphics.ColorFilter
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.thf.chat.R

class BannerView(context: Context, attrs: AttributeSet? = null): LinearLayout(context, attrs) {

    private val dismissButton: ImageButton
    private val textView: TextView

    var onDismiss: () -> Unit = {}

    init {
        inflate(context, R.layout.banner_view, this)

        dismissButton = findViewById<ImageButton>(R.id.bannerDismissButton)
        textView = findViewById<TextView>(R.id.bannerText)

        dismissButton.setOnClickListener {
            onDismiss()
        }

        context.theme.obtainStyledAttributes(attrs, R.styleable.BannerView, 0, 0).apply {
            try {
                textView.text = getString(R.styleable.BannerView_text)?: ""
                getColorStateList(R.styleable.BannerView_textColor)?.let {
                    textView.setTextColor(it)
                    dismissButton.imageTintList = it
                }
            } finally {
                recycle()
            }
        }
    }

    var text: String
        get() = textView.text.toString()
        set(value) {
            textView.text = value
            invalidate()
        }

    fun onDismiss(func: () -> Unit){
        onDismiss = func
    }
}
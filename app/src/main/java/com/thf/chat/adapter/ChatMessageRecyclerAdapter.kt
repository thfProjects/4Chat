package com.thf.chat.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.thf.chat.R
import com.thf.chat.model.ChatMessage
import com.thf.chat.model.Message
import com.thf.chat.model.WelcomeMessage


class ChatMessageRecyclerAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val CHATTYPE = 0
    private val WELCOMETYPE = 1
    private val CONNLOSTTYPE = 2

    var dataset: List<Message> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onMessageSenderClick: (sender: String) -> Unit = {}

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout = itemView.findViewById<LinearLayout>(R.id.chatMessageLayout)
        val senderTextView = itemView.findViewById<TextView>(R.id.chatMessageSender)
        val contentTextView = itemView.findViewById<TextView>(R.id.chatMessageContent)

        init {
            senderTextView.setOnClickListener {
                onMessageSenderClick((dataset[adapterPosition] as ChatMessage).sender)
            }
        }
    }

    inner class ConnLostViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class WelcomeViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when {
            dataset[position] is ChatMessage -> CHATTYPE
            dataset[position] is WelcomeMessage -> WELCOMETYPE
            else -> CONNLOSTTYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CHATTYPE -> ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item, parent, false))
            WELCOMETYPE -> WelcomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.welcome_view, parent, false))
            else -> ConnLostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.connection_lost_view, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatViewHolder) {

            val message = dataset[position] as ChatMessage

            val senderChanged = position == 0 || dataset[position - 1] !is ChatMessage || message.sender != (dataset[position - 1] as ChatMessage).sender
            val mine = message.mine
            val whisper = message.whisperRecipient != null
            val whisperRecipientChanged = position == 0 || dataset[position - 1] !is ChatMessage || message.whisperRecipient != (dataset[position - 1] as ChatMessage).whisperRecipient

            holder.contentTextView.setBackgroundResource(
                when {
                    (senderChanged || whisperRecipientChanged) && mine -> R.drawable.chat_message_background_top_right
                    (senderChanged || whisperRecipientChanged) && !mine -> R.drawable.chat_message_background_top_left
                    else -> R.drawable.chat_message_background
                }
            )

            holder.senderTextView.visibility = if ((senderChanged || whisperRecipientChanged ) && (!mine || whisper)) View.VISIBLE else View.GONE

            holder.senderTextView.text = when {
                whisper && mine -> "Whispering to ${message.whisperRecipient}"
                whisper && !mine -> "(Whisper) ${message.sender}"
                !mine -> message.sender
                else -> ""
            }

            holder.senderTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = if (mine) GravityCompat.END else GravityCompat.START
            }

            holder.layout.updateLayoutParams<RecyclerView.LayoutParams> {
                topMargin = if (senderChanged || whisperRecipientChanged) holder.itemView.context.resources.getDimension(R.dimen.eight_dp).toInt() else 0
            }

            holder.contentTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = if (mine) GravityCompat.END else GravityCompat.START
            }

            holder.contentTextView.backgroundTintList =
                if (whisper) ColorStateList.valueOf(holder.itemView.context.resources.getColor(R.color.whisper, null)) else null

            holder.contentTextView.text = message.content
        }
        }


    override fun getItemCount(): Int {
        return dataset.size
    }

    @ColorInt
    fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
        val resolvedAttr = resolveThemeAttr(colorAttr)
        // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
        val colorRes = if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return ContextCompat.getColor(this, colorRes)
    }

    fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }
}
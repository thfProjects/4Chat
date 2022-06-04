package com.thf.chat.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.SavedStateHandle
import androidx.recyclerview.widget.RecyclerView
import com.thf.chat.R
import com.thf.chat.model.ChatMessage

class ChatMessageRecyclerAdapter : RecyclerView.Adapter<ChatMessageRecyclerAdapter.ViewHolder>() {

    var dataset: List<ChatMessage> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onMessageSenderClick: (sender: String) -> Unit = {}

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout = itemView.findViewById<LinearLayout>(R.id.chatMessageLayout)
        val senderTextView = itemView.findViewById<TextView>(R.id.chatMessageSender)
        val contentTextView = itemView.findViewById<TextView>(R.id.chatMessageContent)

        init {
            senderTextView.setOnClickListener {
                onMessageSenderClick(dataset[adapterPosition].sender)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val senderChanged = position == 0 || dataset[position].sender != dataset[position - 1].sender
        val mine = dataset[position].mine
        val whisper = dataset[position].whisperRecipient != null
        val whisperRecipientChanged = position == 0 || dataset[position].whisperRecipient != dataset[position - 1].whisperRecipient

        holder.contentTextView.setBackgroundResource(
            when {
                (senderChanged || whisperRecipientChanged) && mine -> R.drawable.chat_message_background_top_right
                (senderChanged || whisperRecipientChanged) && !mine -> R.drawable.chat_message_background_top_left
                else -> R.drawable.chat_message_background
            }
        )

        holder.senderTextView.visibility = if ((senderChanged || whisperRecipientChanged ) && (!mine || whisper)) View.VISIBLE else View.GONE

        holder.senderTextView.setTextColor(
            if (whisper) holder.itemView.context.resources.getColor(R.color.whisper, null)
            else holder.itemView.context.resources.getColor(R.color.purple_500, null)
        )

        holder.senderTextView.text = when {
            whisper && mine -> "Whispering to ${dataset[position].whisperRecipient}"
            whisper && !mine -> "(Whisper) ${dataset[position].sender}"
            !mine -> dataset[position].sender
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

        holder.contentTextView.text = dataset[position].content
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
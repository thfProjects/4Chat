package com.thf.chat.model

import java.util.*

data class ChatMessage(val sender: String, val content: String, val mine: Boolean, val whisperRecipient: String?) {
}
package com.thf.chat.model

enum class Blocked(val title: String) {
    NO("No"), YES("Yes"), ONLY_WHISPERS("Only whispers");

    override fun toString() = title
}
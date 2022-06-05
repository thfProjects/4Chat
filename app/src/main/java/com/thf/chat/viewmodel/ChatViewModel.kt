package com.thf.chat.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.thf.chat.data.ChatClient

class ChatViewModel (application: Application, savedStateHandle: SavedStateHandle): AndroidViewModel(application) {

    private val chatClient = ChatClient(savedStateHandle["username"]?:"Guest")

    var username: String? = null

    init {
        chatClient.onConnect = {
            username = chatClient.username
        }
    }

    val messages = chatClient.messages

    val users = chatClient.users

    val whisperingTo: LiveData<String?> = MutableLiveData(null)

    fun handleSendButtonClick (messageContent: String) {
        if (messageContent != "") chatClient.sendChatMessage(messageContent, whisperingTo.value)
    }

    fun setWhisperingTo(user: String?) {
        whisperingTo.setValue(user)
    }

    override fun onCleared() {
        chatClient.close()
    }

    private fun <T> LiveData<T>.setValue (value: T) {
        (this as MutableLiveData<T>).value = value
    }
}
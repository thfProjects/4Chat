package com.thf.chat.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.thf.chat.data.ChatClient
import com.thf.chat.data.ChatRepository
import com.thf.chat.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class ChatViewModel (application: Application, savedStateHandle: SavedStateHandle): AndroidViewModel(application) {

    private val chatRepository = ChatRepository()

    var username: String? = null

    init {
        chatRepository.startChatService(application, savedStateHandle["username"]?:"Guest")
        chatRepository.onConnect = {
            username = chatRepository.username
        }
    }

    val messages = chatRepository.messages

    val users = chatRepository.users

    val whisperingTo: LiveData<String?> = MutableLiveData(null)

    fun handleSendButtonClick (messageContent: String) {
        if (messageContent != "") chatRepository.sendChatMessage(messageContent, whisperingTo.value)
    }

    fun setWhisperingTo(user: String?) {
        whisperingTo.setValue(user)
    }

    override fun onCleared() {
        chatRepository.stopChatService()
    }

    private fun <T> LiveData<T>.setValue (value: T) {
        (this as MutableLiveData<T>).value = value
    }
}
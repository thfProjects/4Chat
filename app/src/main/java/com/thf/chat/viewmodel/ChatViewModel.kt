package com.thf.chat.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.thf.chat.model.Blocked
import com.thf.chat.SingleLiveEvent
import com.thf.chat.data.ChatClient

class ChatViewModel (application: Application): AndroidViewModel(application) {

    private val chatClient = ChatClient()

    private val usernameAttempt = application.getSharedPreferences("preferences", Context.MODE_PRIVATE).getString("usernameAttempt", "Guest")!!

    private lateinit var username: String

    init {
        chatClient.connect(usernameAttempt)
        chatClient.onConnect = {
            chatClient.username?.let { username = it }
        }
    }

    val messages = chatClient.messages

    val users = chatClient.users

    val whisperingTo: LiveData<String?> = MutableLiveData(null)

    val showUserActionDialogCommand: LiveData<String> = SingleLiveEvent()

    val newChatButtonClicked: LiveData<Boolean> = MutableLiveData(false)

    fun handleNewChatButtonClick () {
        if (newChatButtonClicked.value == true) {
            newChatButtonClicked.setValue(false)
            chatClient.close()
            chatClient.connect(usernameAttempt)
        }else
            newChatButtonClicked.setValue(true)
    }

    fun handleClickOutsideNewChatButton () {
        newChatButtonClicked.setValue(false)
    }

    fun handleSendButtonClick (messageContent: String) {
        if (messageContent != "") chatClient.sendChatMessage(messageContent, whisperingTo.value)
    }

    fun setWhisperingTo(user: String?) {
        whisperingTo.setValue(user)
    }

    fun setBlocked (user: String, blocked: Blocked) {
        chatClient.setBlocked(user, blocked)
    }

    fun getBlocked (user: String) = chatClient.getBlocked(user)

    fun handleUserClick (user: String) {
        if (user != username) showUserActionDialogCommand.fire(user)
    }

    fun handleMessageSenderClick (user: String) {
        if (user != username) showUserActionDialogCommand.fire(user)
    }

    override fun onCleared() {
        chatClient.close()
    }

    private fun <T> LiveData<T>.setValue (value: T) {
        (this as MutableLiveData<T>).value = value
    }

    private fun <T> LiveData<T>.fire(value: T) {
        (this as MutableLiveData<T>).value = value
    }
}
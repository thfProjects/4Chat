package com.thf.chat.data

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.thf.chat.model.ChatMessage

class ChatRepository () {

    private var chatService: ChatService? = null

    var onConnect: () -> Unit = {}

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ChatService.ChatServiceBinder
            chatService = binder.getService()
            messages.addSource(chatService!!.messages)
            users.addSource(chatService!!.users)
            chatService!!.onConnect = {
                username = chatService!!.username
                onConnect()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

    fun startChatService (application: Application, usernameAttempt: String) {
        val intent = Intent(application, ChatService::class.java)
        intent.putExtra("usernameAttempt", usernameAttempt)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        }else
            application.startService(intent)

        application.bindService(intent, connection, 0)
    }

    val messages: LiveData<List<ChatMessage>> = MediatorLiveData()

    val users: LiveData<List<String>> = MediatorLiveData()

    var username: String? = null

    fun sendChatMessage (messageContent: String, whisperingTo: String? = null) {
        chatService?.sendChatMessage(messageContent, whisperingTo)
    }

    fun stopChatService() {
        chatService?.close()
    }

    private fun <T> LiveData<T>.addSource(@NonNull source: LiveData<T>) {
        (this as MediatorLiveData).addSource(source) {
            this.value = it
        }
    }
}
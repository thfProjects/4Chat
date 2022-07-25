package com.thf.chat.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.thf.chat.model.*
import okhttp3.*

class ChatClient() {

    var username: String? = null

    val messages: LiveData<List<Message>> = MutableLiveData(ArrayList())

    val users: LiveData<List<String>> = MutableLiveData(ArrayList())

    var onConnect: () -> Unit = {}

    private val blockedList = mutableSetOf<String>()

    private val blockedWhisperList = mutableSetOf<String>()

    private val webSocketListener = object: WebSocketListener() {

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)

            val gson = Gson()
            val message = gson.fromJson(text, JsonObject::class.java)
            when(message.get("type").asString){
                "greeting" -> {
                    username = message["username"].asString
                    users.addAll(gson.fromJson(message["users"], (object : TypeToken<List<String>>(){}).type))
                    messages.add(WelcomeMessage())
                    onConnect()
                }
                "userJoined" ->
                    users.add(message["username"].asString)
                "userLeft" ->
                    users.remove(message["username"].asString)
                "chat" -> {
                    message.remove("type")
                    if (message["sender"].asString == username) message.addProperty("mine", true)
                    else message.addProperty("mine", false)
                    val chatMessage = gson.fromJson(message, ChatMessage::class.java)
                    if (chatMessage.sender !in blockedList && !(chatMessage.whisperRecipient == username && chatMessage.sender in blockedWhisperList))
                        messages.add(chatMessage)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)

            messages.add(ConnectionLostMessage())
        }
    }

    private val client = OkHttpClient()

    private lateinit var webSocket: WebSocket

    fun connect(usernameAttempt: String) {
        messages.clear()
        users.clear()
        val request: Request = Request.Builder().url("ws://192.168.1.3:8080/chatwebsocket/${usernameAttempt}").build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendChatMessage (messageContent: String, whisperingTo: String? = null) {
        val message = JsonObject().apply {
            addProperty("content", messageContent)
            whisperingTo?.let { addProperty("whisperingTo", it) }
        }
        webSocket.send(message.toString())
    }

    fun setBlocked (user: String, blocked: Blocked) {
        when (blocked) {
            Blocked.NO -> {
                blockedList.remove(user)
                blockedWhisperList.remove(user)
            }
            Blocked.YES -> {
                blockedList.add(user)
                blockedWhisperList.remove(user)
            }
            Blocked.ONLY_WHISPERS -> {
                blockedList.remove(user)
                blockedWhisperList.add(user)
            }
        }
    }

    fun getBlocked (user: String): Blocked {
        return when (user) {
            in blockedList -> Blocked.YES
            in blockedWhisperList -> Blocked.ONLY_WHISPERS
            else -> Blocked.NO
        }
    }

    fun close () {
        webSocket.close(1000, null)
    }

    private fun <T> LiveData<List<T>>.add (item: T) {
        val items = this.value as ArrayList<T>
        items.add(item)
        (this as MutableLiveData<List<T>>).postValue(items)
    }

    private fun <T> LiveData<List<T>>.addAll (list: List<T>) {
        val items = this.value as ArrayList<T>
        items.addAll(list)
        (this as MutableLiveData<List<T>>).postValue(items)
    }

    private fun <T> LiveData<List<T>>.remove (item: T) {
        val items = this.value as ArrayList<T>
        items.remove(item)
        (this as MutableLiveData<List<T>>).postValue(items)
    }

    private fun <T> LiveData<List<T>>.clear () {
        val items = this.value as ArrayList<T>
        items.clear()
        (this as MutableLiveData<List<T>>).postValue(items)
    }
}
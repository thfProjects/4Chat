package com.thf.chat.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.thf.chat.model.ChatMessage
import okhttp3.*

class ChatClient(usernameAttempt: String) {

    var roomId: Int? = null
    var username: String? = null

    val messages: LiveData<List<ChatMessage>> = MutableLiveData(ArrayList())

    val users: LiveData<List<String>> = MutableLiveData(ArrayList())

    var onConnect: () -> Unit = {}

    private val webSocketListener = object: WebSocketListener() {

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)

            val gson = Gson()
            val message = gson.fromJson(text, JsonObject::class.java)
            when(message.get("type").asString){
                "greeting" -> {
                    roomId = message["roomId"].asInt
                    username = message["username"].asString
                    users.setValue(gson.fromJson(message["users"], (object : TypeToken<List<String>>(){}).type))
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
                    messages.add(gson.fromJson(message, ChatMessage::class.java))
                }
            }
        }
    }

    private val client = OkHttpClient()

    private val request: Request = Request.Builder().url("ws://192.168.1.3:8080/chatwebsocket/${usernameAttempt}").build()

    private val webSocket = client.newWebSocket(request, webSocketListener)

    fun sendChatMessage (messageContent: String, whisperingTo: String? = null) {
        val message = JsonObject().apply {
            addProperty("content", messageContent)
            addProperty("roomId", roomId)
            whisperingTo?.let { addProperty("whisperingTo", it) }
        }
        webSocket.send(message.toString())
    }

    fun close () {
        webSocket.close(1000, null)
    }

    private fun <T> LiveData<T>.setValue (value: T) {
        (this as MutableLiveData<T>).postValue(value)
    }

    private fun <T> LiveData<List<T>>.add (item: T) {
        val items = this.value as ArrayList<T>
        items.add(item)
        (this as MutableLiveData<List<T>>).postValue(items)
    }

    private fun <T> LiveData<List<T>>.remove (item: T) {
        val items = this.value as ArrayList<T>
        items.remove(item)
        (this as MutableLiveData<List<T>>).postValue(items)
    }
}
package com.thf.chat.data

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.thf.chat.MainActivity
import com.thf.chat.R
import com.thf.chat.model.ChatMessage
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class ChatService: Service() {

    private val NOTIFICATION_ID = 8888
    private val CHANNEL_ID = "ChatChannel"
    private val CHANNEL_NAME = "ChatChannel"

    private val binder = ChatServiceBinder()

    var roomId: Int? = null
    var username: String? = null

    var onConnect: () -> Unit = {}

    val messages: LiveData<List<ChatMessage>> = MutableLiveData(ArrayList())

    val users: LiveData<List<String>> = MutableLiveData(ArrayList())

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

    private lateinit var webSocket: WebSocket

    fun connect (usernameAttempt: String) {
        val request: Request = Request.Builder().url("ws://192.168.1.3:8080/chatwebsocket/${usernameAttempt}").build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendChatMessage (messageContent: String, whisperingTo: String? = null) {
        val message = JsonObject().apply {
            addProperty("content", messageContent)
            addProperty("roomId", roomId)
            whisperingTo?.let { addProperty("whisperingTo", it) }
        }
        webSocket.send(message.toString())
    }

    fun close () {
        webSocket.close(1000, "")
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        connect(intent?.getStringExtra("usernameAttempt")?:"Guest")

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Chat running")
            .setContentText("Chat running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
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

    inner class ChatServiceBinder : Binder() {
        fun getService() = this@ChatService
    }
}
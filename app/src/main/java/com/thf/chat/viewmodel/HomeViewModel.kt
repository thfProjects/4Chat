package com.thf.chat.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thf.chat.SingleLiveEvent

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    val usernameAttempt = sharedPreferences.getString("usernameAttempt", "")

    val invalidUsername: LiveData<Boolean> = MutableLiveData(false)

    val navToChatCommand: LiveData<Unit> = SingleLiveEvent()

    fun handleStartChatButton (usernameAttempt: String) {
        if (isUsernameValid(usernameAttempt)){
            sharedPreferences.edit().apply {
                putString("usernameAttempt", usernameAttempt)
                apply()
            }
            invalidUsername.setValue(false)
            navToChatCommand.fire()
        }else
            invalidUsername.setValue(true)

    }

    private fun isUsernameValid (username: String): Boolean {
        return username.matches(Regex("[A-Za-z0-9 ]*"))
    }

    private fun <T> LiveData<T>.setValue (value: T) {
        (this as MutableLiveData<T>).value = value
    }

    private fun LiveData<Unit>.fire () {
        (this as MutableLiveData<Unit>).value = Unit
    }
}
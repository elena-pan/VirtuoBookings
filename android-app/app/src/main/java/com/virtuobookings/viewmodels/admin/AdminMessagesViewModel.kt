package com.virtuobookings.viewmodels.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.virtuobookings.database.User

class AdminMessagesViewModel(): ViewModel() {

    companion object {
        const val TAG = "AdminMessagesViewModel"
    }

    private val _chatClicked = MutableLiveData<User?>()
    val chatClicked: LiveData<User?>
        get() = _chatClicked

    fun navigateToChat(user: User) {
        _chatClicked.value = user
    }

    fun navigateToChatDone() {
        _chatClicked.value = null
    }
}
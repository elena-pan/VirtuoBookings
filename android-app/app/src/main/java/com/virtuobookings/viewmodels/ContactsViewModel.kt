package com.virtuobookings.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.virtuobookings.database.User

class ContactsViewModel : ViewModel() {

    private val _navigateToChat = MutableLiveData<User>()
    val navigateToChat: LiveData<User>
        get() = _navigateToChat

    private val _newChatClicked = MutableLiveData<Boolean>()
    val newChatClicked: LiveData<Boolean>
        get() = _newChatClicked

    fun navigateToChat(user: User) {
        _navigateToChat.value = user
    }

    fun doneNavigatingToChat() {
        _navigateToChat.value = null
    }

    fun searchUsersNewChat() {
        _newChatClicked.value = true
    }

    fun doneNavigatingToSearchUsers() {
        _newChatClicked.value = null
    }
}

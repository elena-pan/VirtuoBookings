package com.virtuobookings.viewmodels.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdminViewModel(): ViewModel() {

    companion object {
        const val TAG = "AdminViewModel"
    }

    private val _messagesClicked = MutableLiveData<Boolean>()
    val messagesClicked: LiveData<Boolean>
        get() = _messagesClicked

    private val _editUsersClicked = MutableLiveData<Boolean>()
    val editUsersClicked: LiveData<Boolean>
        get() = _editUsersClicked

    fun navigateToMessages() {
        _messagesClicked.value = true
    }

    fun navigateToMessagesDone() {
        _messagesClicked.value = null
    }

    fun navigateToEditUsers() {
        _editUsersClicked.value = true
    }

    fun navigateToEditUsersDone() {
        _editUsersClicked.value = null
    }
}
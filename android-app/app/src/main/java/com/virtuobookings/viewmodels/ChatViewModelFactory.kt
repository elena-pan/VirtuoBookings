package com.virtuobookings.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.virtuobookings.database.User

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class ChatViewModelFactory(
    private val toUser: User,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(toUser, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.virtuobookings.viewmodels.admin

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.virtuobookings.database.User

class AdminChatViewModelFactory(
    private val toUser: User,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminChatViewModel::class.java)) {
            return AdminChatViewModel(toUser, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
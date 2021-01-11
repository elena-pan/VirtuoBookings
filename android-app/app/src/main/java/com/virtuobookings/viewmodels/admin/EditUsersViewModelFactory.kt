package com.virtuobookings.viewmodels.admin

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditUsersViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditUsersViewModel::class.java)) {
            return EditUsersViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
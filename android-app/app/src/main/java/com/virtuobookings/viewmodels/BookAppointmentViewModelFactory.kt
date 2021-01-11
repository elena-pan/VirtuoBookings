package com.virtuobookings.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.virtuobookings.database.User

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class BookAppointmentViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookAppointmentViewModel::class.java)) {
            return BookAppointmentViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
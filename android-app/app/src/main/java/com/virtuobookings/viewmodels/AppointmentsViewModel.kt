package com.virtuobookings.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.DataStatus
import com.virtuobookings.util.FIREBASE_APPOINTMENTS_PATH
import com.virtuobookings.util.isNetworkConnected

class AppointmentsViewModel(private val application: Application): ViewModel() {

    companion object {
        const val TAG = "AppointmentsViewModel"
    }

    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    private val _bookAppointment = MutableLiveData<Boolean>()
    val bookAppointment: LiveData<Boolean>
        get() = _bookAppointment

    fun cancelAppointment(appointment: Appointment) {
        // If no internet, display error snackbar
        if (!isNetworkConnected(
                application.applicationContext
            )
        ) {
            _status.value = DataStatus.NO_INTERNET
            return
        }

        val database = FirebaseDatabase.getInstance().reference

        database.child("$FIREBASE_APPOINTMENTS_PATH/${appointment.id}").removeValue()
            .addOnSuccessListener {
                _status.value = DataStatus.SUCCESS
            }
            .addOnFailureListener {
                Log.w(TAG, it.toString())
                _status.value = DataStatus.ERROR
            }
    }

    fun cancelAppointmentDone() {
        _status.value = null
    }

    fun navigateToBookAppointment() {
        _bookAppointment.value = true
    }

    fun navigateToBookAppointmentDone() {
        _bookAppointment.value = null
    }
}
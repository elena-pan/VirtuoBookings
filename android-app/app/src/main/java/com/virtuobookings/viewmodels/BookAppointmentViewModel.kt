package com.virtuobookings.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.util.FIREBASE_APPOINTMENTS_PATH
import com.virtuobookings.util.convertDateTimeToLong
import com.virtuobookings.util.convertLongToLocalDateTime
import com.virtuobookings.util.isNetworkConnected
import java.time.LocalDate

class BookAppointmentViewModel(private val application: Application) : ViewModel() {

    private class Time(val hour: Int, val minute: Int)

    companion object {
        const val TAG = "BookAppointmentViewMode"
        // Initial timeslots of 9am to 5pm
        private val INITIAL_TIMESLOTS = listOf<Time>(
            Time(9, 0),
            Time(10, 0),
            Time(11, 0),
            Time(12, 0),
            Time(13, 0),
            Time(14, 0),
            Time(15, 0),
            Time(16, 0),
            Time(17, 0)
        )
    }

    private val _bookAppointment = MutableLiveData<DataStatus>()
    val bookAppointment: LiveData<DataStatus>
        get() = _bookAppointment

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>>
        get() = _appointments

    private val _openAppointmentSlots = MutableLiveData<List<Appointment>>()
    val openAppointmentSlots: LiveData<List<Appointment>>
        get() = _openAppointmentSlots

    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    init {
        _status.value = DataStatus.SUCCESS
        getAllAppointments()
    }

    fun bookAppointment(appointment: Appointment, user: User) {
        // If no internet
        if (!isNetworkConnected(application.applicationContext)) {
            _status.value = DataStatus.NO_INTERNET
            _bookAppointment.value = DataStatus.NO_INTERNET
            return
        }
        _status.value = DataStatus.LOADING

        val database = FirebaseDatabase.getInstance().reference
        val userId = user.uid
        val appointmentData = Appointment("", userId, appointment.timestamp, null, null)
            .toMap()

        val key = database.child(FIREBASE_APPOINTMENTS_PATH).push().key

        val childUpdates = hashMapOf<String, Any>(
            "$FIREBASE_APPOINTMENTS_PATH/$key" to appointmentData
        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener {
                _status.value = DataStatus.SUCCESS
                _bookAppointment.value = DataStatus.SUCCESS
                generateAppointmentSlotsForDay(convertLongToLocalDateTime(appointment.timestamp).toLocalDate())
            }
            .addOnFailureListener {
                Log.w(TAG, it.toString())
                _status.value = DataStatus.SUCCESS
                _bookAppointment.value = DataStatus.ERROR
            }
    }

    fun doneBookAppointment() {
        _bookAppointment.value = null
    }

    private fun filterAppointmentsByDay(selectedDate: LocalDate): List<Appointment>? {
        return if (_appointments.value == null) {
            null
        } else {
            // Filter fragment_appointments by day
            _appointments.value?.filter {
                selectedDate == convertLongToLocalDateTime(it.timestamp).toLocalDate()
            }
        }
    }

    fun generateAppointmentSlotsForDay(selectedDate: LocalDate) {
        // If no internet
        if (!isNetworkConnected(application.applicationContext)) {
            _status.value = DataStatus.NO_INTERNET
            return
        }

        _status.value = DataStatus.LOADING
        // Get existing fragment_appointments booked for the day
        val filteredAppointments = filterAppointmentsByDay(selectedDate)
        val initialList: List<Appointment> = INITIAL_TIMESLOTS.map {
            Appointment("", "",
                convertDateTimeToLong(selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth, it.hour, it.minute), null, null)
        }
        if (filteredAppointments == null) {
            _openAppointmentSlots.value = initialList
        } else {
            // Convert to a list of timestamps to use contain function
            val timestamps = filteredAppointments.map { it.timestamp }
            _openAppointmentSlots.value = initialList.filter {
                !timestamps.contains(it.timestamp)
            }
        }
        _status.value = null
    }

    private fun getAllAppointments() {

        _status.value = DataStatus.LOADING

        // Set appointment listener for database
        val database = FirebaseDatabase.getInstance().reference
        database.child(FIREBASE_APPOINTMENTS_PATH).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        _status.value = DataStatus.NO_RESULTS
                    }

                    else {
                        val queryResult = dataSnapshot.children

                        // Convert snapshots into User objects
                        _appointments.value = queryResult.mapNotNull {
                            it.getValue(Appointment::class.java).apply {
                                this?.id = it.key!!
                            }
                        }

                        _status.value = if (_appointments.value!!.isEmpty()) DataStatus.NO_RESULTS else DataStatus.SUCCESS
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "getAppointments:onCancelled", databaseError.toException())
                    _status.value = DataStatus.ERROR
                }
            })
    }

}

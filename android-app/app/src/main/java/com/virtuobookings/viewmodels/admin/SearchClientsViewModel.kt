package com.virtuobookings.viewmodels.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.virtuobookings.VirtuoBookingsApplication
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.util.FIREBASE_USERDATA_PATH
import com.virtuobookings.viewmodels.SearchUsersViewModel

class SearchClientsViewModel : ViewModel() {

    companion object {
        const val TAG = "SearchClientsViewModel"
    }

    private val _navigateToBookAppointment = MutableLiveData<User>()
    val navigateToBookAppointment: LiveData<User>
        get() = _navigateToBookAppointment

    private val _users = MutableLiveData<List<User>>()

    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>>
        get() = _filteredUsers

    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    init {
        getAllUsers()
    }

    fun navigateToBookAppointment(user: User) {
        _navigateToBookAppointment.value = user
    }

    fun doneNavigatingToBookAppointment() {
        _navigateToBookAppointment.value = null
    }

    fun doneShowingErrorToast() {
        _status.value = DataStatus.SUCCESS
    }

    private fun getAllUsers() {

        _status.value = DataStatus.LOADING

        // Get user from database
        val database = FirebaseDatabase.getInstance().reference
        database.child(FIREBASE_USERDATA_PATH).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        _status.value = DataStatus.NO_RESULTS
                        _users.value = listOf()
                    }

                    else {
                        val queryResult = dataSnapshot.children

                        // Convert snapshots into User objects, filter for clients
                        _users.value = queryResult
                            .mapNotNull {
                                it.getValue(User::class.java).apply {
                                    this?.uid = it.key!!
                                }
                            }
                            .filter {
                                it.userType == "Client"
                            }
                        _status.value = if (_users.value!!.isEmpty()) DataStatus.NO_RESULTS else DataStatus.SUCCESS
                    }
                    filterUsers(null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(SearchUsersViewModel.TAG, "getUsers:onCancelled", databaseError.toException())
                    _status.value = DataStatus.ERROR
                }
            })
    }

    fun filterUsers(query: String?) {

        _status.value = DataStatus.LOADING
        // Check for substring matches in name, uid, and email
        // Make sure result is not current user
        var filteredUsersTemp: List<User>? = _users.value

        query?.let {
            filteredUsersTemp = filteredUsersTemp?.filter {
                (it.name.contains(query, true) ||
                        it.uid.contains(query, true) ||
                        it.email.contains(query, true)) &&
                        it.uid != VirtuoBookingsApplication.currentUser!!.uid
            }
        }
        _filteredUsers.value = filteredUsersTemp
        _status.value = if (filteredUsersTemp == null) DataStatus.NO_RESULTS else DataStatus.SUCCESS
    }

}

package com.virtuobookings.viewmodels.admin

import android.app.Application
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
import com.virtuobookings.util.isNetworkConnected
import com.virtuobookings.viewmodels.SearchUsersViewModel

class EditUsersViewModel(val application: Application): ViewModel() {

    companion object {
        const val TAG = "EditUsersViewModel"
    }

    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    private val _setAdminStatus = MutableLiveData<DataStatus>()
    val setAdminStatus: LiveData<DataStatus>
        get() = _setAdminStatus

    private val _navigateToChat = MutableLiveData<User>()
    val navigateToChat: LiveData<User>
        get() = _navigateToChat

    private val _navigateToPastAppointments = MutableLiveData<User>()
    val navigateToPastAppointments: LiveData<User>
        get() = _navigateToPastAppointments

    private var userType: String? = null
    private val _users = MutableLiveData<List<User>>()

    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>>
        get() = _filteredUsers

    init {
        getAllUsers()
    }

    fun navigateToChat(user: User) {
        _navigateToChat.value = user
    }

    fun doneNavigatingToChat() {
        _navigateToChat.value = null
    }

    fun doneShowingErrorToast() {
        _status.value = DataStatus.SUCCESS
    }

    fun navigateToPastAppointments(user: User) {
        _navigateToPastAppointments.value = user
    }

    fun doneNavigatingToPastAppointments() {
        _navigateToPastAppointments.value = null
    }

    fun changeAdminStatus(user: User, admin: Boolean) {
        if (!isNetworkConnected(application.applicationContext)) {
            _setAdminStatus.value = DataStatus.NO_INTERNET
            return
        }
        val database = FirebaseDatabase.getInstance().reference
        database.child("$FIREBASE_USERDATA_PATH/${user.uid}/admin").setValue(admin)
            .addOnSuccessListener {
                _setAdminStatus.value = DataStatus.SUCCESS
            }
            .addOnFailureListener {
                _setAdminStatus.value = DataStatus.ERROR
            }
    }

    fun changeAdminStatusDone() {
        _setAdminStatus.value = null
    }

    fun changeUserType(selectedItem: String?) {
        userType = if (selectedItem == "All") null else selectedItem
    }

    fun filterUsers(query: String?) {
        if (!isNetworkConnected(application.applicationContext)) {
            _status.value = DataStatus.NO_INTERNET
            return
        }
        _status.value = DataStatus.LOADING
        // Check for substring matches in name, uid, and email
        // Make sure result is not current user
        var filteredByUserType = when (userType) {
            "Client" -> _users.value?.filter {
                it.userType == "Client"
            }
            "Service Provider" -> _users.value?.filter {
                it.userType == "Service Provider"
            }
            "Staff" -> _users.value?.filter {
                it.userType == "Staff"
            }
            "Admin" -> _users.value?.filter {
                it.admin == true
            }
            else -> _users.value
        }

        query?.let {
            filteredByUserType = filteredByUserType?.filter {
                (it.name.contains(query, true) ||
                        it.uid.contains(query, true) ||
                        it.email.contains(query, true)) &&
                        it.uid != VirtuoBookingsApplication.currentUser!!.uid
            }
        }
        _filteredUsers.value = filteredByUserType
        _status.value = if (filteredByUserType == null) DataStatus.NO_RESULTS else DataStatus.SUCCESS
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

                        // Convert snapshots into User objects
                        _users.value = queryResult.mapNotNull {
                            it.getValue(User::class.java).apply {
                                this?.uid = it.key!!
                            }
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
}
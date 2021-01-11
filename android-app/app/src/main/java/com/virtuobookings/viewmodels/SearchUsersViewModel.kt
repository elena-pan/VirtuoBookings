package com.virtuobookings.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User

class SearchUsersViewModel : ViewModel() {

    companion object {
        const val TAG = "SearchUsersViewModel"
    }

    private val _navigateToChat = MutableLiveData<User>()
    val navigateToChat: LiveData<User>
        get() = _navigateToChat

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() = _users

    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    init {
        _status.value = DataStatus.SUCCESS
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

    fun getSearchResults(query: String) {

        _status.value = DataStatus.LOADING

        // Get user from database
        val database = FirebaseDatabase.getInstance().reference
        database.child("userData").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        _status.value = DataStatus.NO_RESULTS
                    }

                    else {
                        val queryResult = dataSnapshot.children

                        // Convert snapshots into User objects
                        val usersList = queryResult.mapNotNull {
                            it.getValue(User::class.java).apply {
                                this?.uid = it.key!!
                            }
                        }
                        // Check for substring matches in name, uid, and email
                        // Make sure result is not current user
                        _users.value = usersList.filter {
                            (it.name.contains(query, true) ||
                             it.uid.contains(query, true) ||
                             it.email.contains(query, true)) &&
                             it.uid != currentUser!!.uid
                        }

                        _status.value = if (_users.value!!.isEmpty()) DataStatus.NO_RESULTS else DataStatus.SUCCESS
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "getUsers:onCancelled", databaseError.toException())
                    _status.value = DataStatus.ERROR
                }
            })
    }

}

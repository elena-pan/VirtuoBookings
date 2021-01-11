package com.virtuobookings.viewmodels.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.database.User
import com.virtuobookings.util.ADMIN_ID
import com.virtuobookings.util.FIREBASE_ADMIN_LATEST_MESSAGES_PATH
import com.virtuobookings.util.FIREBASE_ADMIN_MESSAGES_PATH
import com.virtuobookings.util.isNetworkConnected

class AdminChatViewModel(private val toUser: User, private val application: Application): ViewModel() {

    companion object {
        const val TAG = "AdminChatViewModel"
    }

    enum class MessageStatus { SUCCESS, NO_INTERNET, ERROR }

    // The internal MutableLiveData that stores if a message has been sent
    private val _messageSent = MutableLiveData<MessageStatus>()

    // The external immutable LiveData
    val messageSent: LiveData<MessageStatus>
        get() = _messageSent

    private var messageText = ""

    fun sendMessage() {
        // If no internet, display error snackbar
        if (!isNetworkConnected(
                application.applicationContext
            )
        ) {
            _messageSent.value = MessageStatus.NO_INTERNET
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val fromId = ADMIN_ID
        val messageData = ChatMessage("", messageText, fromId, toUser.uid, System.currentTimeMillis(), null)
            .toMap()

        val key1 = database.child("$FIREBASE_ADMIN_MESSAGES_PATH/${toUser.uid}").push().key

        val childUpdates = hashMapOf<String, Any>(
            "$FIREBASE_ADMIN_MESSAGES_PATH/${toUser.uid}/$key1" to messageData,
            "$FIREBASE_ADMIN_LATEST_MESSAGES_PATH/${toUser.uid}" to messageData
        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener {
                _messageSent.value = MessageStatus.SUCCESS
            }
            .addOnFailureListener {
                Log.w(TAG, it.toString())
                _messageSent.value = MessageStatus.ERROR
            }
    }

    fun onMessageTextChanged(inputText: String) {
        messageText = inputText
    }

    fun messageSentDone() {
        _messageSent.value = null
    }
}
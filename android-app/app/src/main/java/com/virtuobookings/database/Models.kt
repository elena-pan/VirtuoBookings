package com.virtuobookings.database

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.util.ADMIN_ID
import com.virtuobookings.util.convertLongToDateString
import com.virtuobookings.util.convertLongToTimeString
import kotlinx.android.parcel.Parcelize

enum class DataStatus { SUCCESS, NO_RESULTS, LOADING, ERROR, NO_INTERNET }

@IgnoreExtraProperties
@Parcelize
data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var userType: String = "",
    var admin: Boolean = false
) : Parcelable {

    // Hide client names if current user is service provider, put "Client " + first 5 characters of uid
    val displayName: String
    get() =
        if (currentUser?.userType == "Service Provider" && userType == "Client") {
            "$userType ${uid.substring(0, 5)}"
        }
        else if (uid == ADMIN_ID) {
            name
        }
        else {
            "$name, $userType"
        }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "userType" to userType,
            "admin" to admin
        )
    }
}

data class ChatMessage(
    var id: String,
    var text: String,
    var fromId: String,
    var toId: String,
    var timestamp: Long,
    var isFromCurrentUser: Boolean?) {

    constructor() : this("", "", "", "", -1, null)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "text" to text,
            "timestamp" to timestamp,
            "fromId" to fromId
        )
    }

    val formattedTimestamp
        get() = convertLongToDateString(
            timestamp
        )
}

data class Appointment(
    var id: String = "",
    var clientUid: String = "",
    var timestamp: Long = -1,
    var isFromCurrentUser: Boolean?,
    var isCancelable: Boolean?
) {

    constructor() : this("", "", -1, null, null)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "clientUid" to clientUid,
            "timestamp" to timestamp
        )
    }

    val formattedTimestamp
        get() = convertLongToDateString(
            timestamp
        )

    val formattedTimeOnly
        get() = convertLongToTimeString(timestamp)
}

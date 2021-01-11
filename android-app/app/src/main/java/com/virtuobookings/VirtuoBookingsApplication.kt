package com.virtuobookings

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.virtuobookings.User

/**
 * Override application to setup background work via WorkManager
 */
class VirtuoBookingsApplication : Application() {

    companion object {
        var currentUser: User? = null
    }

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}

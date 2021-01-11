
package com.virtuobookings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.User
import com.virtuobookings.util.FIREBASE_NOTIFICATION_TOKENS
import com.virtuobookings.util.FIREBASE_USERDATA_PATH
import com.virtuobookings.util.hideKeyboard

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            // No user is signed in, redirect to SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MainActivity", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                if (token != null) {
                    val database = FirebaseDatabase.getInstance().reference

                    if (currentUser!!.userType == "Service Provider") {
                        database.child("$FIREBASE_NOTIFICATION_TOKENS/service-providers/$token").setValue(true)
                            .addOnSuccessListener {}
                            .addOnFailureListener {
                                Log.w("MainActivity", it.toString())
                            }
                    }
                    else if (currentUser!!.admin == true) {
                        database.child("$FIREBASE_NOTIFICATION_TOKENS/admin/$token").setValue(true)
                            .addOnSuccessListener {}
                            .addOnFailureListener {
                                Log.w("MainActivity", it.toString())
                            }
                    }

                    database.child("$FIREBASE_USERDATA_PATH/${currentUser!!.uid}/notification-tokens/$token").setValue(true)
                        .addOnSuccessListener {}
                        .addOnFailureListener {
                            Log.w("MainActivity", it.toString())
                        }
                }
            })

        if (currentUser!!.admin) {
            setContentView(R.layout.activity_main_admin)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.contactsFragment,
                    R.id.settingsFragment,
                    R.id.appointmentsFragment,
                    R.id.adminFragment
                )
            )
            navView = findViewById(R.id.nav_view_admin)
            navController = findNavController(R.id.nav_host_fragment_admin)
        } else {
            setContentView(R.layout.activity_main)
            // Set top-level destinations for actionbar
            // Chat fragment should be the only one with an up button
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.contactsFragment,
                    R.id.settingsFragment,
                    R.id.appointmentsFragment
                )
            )
            navView = findViewById(R.id.nav_view)
            navController = findNavController(R.id.nav_host_fragment)
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (intent.extras?.get("Fragment") == "chatFragment") {
            val user: User? = intent.extras?.get("User") as User?
            user?.let {
                val bundle = bundleOf("user" to it, "displayName" to it.displayName)
                findNavController(R.id.nav_host_fragment).navigate(R.id.chatFragment, bundle)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard(this)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

package com.virtuobookings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var database: DatabaseReference
    private val _status = MutableLiveData<DataStatus>()
    val status: LiveData<DataStatus>
        get() = _status

    companion object {
        const val TAG = "SignInActivity"
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _status.value = DataStatus.LOADING
        database = FirebaseDatabase.getInstance().reference

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.lifecycleOwner = this

        if (FirebaseAuth.getInstance().currentUser != null) {
            getUserSignIn()
        }

        _status.value = DataStatus.SUCCESS

        binding.signInButton.setOnClickListener {
            launchSignInFlow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                getUserSignIn()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")

                if (response == null) {
                    // User pressed back button
                    return;
                }

                else if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    val snackbar = Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    return
                }
                else {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Sign-in failed",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                }
            }
        }
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    private fun getUserSignIn() {
        _status.value = DataStatus.LOADING
        // User is already signed in, check if need information update, set user instance
        // Read user data once
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        database.child("userData").child(firebaseUser!!.uid).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.value == null) {
                        currentUser = null
                        // Navigate to registration
                        startActivity(Intent(this@SignInActivity, UserRegistrationActivity::class.java))
                        finish()
                        return
                    } else {
                        currentUser = dataSnapshot.getValue(User::class.java)
                        currentUser!!.uid = dataSnapshot.key!!
                        // Navigate to main activity
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                        finish()
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("signInSuccessful", "getUser:onCancelled", databaseError.toException())
                    currentUser = null
                    startActivity(Intent(this@SignInActivity, UserRegistrationActivity::class.java))
                    finish()
                }
            })
    }
}
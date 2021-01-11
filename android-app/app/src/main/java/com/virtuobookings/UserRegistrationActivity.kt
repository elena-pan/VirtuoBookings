package com.virtuobookings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.User
import com.virtuobookings.databinding.ActivityUserRegistrationBinding

class UserRegistrationActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    companion object {
        const val TAG = "UserRegisterActivity"
    }

    private lateinit var binding: ActivityUserRegistrationBinding
    private lateinit var database: DatabaseReference
    private var firebaseUser: FirebaseUser? = null

    // Default user type is client
    private var userType = "Client"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            // No user is signed in, redirect to SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        binding = ActivityUserRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val spinner: Spinner = binding.spinnerUserType

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.user_type,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this

        binding.submitButton.setOnClickListener {
            newUser(firebaseUser!!.uid, firebaseUser!!.displayName!!, firebaseUser!!.email!!, userType)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        userType = parent.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun newUser(uid: String, name: String, email: String, userType: String) {

        // Uid is always unique - use as key
        val user = User(uid, name, email, userType, false)
        val userValues = user.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/userData/$uid" to userValues
        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener {
                // Navigate to MainActivity
                currentUser = user
                startActivity(Intent(this@UserRegistrationActivity, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Log.w(TAG, it.toString())
                Snackbar.make(binding.root, "An error has occurred. Please try again later.", Snackbar.LENGTH_LONG)
                    .show()
            }
    }

}

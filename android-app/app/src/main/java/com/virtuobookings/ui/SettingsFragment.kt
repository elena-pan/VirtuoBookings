package com.virtuobookings.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.virtuobookings.R
import com.virtuobookings.SignInActivity
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_settings, container, false)

        binding.nameText.text = getString(R.string.settings_name_text, currentUser!!.name)
        binding.usertypeText.text = currentUser!!.userType
        binding.uidText.text = getString(R.string.settings_id_text, currentUser!!.uid)
        binding.emailText.text = getString(R.string.settings_email_text, currentUser!!.email)

        binding.logoutButton.setOnClickListener {
            // Logout user
            AuthUI.getInstance().signOut(requireContext())
                .addOnSuccessListener {
                    currentUser = null
                    // Redirect to SigninActivity
                    startActivity(Intent(activity, SignInActivity::class.java))
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Log.i("SettingsFragment", it.toString())
                }
        }

        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you would like to delete your account? This is permanent and cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().currentUser!!.delete()
                        .addOnSuccessListener {
                            currentUser = null
                            // Redirect to SigninActivity
                            startActivity(Intent(activity, SignInActivity::class.java))
                            requireActivity().finish()
                        }
                        .addOnFailureListener {
                            Log.i("SettingsFragment", it.toString())
                            Snackbar.make(binding.root, "An error has occurred. Please sign out and try again later", Snackbar.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return binding.root
    }
}

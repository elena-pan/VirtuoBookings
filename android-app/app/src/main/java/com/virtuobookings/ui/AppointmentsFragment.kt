package com.virtuobookings.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.virtuobookings.R
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.databinding.FragmentAppointmentsBinding
import com.virtuobookings.util.AppointmentsAdapter
import com.virtuobookings.util.FIREBASE_APPOINTMENTS_PATH
import com.virtuobookings.util.FIREBASE_PAST_APPOINTMENTS_PATH
import com.virtuobookings.viewmodels.AppointmentsViewModel
import com.virtuobookings.viewmodels.AppointmentsViewModelFactory

class AppointmentsFragment: Fragment() {
    companion object {
        const val TAG = "appointmentsFragment"
    }

    private lateinit var binding: FragmentAppointmentsBinding
    private lateinit var database: DatabaseReference

    private val appointmentsViewModel: AppointmentsViewModel by lazy {
        val appointmentsViewModelFactory = AppointmentsViewModelFactory(requireActivity().application)
        ViewModelProvider(this, appointmentsViewModelFactory).get(AppointmentsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_appointments, container, false)
        binding.lifecycleOwner = this
        binding.appointmentsViewModel = appointmentsViewModel

        if (currentUser!!.userType == "Client" || currentUser!!.admin == true) {
            binding.fabBookAppointment.show()
        } else {
            binding.fabBookAppointment.hide()
        }

        database = FirebaseDatabase.getInstance().reference

        appointmentsViewModel.status.observe(viewLifecycleOwner, Observer {
            if ( null != it ) {
                when (it) {
                    // Clear edittext
                    DataStatus.SUCCESS -> Snackbar.make(binding.root, "Appointment canceled", Snackbar.LENGTH_LONG).show()
                    // Show no internet snackbar
                    DataStatus.NO_INTERNET -> Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_LONG).show()
                    // Show error snackbar
                    DataStatus.ERROR -> Snackbar.make(binding.root, "Error canceling appointment", Snackbar.LENGTH_LONG).show()
                }
                appointmentsViewModel.cancelAppointmentDone()
            }
        })

        appointmentsViewModel.bookAppointment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                if (currentUser!!.userType == "Client") {
                    // Navigate to book appointment fragment
                    findNavController().navigate(AppointmentsFragmentDirections.actionAppointmentsFragment2ToBookAppointmentFragment(currentUser!!))
                    appointmentsViewModel.navigateToBookAppointmentDone()
                } else if (currentUser!!.admin == true) {
                    findNavController().navigate(AppointmentsFragmentDirections.actionAppointmentsFragmentToSearchClientsFragment())
                    appointmentsViewModel.navigateToBookAppointmentDone()
                }
            }
        })

        val parser: SnapshotParser<Appointment> = SnapshotParser { dataSnapshot ->
            val appointment = dataSnapshot.getValue(Appointment::class.java)
            if (appointment != null) {
                // Delete past appointments
                val dayInMillis: Long = 1000.toLong() * 60 * 60 * 24
                if ((System.currentTimeMillis() - appointment.timestamp) > dayInMillis) {
                    val key = database.child(FIREBASE_PAST_APPOINTMENTS_PATH).push().key
                    database.child("$FIREBASE_PAST_APPOINTMENTS_PATH/$key").setValue(dataSnapshot.value)
                    dataSnapshot.ref.setValue(null)
                }
                
                appointment.id = dataSnapshot.key!!

                // Set isCancelable
                appointment.isCancelable = currentUser!!.userType == "Client" || currentUser!!.admin == true

                appointment.isFromCurrentUser = currentUser!!.userType == "Client"
            }
            appointment!!
        }

        val query: Query = when (currentUser!!.userType) {
            "Client" -> database.child(FIREBASE_APPOINTMENTS_PATH).orderByChild("clientUid").equalTo(currentUser!!.uid)
            else -> database.child(FIREBASE_APPOINTMENTS_PATH)
        }

        // Set query options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<Appointment>()
            .setQuery(query, parser)
            // Set lifecycle owner to automatically handle start and stop listening
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        binding.recyclerviewAppointments.adapter = AppointmentsAdapter(options, AppointmentsAdapter.OnCancelClickListener { appointment: Appointment, user: User? ->
            if (currentUser!!.userType == "Client" || currentUser!!.admin == true) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Are you would like to cancel your appointment on ${appointment.formattedTimestamp}?")
                    .setPositiveButton("Yes") { _, _ ->
                        appointmentsViewModel.cancelAppointment(appointment)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                findNavController().navigate(AppointmentsFragmentDirections.actionAppointmentsFragmentToChatFragment2(user!!, user.displayName))
            }
        })

        binding.recyclerviewAppointments.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))

        return binding.root
    }
}
package com.virtuobookings.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.virtuobookings.R
import com.virtuobookings.database.Appointment
import com.virtuobookings.databinding.FragmentPastAppointmentsBinding
import com.virtuobookings.util.FIREBASE_PAST_APPOINTMENTS_PATH
import com.virtuobookings.util.PastAppointmentsAdapter

class PastAppointmentsFragment: Fragment() {

    private lateinit var binding: FragmentPastAppointmentsBinding
    private lateinit var database: DatabaseReference
    private lateinit var arguments: PastAppointmentsFragmentArgs
    private lateinit var pastAppointmentsAdapter: PastAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_past_appointments, container, false)
        binding.lifecycleOwner = this

        arguments = PastAppointmentsFragmentArgs.fromBundle(requireArguments())
        val user = arguments.user
        database = FirebaseDatabase.getInstance().reference

        val parser: SnapshotParser<Appointment> = SnapshotParser { dataSnapshot ->
            val appointment = dataSnapshot.getValue(Appointment::class.java)
            if (appointment != null) {
                // Delete data older than one year
                val yearInMillis: Long = 1000.toLong() * 60 * 60 * 24 * 365
                if ((System.currentTimeMillis() - appointment.timestamp) > yearInMillis) {
                    dataSnapshot.ref.setValue(null)
                }

                appointment.id = dataSnapshot.key!!
            }
            appointment!!
        }

        val query: Query = database.child(FIREBASE_PAST_APPOINTMENTS_PATH).orderByChild("clientUid").equalTo(user.uid)

        // Set query options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<Appointment>()
            .setQuery(query, parser)
            // Set lifecycle owner to automatically handle start and stop listening
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        pastAppointmentsAdapter =
            PastAppointmentsAdapter(
                options
            )

        binding.recyclerviewPastAppointments.adapter = pastAppointmentsAdapter

        return binding.root
    }
}
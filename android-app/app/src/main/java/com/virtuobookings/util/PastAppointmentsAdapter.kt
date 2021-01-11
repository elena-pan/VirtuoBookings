package com.virtuobookings.util

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.User
import com.virtuobookings.databinding.AppointmentItemBinding
import com.virtuobookings.viewmodels.ChatViewModel

class PastAppointmentsAdapter(options: FirebaseRecyclerOptions<Appointment>): FirebaseRecyclerAdapter<Appointment, PastAppointmentsAdapter.PastAppointmentViewHolder>(options)  {

    // Viewholder
    class PastAppointmentViewHolder(val binding: AppointmentItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment, user: User) {
            binding.appointment = appointment
            binding.user = user
            binding.appointmentActionButton.visibility = View.GONE
            binding.executePendingBindings()
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastAppointmentViewHolder {
        return PastAppointmentViewHolder(
            AppointmentItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: PastAppointmentViewHolder, position: Int, appointment: Appointment) {
        // Get user from database
        val database = FirebaseDatabase.getInstance().reference
        database.child("userData").child(appointment.clientUid).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        Log.w(ChatViewModel.TAG, "User not found")
                    } else {
                        val user = dataSnapshot.getValue(User::class.java)
                        user!!.uid = dataSnapshot.key!!
                        holder.bind(appointment, user)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ChatViewModel.TAG, "getUser:onCancelled", databaseError.toException())
                }
            })
        }

    override fun onError(error: DatabaseError) {
        super.onError(error)
        Log.i("pastAppointAdapter", error.toString())
    }
}
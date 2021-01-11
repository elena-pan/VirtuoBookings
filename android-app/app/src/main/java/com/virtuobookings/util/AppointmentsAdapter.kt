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
import com.virtuobookings.R
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.User
import com.virtuobookings.databinding.AppointmentItemBinding
import com.virtuobookings.viewmodels.ChatViewModel


class AppointmentsAdapter(options: FirebaseRecyclerOptions<Appointment>, val onCancelClickListener: OnCancelClickListener): FirebaseRecyclerAdapter<Appointment, AppointmentsAdapter.AppointmentViewHolder>(options)  {

    // Viewholder
    class AppointmentViewHolder(val binding: AppointmentItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment, onCancelClickListener: OnCancelClickListener, user: User?) {
            binding.appointment = appointment
            binding.user = user
            if (appointment.isFromCurrentUser == true) {
                binding.clientName.visibility = View.GONE
            }
            if (appointment.isCancelable == true) {
                binding.appointmentActionButton.setText(R.string.cancel_appointment_text)
                binding.appointmentActionButton.setOnClickListener {
                    onCancelClickListener.onClick(appointment, null)
                }
            } else {
                binding.appointmentActionButton.setText(R.string.message_client_text)
                binding.appointmentActionButton.setOnClickListener {
                    onCancelClickListener.onClick(appointment, user)
                }
            }
            binding.executePendingBindings()
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(AppointmentItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int, appointment: Appointment) {
        if (appointment.isFromCurrentUser == true) {
            holder.bind(appointment, onCancelClickListener, null)
            return
        } else {
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
                            holder.bind(appointment, onCancelClickListener, user)
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(ChatViewModel.TAG, "getUser:onCancelled", databaseError.toException())
                    }
                })
        }

    }

    override fun onError(error: DatabaseError) {
        super.onError(error)
        // Called when there is an error getting data.
        // Update UI to display an error message to the user.
        Log.i("appointmentAdapter", error.toString())
    }

    /**
     * Custom listener that handles clicks on the item button.  Passes the [Appointment]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [Appointment]
     */
    class OnCancelClickListener(val clickListener: (Appointment, User?) -> Unit) {
        fun onClick(appointment: Appointment, user: User?) = clickListener(appointment, user)
    }
}
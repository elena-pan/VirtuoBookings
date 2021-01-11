package com.virtuobookings.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.virtuobookings.database.Appointment
import com.virtuobookings.databinding.OpenAppointmentSlotItemBinding

/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 * @param onClick a lambda that takes the
 */
class OpenAppointmentsAdapter( val onBookAppointmentClickListener: OnBookAppointmentClickListener ) :
    ListAdapter<Appointment, OpenAppointmentsAdapter.AppointmentViewHolder>(DiffCallback) {
    /**
     * The MarsPropertyViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [User] information.
     */
    class AppointmentViewHolder(private var binding: OpenAppointmentSlotItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appointment: Appointment, onBookAppointmentClickListener: OnBookAppointmentClickListener) {
            binding.appointment = appointment
            binding.bookButton.setOnClickListener {
                onBookAppointmentClickListener.onClick(appointment)
            }
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Appointment]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            OpenAppointmentSlotItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = getItem(position)
        holder.bind(appointment, onBookAppointmentClickListener)
    }

    /**
     * Custom listener that handles clicks on the item button.  Passes the [Appointment]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [Appointment]
     */
    class OnBookAppointmentClickListener(val clickListener: (Appointment) -> Unit) {
        fun onClick(appointment: Appointment) = clickListener(appointment)
    }
}

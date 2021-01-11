package com.virtuobookings.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.virtuobookings.database.User
import com.virtuobookings.databinding.EditUsersItemBinding

/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 * @param onClick a lambda that takes the
 */
class EditUsersAdapter( val onClickListener: OnClickListener, val onCheckboxClicked: OnCheckboxClicked, val onPastAppointmentsClicked: OnPastAppointmentsClicked ) :
    ListAdapter<User, EditUsersAdapter.UserViewHolder>(DiffCallback) {
    /**
     * The MarsPropertyViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [User] information.
     */
    class UserViewHolder(private var binding: EditUsersItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, onClickListener: OnClickListener, onCheckboxClicked: OnCheckboxClicked, onPastAppointmentsClicked: OnPastAppointmentsClicked) {
            binding.user = user
            binding.isadminCheckbox.isChecked = user.admin

            binding.messageButton.setOnClickListener{
                onClickListener.onClick(user)
            }

            if (user.userType == "Staff") {
                binding.isadminCheckbox.setOnClickListener {
                    onCheckboxClicked.onClick(user, binding.isadminCheckbox.isChecked, binding.isadminCheckbox)
                }
            } else {
                binding.isadminCheckbox.isEnabled = false
            }

            if (user.userType == "Client") {
                binding.pastAppointmentsButton.setOnClickListener {
                    onPastAppointmentsClicked.onClick(user)
                }
            } else {
                binding.pastAppointmentsButton.visibility = View.GONE
            }

            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [User]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid === newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): UserViewHolder {
        return UserViewHolder(EditUsersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, onClickListener, onCheckboxClicked, onPastAppointmentsClicked)
    }

    class OnClickListener(val clickListener: (user: User) -> Unit) {
        fun onClick(user: User) = clickListener(user)
    }
    class OnCheckboxClicked(val checkboxClickListener: (user: User, isChecked: Boolean, compoundButton: CompoundButton) -> Unit) {
        fun onClick(user: User, isChecked: Boolean, compoundButton: CompoundButton) = checkboxClickListener(user, isChecked, compoundButton)
    }
    class OnPastAppointmentsClicked(val clickListener: (user: User) -> Unit) {
        fun onClick(user: User) = clickListener(user)
    }
}

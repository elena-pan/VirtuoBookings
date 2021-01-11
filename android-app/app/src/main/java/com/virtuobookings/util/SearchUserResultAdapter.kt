package com.virtuobookings.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.virtuobookings.database.User
import com.virtuobookings.databinding.NewContactsItemBinding

/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 * @param onClick a lambda that takes the
 */
class SearchUserResultAdapter( val onClickListener: OnClickListener ) :
    ListAdapter<User, SearchUserResultAdapter.UserViewHolder>(DiffCallback) {
    /**
     * The MarsPropertyViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [User] information.
     */
    class UserViewHolder(private var binding: NewContactsItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user
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
        return UserViewHolder(NewContactsItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(user)
        }
        holder.bind(user)
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [User]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [User]
     */
    class OnClickListener(val clickListener: (user: User) -> Unit) {
        fun onClick(user: User) = clickListener(user)
    }
}

package com.virtuobookings.util

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.database.User
import com.virtuobookings.databinding.ContactsItemBinding
import com.virtuobookings.viewmodels.ChatViewModel

class ContactsAdapter(options: FirebaseRecyclerOptions<ChatMessage>, val onClickListener: OnClickListener): FirebaseRecyclerAdapter<ChatMessage, ContactsAdapter.ContactViewHolder>(options)  {

    companion object {
        const val TO_MESSAGE = 0
        const val FROM_MESSAGE = 1
    }

    // Viewholder for contacts/latest messages
    class ContactViewHolder(val binding: ContactsItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage, user: User) {
            binding.user = user
            binding.chatMessage = chatMessage
            // Forces data binding to execute immediately
            binding.executePendingBindings()
        }
    }

    // Set viewtype as a from message or to message
    override fun getItemViewType(position: Int): Int {
        val chatMessage = getItem(position)
        return if (chatMessage.isFromCurrentUser == true) {
            TO_MESSAGE
        } else {
            FROM_MESSAGE
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
                ContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int, chatMessage: ChatMessage) {
        // Get contact id based on whether message is from current user or not
        val contactId = when (chatMessage.isFromCurrentUser) {
            true -> chatMessage.toId
            else -> chatMessage.fromId
        }
        // Get user from database
        val database = FirebaseDatabase.getInstance().reference
        database.child("userData").child(contactId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        Log.w(ChatViewModel.TAG, "User not found")
                    }

                    else {
                        val user = dataSnapshot.getValue(User::class.java)
                        user!!.uid = dataSnapshot.key!!

                        holder.itemView.setOnClickListener {
                            onClickListener.onClick(user)
                        }
                        holder.bind(chatMessage, user)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ChatViewModel.TAG, "getUser:onCancelled", databaseError.toException())
                }
            })
    }

    override fun onError(error: DatabaseError) {
        super.onError(error)
        // Called when there is an error getting data.
        // Update UI to display an error message to the user.
        Log.i("contactsAdapter", error.toString())
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [User]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [User]
     */
    class OnClickListener(val clickListener: (user: User) -> Unit) {
        fun onClick(user:User) = clickListener(user)
    }
}

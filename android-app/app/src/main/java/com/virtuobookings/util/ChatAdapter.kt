package com.virtuobookings.util

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.databinding.ChatMessageFromBinding
import com.virtuobookings.databinding.ChatMessageToBinding
import com.virtuobookings.databinding.FragmentAdminChatBinding
import com.virtuobookings.databinding.FragmentChatBinding


class ChatAdapter(options: FirebaseRecyclerOptions<ChatMessage>): FirebaseRecyclerAdapter<ChatMessage, ChatAdapter.MessageViewHolder>(options)  {

    companion object {
        const val TO_MESSAGE = 0
        const val FROM_MESSAGE = 1
    }

    // Viewholder for chat messages
    class MessageViewHolder: RecyclerView.ViewHolder {

        private var chatMessageToBinding: ChatMessageToBinding? = null
        private var chatMessageFromBinding: ChatMessageFromBinding? = null

        // Two different constructors to accommodate for different bindings
        constructor (binding: ChatMessageToBinding): super(binding.root) {
            chatMessageToBinding = binding
        }

        constructor (binding: ChatMessageFromBinding): super(binding.root) {
            chatMessageFromBinding = binding
        }

        fun bind(chatMessage: ChatMessage) {
            chatMessageFromBinding?.chatMessage = chatMessage
            chatMessageToBinding?.chatMessage = chatMessage
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            chatMessageFromBinding?.executePendingBindings()
            chatMessageToBinding?.executePendingBindings()
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            TO_MESSAGE -> MessageViewHolder(
                ChatMessageToBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            FROM_MESSAGE -> MessageViewHolder(
                ChatMessageFromBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> throw IllegalArgumentException("Invalid type of message")
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, chatMessage: ChatMessage) {
        holder.bind(chatMessage)
    }

    override fun onError(error: DatabaseError) {
        super.onError(error)
        // Called when there is an error getting data.
        // Update UI to display an error message to the user.
        Log.i("chatAdapter", error.toString())
    }
}

// AdapterDataObserver to check if should scroll to bottom when item is inserted
class ChatAdapterObserver(
    private val chatAdapter: ChatAdapter,
    private val linearLayoutManager: LinearLayoutManager,
    private val binding: FragmentChatBinding): RecyclerView.AdapterDataObserver() {

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        val numMessages = chatAdapter.itemCount
        val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        // If the recyclerview is initially being loaded or the
        // user is at the bottom of the list, scroll to the bottom
        // of the list to show the newly added message.
        if (lastVisiblePosition == -1 ||
            positionStart >= numMessages - 1 &&
            lastVisiblePosition == positionStart - 1
        ) {
            binding.recyclerviewChat.scrollToPosition(positionStart)
        }
    }
}

// AdapterDataObserver to check if should scroll to bottom when item is inserted
class AdminChatAdapterObserver(
    private val chatAdapter: ChatAdapter,
    private val linearLayoutManager: LinearLayoutManager,
    private val binding: FragmentAdminChatBinding
): RecyclerView.AdapterDataObserver() {

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        val numMessages = chatAdapter.itemCount
        val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        // If the recyclerview is initially being loaded or the
        // user is at the bottom of the list, scroll to the bottom
        // of the list to show the newly added message.
        if (lastVisiblePosition == -1 ||
            positionStart >= numMessages - 1 &&
            lastVisiblePosition == positionStart - 1
        ) {
            binding.recyclerviewChat.scrollToPosition(positionStart)
        }
    }
}
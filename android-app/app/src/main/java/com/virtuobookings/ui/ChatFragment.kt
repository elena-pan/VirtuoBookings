package com.virtuobookings.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.virtuobookings.R
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.databinding.FragmentChatBinding
import com.virtuobookings.util.*
import com.virtuobookings.viewmodels.ChatViewModel
import com.virtuobookings.viewmodels.ChatViewModelFactory

class ChatFragment: Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatViewModelFactory: ChatViewModelFactory
    private lateinit var arguments: ChatFragmentArgs
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatAdapterObserver: ChatAdapterObserver
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding.lifecycleOwner = this

        arguments = ChatFragmentArgs.fromBundle(requireArguments())
        val user = arguments.user
        database = FirebaseDatabase.getInstance().reference

        chatViewModelFactory = ChatViewModelFactory(user, requireActivity().application)
        chatViewModel = ViewModelProvider(this, chatViewModelFactory).get(ChatViewModel::class.java)
        binding.chatViewModel = chatViewModel

        // Clear edittext or show error after a message has been sent
        chatViewModel.messageSent.observe(viewLifecycleOwner, Observer {
            if ( null != it ) {
                when (it) {
                    // Clear edittext
                    ChatViewModel.MessageStatus.SUCCESS -> {
                        binding.edittextChat.setText("")
                        chatViewModel.messageSentDone()
                    }
                    // Show no internet snackbar
                    ChatViewModel.MessageStatus.NO_INTERNET -> Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_LONG).show()
                    // Show error snackbar
                    ChatViewModel.MessageStatus.ERROR -> Snackbar.make(binding.root, "Error sending message", Snackbar.LENGTH_LONG).show()
                }
            }
        })

        // Listener for changes to edittext
        binding.edittextChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // Remove whitespace
                val trimmedText = charSequence.toString().trim()
                // Enable button if text is not empty
                binding.sendButtonChatLog.isEnabled = trimmedText.isNotEmpty()
                // Set change in viewmodel
                chatViewModel.onMessageTextChanged(trimmedText)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        // Set listener for enter key press to send message
        binding.edittextChat.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEND){
                chatViewModel.sendMessage()
                true
            } else {
                false
            }
        }

        val parser: SnapshotParser<ChatMessage> = SnapshotParser { dataSnapshot ->
            val chatMessage = dataSnapshot.getValue(ChatMessage::class.java)
            if (chatMessage != null) {

                // Delete messages older than one month
                val monthInMillis: Long = 1000.toLong() * 60 * 60 * 24 * 30
                if ((System.currentTimeMillis() - chatMessage.timestamp) > monthInMillis) {
                    dataSnapshot.ref.setValue(null)
                }

                chatMessage.id = dataSnapshot.key!!

                // Set isFromCurrentUser
                chatMessage.isFromCurrentUser = chatMessage.fromId == currentUser!!.uid
            }
            chatMessage!!
        }

        val query = when(user.uid) {
            ADMIN_ID -> database.child("$FIREBASE_ADMIN_MESSAGES_PATH/${currentUser!!.uid}")
            else -> database.child("$FIREBASE_MESSAGES_PATH/${currentUser!!.uid}/${user.uid}")
        }

        // Set query options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(query, parser)
            // Set lifecycle owner to automatically handle start and stop listening
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        // Initialize linear layout manager here so we can use findLastCompletelyVisibleItemPosition()
        linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        binding.recyclerviewChat.layoutManager = linearLayoutManager

        chatAdapter =
            ChatAdapter(
                options
            )
        chatAdapterObserver =
            ChatAdapterObserver(
                chatAdapter,
                linearLayoutManager,
                binding
            )

        // When new items are inserted
        chatAdapter.registerAdapterDataObserver(chatAdapterObserver)

        binding.recyclerviewChat.adapter = chatAdapter

        return binding.root
    }
}
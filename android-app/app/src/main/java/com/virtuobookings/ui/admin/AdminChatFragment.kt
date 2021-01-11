package com.virtuobookings.ui.admin

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
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.databinding.FragmentAdminChatBinding
import com.virtuobookings.ui.ChatFragmentArgs
import com.virtuobookings.util.ADMIN_ID
import com.virtuobookings.util.AdminChatAdapterObserver
import com.virtuobookings.util.ChatAdapter
import com.virtuobookings.util.FIREBASE_ADMIN_MESSAGES_PATH
import com.virtuobookings.viewmodels.admin.AdminChatViewModel
import com.virtuobookings.viewmodels.admin.AdminChatViewModelFactory

class AdminChatFragment: Fragment() {

    private lateinit var binding: FragmentAdminChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var adminChatViewModel: AdminChatViewModel
    private lateinit var adminChatViewModelFactory: AdminChatViewModelFactory
    private lateinit var arguments: ChatFragmentArgs
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var adminChatAdapterObserver: AdminChatAdapterObserver
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_chat, container, false)
        binding.lifecycleOwner = this

        arguments = ChatFragmentArgs.fromBundle(requireArguments())
        val user = arguments.user
        database = FirebaseDatabase.getInstance().reference

        adminChatViewModelFactory = AdminChatViewModelFactory(user, requireActivity().application)
        adminChatViewModel = ViewModelProvider(this, adminChatViewModelFactory).get(AdminChatViewModel::class.java)
        binding.adminChatViewModel = adminChatViewModel

        // Clear edittext or show error after a message has been sent
        adminChatViewModel.messageSent.observe(viewLifecycleOwner, Observer {
            if ( null != it ) {
                when (it) {
                    // Clear edittext
                    AdminChatViewModel.MessageStatus.SUCCESS -> {
                        binding.edittextChat.setText("")
                        adminChatViewModel.messageSentDone()
                    }
                    // Show no internet snackbar
                    AdminChatViewModel.MessageStatus.NO_INTERNET -> Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_LONG).show()
                    // Show error snackbar
                    AdminChatViewModel.MessageStatus.ERROR -> Snackbar.make(binding.root, "Error sending message", Snackbar.LENGTH_LONG).show()
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
                adminChatViewModel.onMessageTextChanged(trimmedText)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        // Set listener for enter key press to send message
        binding.edittextChat.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEND){
                adminChatViewModel.sendMessage()
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
                chatMessage.isFromCurrentUser = chatMessage.fromId == ADMIN_ID
            }
            chatMessage!!
        }

        // Set query options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(database.child("$FIREBASE_ADMIN_MESSAGES_PATH/${user.uid}"), parser)
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
        adminChatAdapterObserver =
            AdminChatAdapterObserver(
                chatAdapter,
                linearLayoutManager,
                binding
            )

        // When new items are inserted
        chatAdapter.registerAdapterDataObserver(adminChatAdapterObserver)

        binding.recyclerviewChat.adapter = chatAdapter

        return binding.root
    }
}
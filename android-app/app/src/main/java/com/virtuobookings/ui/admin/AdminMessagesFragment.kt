package com.virtuobookings.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.virtuobookings.R
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.databinding.FragmentAdminMessagesBinding
import com.virtuobookings.util.ADMIN_ID
import com.virtuobookings.util.ContactsAdapter
import com.virtuobookings.util.FIREBASE_ADMIN_LATEST_MESSAGES_PATH
import com.virtuobookings.viewmodels.admin.AdminMessagesViewModel

class AdminMessagesFragment: Fragment() {
    companion object {
        const val TAG = "adminMessagesFragment"
    }

    private lateinit var binding: FragmentAdminMessagesBinding

    private val adminMessagesViewModel: AdminMessagesViewModel by lazy {
        ViewModelProvider(this).get(AdminMessagesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_messages, container, false)
        binding.lifecycleOwner = this
        binding.adminMessagesViewModel = adminMessagesViewModel

        // Sort items in descending order AND populate on top of screen
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.recyclerviewAdminMessages.layoutManager = linearLayoutManager

        val parser: SnapshotParser<ChatMessage> = SnapshotParser { dataSnapshot ->
            val chatMessage = dataSnapshot.getValue(ChatMessage::class.java)
            if (chatMessage != null) {

                // Set isFromCurrentUser
                chatMessage.isFromCurrentUser = chatMessage.fromId == ADMIN_ID

                if (chatMessage.fromId == ADMIN_ID) {
                    chatMessage.toId = dataSnapshot.key!!
                }
            }
            chatMessage!!
        }

        val query: Query = FirebaseDatabase.getInstance().reference
            .child(FIREBASE_ADMIN_LATEST_MESSAGES_PATH)
            // Order by timestamp - reverse order in linearlayoutmanager
            .orderByChild("timestamp")

        // Set query options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(query, parser)
            // Set lifecycle owner to automatically handle start and stop listening
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        // Sets the adapter of the contacts RecyclerView with clickHandler lambda that
        // tells the viewModel when a contact is clicked
        binding.recyclerviewAdminMessages.adapter = ContactsAdapter(options,
            ContactsAdapter.OnClickListener {
                adminMessagesViewModel.navigateToChat(it)
            })

        adminMessagesViewModel.chatClicked.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                // Navigate to admin messages fragment
                findNavController().navigate(AdminMessagesFragmentDirections.actionAdminMessagesFragmentToAdminChatFragment(it, it.displayName))
                adminMessagesViewModel.navigateToChatDone()
            }
        })

        return binding.root
    }
}
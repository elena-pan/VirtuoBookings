/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtuobookings.ui

import android.os.Bundle
import android.util.Log
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
import com.virtuobookings.VirtuoBookingsApplication.Companion.currentUser
import com.virtuobookings.database.ChatMessage
import com.virtuobookings.database.User
import com.virtuobookings.databinding.FragmentContactsBinding
import com.virtuobookings.util.ADMIN_ID
import com.virtuobookings.util.ContactsAdapter
import com.virtuobookings.util.FIREBASE_CONTACTS_LATEST_MESSAGES_PATH
import com.virtuobookings.viewmodels.ContactsViewModel

class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        try {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_contacts, container, false
            )

            val contactsViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)

            binding.contactsViewModel = contactsViewModel

            // Sort items in descending order AND populate on top of screen
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.reverseLayout = true
            linearLayoutManager.stackFromEnd = true
            binding.recyclerviewContacts.layoutManager = linearLayoutManager

            val parser: SnapshotParser<ChatMessage> = SnapshotParser { dataSnapshot ->
                val chatMessage = dataSnapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    // Set isFromCurrentUser
                    chatMessage.isFromCurrentUser = chatMessage.fromId == currentUser!!.uid

                    if (chatMessage.fromId == currentUser!!.uid) {
                        chatMessage.toId = dataSnapshot.key!!
                    }
                }
                chatMessage!!
            }

            val query: Query = FirebaseDatabase.getInstance().reference
                .child("${FIREBASE_CONTACTS_LATEST_MESSAGES_PATH}/${currentUser!!.uid}")
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
            binding.recyclerviewContacts.adapter = ContactsAdapter(options,
                ContactsAdapter.OnClickListener {
                    contactsViewModel.navigateToChat(it)
                })

            binding.adminContact.setOnClickListener {
                contactsViewModel.navigateToChat(User(ADMIN_ID, "Admin", "", ""))
            }

            contactsViewModel.navigateToChat.observe(viewLifecycleOwner, Observer {
                it?.let {
                    this.findNavController().navigate(
                        ContactsFragmentDirections
                            .actionContactsFragmentToChatFragment(it, it.displayName)
                    )
                    contactsViewModel.doneNavigatingToChat()
                }
            })

            // Observe to check if new chat button has been clicked - navigate to search for users
            contactsViewModel.newChatClicked.observe(viewLifecycleOwner, Observer {
                it?.let {
                    findNavController().navigate(ContactsFragmentDirections.actionContactsFragmentToSearchUsersFragment())
                    contactsViewModel.doneNavigatingToSearchUsers()
                }
            })

            return binding.root
        } catch (e: Exception) {
            Log.e("Test", e.toString())
            throw e
        }
    }
}
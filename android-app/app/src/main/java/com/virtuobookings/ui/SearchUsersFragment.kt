package com.virtuobookings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.virtuobookings.database.DataStatus
import com.virtuobookings.databinding.FragmentSearchUsersBinding
import com.virtuobookings.util.SearchUserResultAdapter
import com.virtuobookings.viewmodels.SearchUsersViewModel

class SearchUsersFragment: Fragment() {

    private lateinit var binding: FragmentSearchUsersBinding

    /**
     * Delay creation of the viewModel until an appropriate lifecycle method
     * So viewModel is not referenced before activity is created
     */
    private val searchUsersViewModel: SearchUsersViewModel by lazy {
        ViewModelProvider(this).get(SearchUsersViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentSearchUsersBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        binding.searchUsersViewModel = searchUsersViewModel

        binding.searchBar.apply {
            setIconifiedByDefault(false)
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchUsersViewModel.getSearchResults(query.trim()) }
                    return false
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    return false
                }
            })
        }

        // Set up adapter with onclick listener to pass User object
        binding.recyclerviewSearchResults.adapter = SearchUserResultAdapter(SearchUserResultAdapter.OnClickListener {
            searchUsersViewModel.navigateToChat(it)
        })

        // Navigate to right chat when clicked
        searchUsersViewModel.navigateToChat.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(SearchUsersFragmentDirections.actionSearchUsersFragmentToChatFragment(it, it.displayName))
                searchUsersViewModel.doneNavigatingToChat()
            }
        })

        // Show snackbar if error occurs
        searchUsersViewModel.status.observe(viewLifecycleOwner, Observer {
            if (it == DataStatus.ERROR) {
                Snackbar.make(binding.root, "Error finding users", Snackbar.LENGTH_SHORT)
                searchUsersViewModel.doneShowingErrorToast()
            }
        })

        return binding.root
    }
}
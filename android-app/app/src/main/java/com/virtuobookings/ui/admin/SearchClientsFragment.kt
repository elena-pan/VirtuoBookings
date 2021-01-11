package com.virtuobookings.ui.admin

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
import com.virtuobookings.databinding.FragmentSearchClientsBinding
import com.virtuobookings.util.SearchUserResultAdapter
import com.virtuobookings.viewmodels.admin.SearchClientsViewModel

class SearchClientsFragment: Fragment() {

    private lateinit var binding: FragmentSearchClientsBinding

    /**
     * Delay creation of the viewModel until an appropriate lifecycle method
     * So viewModel is not referenced before activity is created
     */
    private val searchClientsViewModel: SearchClientsViewModel by lazy {
        ViewModelProvider(this).get(SearchClientsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentSearchClientsBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        binding.searchClientsViewModel = searchClientsViewModel

        binding.searchBar.apply {
            setIconifiedByDefault(false)
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchClientsViewModel.filterUsers(query.trim()) }
                    return false
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    return false
                }
            })
        }

        // Set up adapter with onclick listener to pass User object
        binding.recyclerviewSearchResults.adapter = SearchUserResultAdapter(SearchUserResultAdapter.OnClickListener {
            searchClientsViewModel.navigateToBookAppointment(it)
        })

        // Navigate to book appointment onclick
        searchClientsViewModel.navigateToBookAppointment.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(SearchClientsFragmentDirections.actionSearchClientsFragmentToBookAppointmentFragment(it))
                searchClientsViewModel.doneNavigatingToBookAppointment()
            }
        })

        // Show snackbar if error occurs
        searchClientsViewModel.status.observe(viewLifecycleOwner, Observer {
            if (it == DataStatus.ERROR) {
                Snackbar.make(binding.root, "Error finding users", Snackbar.LENGTH_SHORT)
                searchClientsViewModel.doneShowingErrorToast()
            }
        })

        return binding.root
    }
}
package com.virtuobookings.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.virtuobookings.R
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.databinding.FragmentEditUsersBinding
import com.virtuobookings.util.EditUsersAdapter
import com.virtuobookings.viewmodels.admin.EditUsersViewModel
import com.virtuobookings.viewmodels.admin.EditUsersViewModelFactory

class EditUsersFragment: Fragment() {
    companion object {
        const val TAG = "editUsersFragment"
    }

    private lateinit var binding: FragmentEditUsersBinding

    private val editUsersViewModel: EditUsersViewModel by lazy {
        val editUsersViewModelFactory = EditUsersViewModelFactory(requireActivity().application)
        ViewModelProvider(this, editUsersViewModelFactory).get(EditUsersViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_users, container, false)
        binding.lifecycleOwner = this
        binding.editUsersViewModel = editUsersViewModel

        binding.searchBar.apply {
            setIconifiedByDefault(false)
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { editUsersViewModel.filterUsers(query.trim()) }
                    return false
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    return false
                }
            })
        }

        // Set up adapter with onclick listener to pass User object
        binding.recyclerviewUsersList.adapter = EditUsersAdapter(EditUsersAdapter.OnClickListener {
            editUsersViewModel.navigateToChat(it)
        },
        EditUsersAdapter.OnCheckboxClicked { user: User, isChecked: Boolean, compoundButton: CompoundButton ->
            val message: String = if (isChecked) {
                "Are you sure you would like to make ${user.name} an admin?"
            } else {
                "Are you sure you would like to remove admin status from ${user.name}?"
            }
            AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes") { _, _ ->
                    editUsersViewModel.changeAdminStatus(user, isChecked)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    compoundButton.isChecked = user.admin
                }
                .setCancelable(false)
                .show()
        },
        EditUsersAdapter.OnPastAppointmentsClicked {
            editUsersViewModel.navigateToPastAppointments(it)
        })

        // Navigate to right chat when clicked
        editUsersViewModel.navigateToChat.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(EditUsersFragmentDirections.actionEditUsersFragmentToAdminChatFragment(it, it.displayName))
                editUsersViewModel.doneNavigatingToChat()
            }
        })

        // Navigate to past appointments list
        editUsersViewModel.navigateToPastAppointments.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(EditUsersFragmentDirections.actionEditUsersFragmentToPastAppointmentsFragment(it, it.displayName))
                editUsersViewModel.doneNavigatingToPastAppointments()
            }
        })

        editUsersViewModel.setAdminStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    DataStatus.SUCCESS -> Snackbar.make(binding.root, "Changes saved", Snackbar.LENGTH_SHORT).show()
                    DataStatus.NO_INTERNET -> Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_SHORT).show()
                    DataStatus.ERROR -> Snackbar.make(binding.root, "An error has occurred. Please try again later", Snackbar.LENGTH_SHORT).show()
                }
                editUsersViewModel.changeAdminStatusDone()
            }
        })

        // Show snackbar if error occurs
        editUsersViewModel.status.observe(viewLifecycleOwner, Observer {
            if (it == DataStatus.ERROR) {
                Snackbar.make(binding.root, "Error finding users", Snackbar.LENGTH_SHORT)
                editUsersViewModel.doneShowingErrorToast()
            }
        })

        val spinner: Spinner = binding.usertypeSpinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_type_with_admin,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                editUsersViewModel.changeUserType(parent.getItemAtPosition(pos).toString())
                editUsersViewModel.filterUsers(null)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return binding.root
    }
}
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
import com.virtuobookings.R
import com.virtuobookings.databinding.FragmentAdminBinding
import com.virtuobookings.viewmodels.admin.AdminViewModel

class AdminFragment: Fragment() {
    companion object {
        const val TAG = "adminFragment"
    }

    private lateinit var binding: FragmentAdminBinding

    private val adminViewModel: AdminViewModel by lazy {
        ViewModelProvider(this).get(AdminViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false)
        binding.lifecycleOwner = this
        binding.adminViewModel = adminViewModel

        adminViewModel.messagesClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                // Navigate to admin messages fragment
                findNavController().navigate(AdminFragmentDirections.actionAdminFragmentToAdminMessagesFragment())
                adminViewModel.navigateToMessagesDone()
            }
        })

        adminViewModel.editUsersClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                // Navigate to edit users fragment
                findNavController().navigate(AdminFragmentDirections.actionAdminFragmentToEditUsersFragment())
                adminViewModel.navigateToEditUsersDone()
            }
        })

        return binding.root
    }
}
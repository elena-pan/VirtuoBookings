package com.virtuobookings.util

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.virtuobookings.database.Appointment
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User

/**
 * Binding adapter to hide the loading spinner once data is available
 */
@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: DataStatus?) {
    view.visibility = if (it == DataStatus.LOADING) View.VISIBLE else View.GONE
}

/**
 * Hide view if data status is loading
 */
@BindingAdapter("hideIfLoading")
fun hideIfLoading(view: View, it: DataStatus?) {
    view.visibility = if (it == DataStatus.LOADING) View.GONE else View.VISIBLE
}

/**
 * Binding adapter to show the no internet icon
 */
@BindingAdapter("noInternet")
fun noInternet(view: View, it: DataStatus?) {
    view.visibility = if (it == DataStatus.NO_INTERNET) View.VISIBLE else View.GONE
}

/**
 * When data is null, hide the userData [RecyclerView], otherwise show it.
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<User>?) {
    val adapter = recyclerView.adapter as SearchUserResultAdapter
    adapter.submitList(data)
}

/**
 * When data is null, hide the open appointment slots [RecyclerView], otherwise show it.
 */
@BindingAdapter("appointmentsListData")
fun bindOpenAppointmentsRecyclerView(recyclerView: RecyclerView, data: List<Appointment>?) {
    val adapter = recyclerView.adapter as OpenAppointmentsAdapter
    adapter.submitList(data)
}

/**
 * When data is null, hide the open appointment slots [RecyclerView], otherwise show it.
 */
@BindingAdapter("editUsersListData")
fun bindEditUsersRecyclerView(recyclerView: RecyclerView, data: List<User>?) {
    val adapter = recyclerView.adapter as EditUsersAdapter
    adapter.submitList(data)
}

/**
 * When no results, display No Users Found, otherwise hide textview
 */
@BindingAdapter("noUsersFound")
fun noUsersFound(textView: TextView, dataStatus: DataStatus) {
    textView.visibility = if (dataStatus == DataStatus.NO_RESULTS) View.VISIBLE else View.GONE
}
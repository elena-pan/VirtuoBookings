package com.virtuobookings.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.virtuobookings.R
import com.virtuobookings.database.DataStatus
import com.virtuobookings.database.User
import com.virtuobookings.databinding.CalendarDayBinding
import com.virtuobookings.databinding.CalendarHeaderBinding
import com.virtuobookings.databinding.FragmentBookAppointmentBinding
import com.virtuobookings.util.OpenAppointmentsAdapter
import com.virtuobookings.util.daysOfWeekFromLocale
import com.virtuobookings.util.setTextColorRes
import com.virtuobookings.viewmodels.BookAppointmentViewModel
import com.virtuobookings.viewmodels.BookAppointmentViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@SuppressLint("ResourceAsColor")
class BookAppointmentFragment: Fragment() {

    private lateinit var user: User

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val weekFromToday = LocalDate.now().plusDays(7)

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    private lateinit var binding: FragmentBookAppointmentBinding

    /**
     * Delay creation of the viewModel until an appropriate lifecycle method
     * So viewModel is not referenced before activity is created
     */
    private val bookAppointmentViewModel: BookAppointmentViewModel by lazy {
        val bookAppointmentViewModelFactory = BookAppointmentViewModelFactory(requireActivity().application)
        ViewModelProvider(this, bookAppointmentViewModelFactory).get(BookAppointmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_book_appointment, container, false)

        binding.bookAppointmentViewModel = bookAppointmentViewModel
        binding.lifecycleOwner = this

        val arguments = BookAppointmentFragmentArgs.fromBundle(requireArguments())
        user = arguments.user

        bookAppointmentViewModel.bookAppointment.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    DataStatus.SUCCESS -> Snackbar.make(binding.root, "Appointment booked successfully", Snackbar.LENGTH_SHORT).show()
                    DataStatus.ERROR -> Snackbar.make(binding.root, "Error booking appointment", Snackbar.LENGTH_SHORT).show()
                    DataStatus.NO_INTERNET -> Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_SHORT).show()
                }
                bookAppointmentViewModel.doneBookAppointment()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.exThreeRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            adapter = OpenAppointmentsAdapter(OpenAppointmentsAdapter.OnBookAppointmentClickListener {
                AlertDialog.Builder(requireContext())
                    .setMessage("Are you would like to book an appointment on ${it.formattedTimestamp}?")
                    .setPositiveButton("Yes") { _, _ ->
                        bookAppointmentViewModel.bookAppointment(it, user)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            })
        }
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        binding.exThreeCalendar.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            binding.exThreeCalendar.post {
                // Show today's events initially.
                selectDate(weekFromToday)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == weekFromToday || day.date.isAfter(weekFromToday))) {
                        selectDate(day.date)
                    }
                }
            }
        }
        binding.exThreeCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {

                container.day = day
                val textView = container.binding.exThreeDayText

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    if (day.date.isBefore(weekFromToday)) {
                        textView.setTextColorRes(R.color.greyPastDate)
                        if (day.date.isEqual(today)) {
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                    } else {
                        when (day.date) {
                            selectedDate -> {
                                textView.setBackgroundResource(R.drawable.selected_day_bg)
                            }
                            else -> {
                                textView.background = null
                            }
                        }
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                }
            }
        }

        binding.exThreeCalendar.monthScrollListener = {
            binding.monthText.text = if (it.year == weekFromToday.year) {
                titleSameYearFormatter.format(it.yearMonth)
            } else {
                titleFormatter.format(it.yearMonth)
            }

            // Select the first day of the month when
            // we scroll to a new month.
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout
        }

        binding.exThreeCalendar.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                        tv.setTextColorRes(R.color.black)
                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.exThreeCalendar.notifyDateChanged(it) }
            binding.exThreeCalendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        binding.exThreeSelectedDateText.text = selectionFormatter.format(date)
        bookAppointmentViewModel.generateAppointmentSlotsForDay(date)
    }

}
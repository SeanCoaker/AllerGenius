package com.coaker.foodlabelapp

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_view.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*


class DiaryFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var diaryProgressBar: ProgressBar

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private val events = ArrayList<CalendarEvent>()
    private val eventPointers = mutableMapOf<LocalDate, CalendarEvent>()

    private val dateFormatMonth = DateTimeFormatter.ofPattern("MMMM - yyyy", Locale.getDefault())
    private val selectedDateFormat = DateTimeFormatter.ofPattern("d MMMM - yyyy")

    private val user = Firebase.auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.calendar_view, container, false)

        calendarView = root.findViewById(R.id.calendarView)
        selectedDateText = root.findViewById(R.id.selectedDateText)
        diaryProgressBar = root.findViewById(R.id.diaryProgressBar)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(12)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, currentMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        loadMonthEventsFromFirestore(dateFormatMonth.format(currentMonth))

        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            val foodNotifView = view.findViewById<View>(R.id.foodNotifView)
            val symptomNotifView = view.findViewById<View>(R.id.symptomNotifView)
            val eventsSpacer = view.findViewById<Space>(R.id.eventsSpacer)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {

            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {

                container.day = day
                val textView = container.textView
                val foodNotifView = container.foodNotifView
                val symptomNotifView = container.symptomNotifView
                val eventsSpacer = container.eventsSpacer

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    container.textView.visibility = View.VISIBLE

                    when (day.date) {

                        today -> {
                            textView.setTextColor(context!!.getColor(R.color.white))
                            textView.setBackgroundResource(R.drawable.today_day_circle)
                            foodNotifView.visibility = View.GONE
                            symptomNotifView.visibility = View.GONE
                            eventsSpacer.visibility = View.GONE
                        }

                        selectedDate -> {
                            textView.setTextColor(context!!.getColor(R.color.black))
                            textView.setBackgroundResource(R.drawable.selected_day_circle)
                            foodNotifView.visibility = View.GONE
                            symptomNotifView.visibility = View.GONE
                            eventsSpacer.visibility = View.GONE
                        }

                        else -> {
                            textView.setTextColor(context!!.getColor(R.color.black))
                            textView.background = null

                            if (eventPointers.contains(day.date)) {
                                foodNotifView.isVisible = eventPointers[day.date]!!.food
                                symptomNotifView.isVisible = eventPointers[day.date]!!.symptom

                                if (foodNotifView.isVisible && symptomNotifView.isVisible) {
                                    eventsSpacer.visibility = View.VISIBLE
                                } else {
                                    eventsSpacer.visibility = View.GONE
                                }

                            } else {
                                foodNotifView.visibility = View.GONE
                                symptomNotifView.visibility = View.GONE
                                eventsSpacer.visibility = View.GONE
                            }

                        }
                    }
                } else {
                    textView.visibility = View.GONE
                    foodNotifView.visibility = View.GONE
                    symptomNotifView.visibility = View.GONE
                    eventsSpacer.visibility = View.GONE
                }
            }
        }

        calendarView.monthScrollListener = {
            activity?.title = dateFormatMonth.format(it.yearMonth)
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val parentLayout = view.findViewById<ViewGroup>(R.id.dayLinearLayout)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                if (container.parentLayout.tag == null) {
                    container.parentLayout.tag = month.yearMonth
                    container.parentLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeekFromLocale()[index].name.first().toString()
                        tv.setTextColor(context!!.getColor(R.color.black))
                    }
                }
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        selectDate(today)
    }


    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { calendarView.notifyDateChanged(it) }
            calendarView.notifyDateChanged(date)
            selectedDateText.text = selectedDateFormat.format(date)
        }
    }


    private fun loadMonthEventsFromFirestore(monthYear: String) {
        events.clear()
        eventPointers.clear()

        calendarView.visibility = View.GONE
        selectedDateText.visibility = View.GONE
        diaryProgressBar.visibility = View.VISIBLE

        val docRef = db.collection("users").document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val eventsCollection = docRef.collection("events")

                val eventsPointersMonth = eventsCollection
                    .document("eventPointers")
                    .collection("months")
                    .document(monthYear)

                eventsPointersMonth.get().addOnSuccessListener { eventPointersDocSnapshot ->
                    if (eventPointersDocSnapshot.exists()) {
                        val result = eventPointersDocSnapshot.toObject(EventPointers::class.java)
                        if (result?.events != null) {
                            setupMonthEntries(result.events)
                            calendarView.notifyCalendarChanged()
                            calendarView.visibility = View.VISIBLE
                            selectedDateText.visibility = View.VISIBLE
                            diaryProgressBar.visibility = View.GONE
                        }
                    }
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
            calendarView.visibility = View.VISIBLE
            selectedDateText.visibility = View.VISIBLE
            diaryProgressBar.visibility = View.GONE
        }
    }


    private fun setupMonthEntries(events: ArrayList<CalendarEvent>) {

        events.forEach {
            eventPointers[LocalDate.parse(it.day)] = it
        }
    }


    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }
}
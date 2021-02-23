package com.coaker.foodlabelapp

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlin.collections.ArrayList


class DiaryFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var diaryRecordsLayout: View
    private lateinit var selectedDateText: TextView
    private lateinit var foodErrorText: TextView
    private lateinit var diaryProgressBar: ProgressBar
    private lateinit var foodsRecyclerView: RecyclerView
    private lateinit var symptomsTextLayout: TextInputLayout
    private lateinit var symptomsEditText: TextInputEditText
    private lateinit var tabLayoutFoodSymptom: TabLayout
    private lateinit var adapter: FoodsAdapter
    private lateinit var symptomsSubmitButton: Button
    private lateinit var addFoodFab: ExtendedFloatingActionButton

    private var currentSymptomsText: String = ""

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private var foodsArray = ArrayList<FoodDiaryItem>()
    private var events = ArrayList<CalendarEvent>()
    private val eventPointers = mutableMapOf<LocalDate, CalendarEvent>()

    private val dateFormatMonth = DateTimeFormatter.ofPattern("MMMM - yyyy")
    private val selectedDateFormat = DateTimeFormatter.ofPattern("d MMMM - yyyy")
    private val dayFormat = DateTimeFormatter.ofPattern("d")
    private val firestoreDayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val user = Firebase.auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.calendar_view, container, false)

        calendarView = root.findViewById(R.id.calendarView)
        diaryRecordsLayout = root.findViewById(R.id.diaryRecords)
        selectedDateText = root.findViewById(R.id.selectedDateText)
        foodErrorText = root.findViewById(R.id.foodRecordErrorText)
        diaryProgressBar = root.findViewById(R.id.diaryProgressBar)
        foodsRecyclerView = root.findViewById(R.id.foodsRecyclerView)
        symptomsTextLayout = root.findViewById(R.id.symptomsTextLayout)
        symptomsEditText = root.findViewById(R.id.symptomsEditText)
        tabLayoutFoodSymptom = root.findViewById(R.id.tabLayoutFoodSymptom)
        symptomsSubmitButton = root.findViewById(R.id.symptomsSubmitButton)
        addFoodFab = root.findViewById(R.id.addFoodFab)


        addFoodFab.setOnClickListener {
            val dialog = AddFoodDialogFragment(firestoreDayFormat.format(selectedDate), null)
            dialog.setTargetFragment(this, ADD_FOOD_REQUEST_CODE)
            dialog.show(parentFragmentManager, "Add Food")
        }


        symptomsSubmitButton.setOnClickListener {
            saveSymptoms(dateFormatMonth.format(selectedDate), dayFormat.format(selectedDate), firestoreDayFormat.format(selectedDate))
        }


        tabLayoutFoodSymptom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab!!.text) {
                    "Foods" -> {
                        foodsRecyclerView.visibility = View.VISIBLE
                        addFoodFab.visibility = View.VISIBLE
                        symptomsTextLayout.visibility = View.GONE
                        symptomsSubmitButton.visibility = View.GONE
                    }

                    "Symptoms" -> {
                        foodsRecyclerView.visibility = View.GONE
                        addFoodFab.visibility = View.GONE
                        symptomsTextLayout.visibility = View.VISIBLE
                        symptomsSubmitButton.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(11)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, currentMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        foodsRecyclerView.layoutManager = LinearLayoutManager(context)

        adapter = FoodsAdapter(this, foodsArray)
        foodsRecyclerView.adapter = adapter

        class DayViewContainer(view: View) : ViewContainer(view) {
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
            selectDate(it.yearMonth.atDay(1));
            loadMonthEventsFromFirestore(dateFormatMonth.format(it.yearMonth))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val parentLayout = view.findViewById<ViewGroup>(R.id.dayLinearLayout)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                if (container.parentLayout.tag == null) {
                    container.parentLayout.tag = month.yearMonth
                    container.parentLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            tv.text = daysOfWeekFromLocale()[index].name.first().toString()
                            tv.setTextColor(context!!.getColor(R.color.black))
                        }
                }
            }
        }


        loadMonthEventsFromFirestore(dateFormatMonth.format(currentMonth))

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
            val tab = tabLayoutFoodSymptom.getTabAt(0)
            tabLayoutFoodSymptom.selectTab(tab)
            loadDiaryRecords(dateFormatMonth.format(date), date.dayOfMonth.toString())
        }
    }

    private fun loadDiaryRecords(monthYear: String, day: String) {
        foodsArray.clear()

        foodsRecyclerView.visibility = View.GONE
        symptomsTextLayout.visibility = View.GONE
        symptomsSubmitButton.visibility = View.GONE

        val docRef = db.collection("users").document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val eventsCollection = docRef.collection("events")

                val foodsDocument = eventsCollection
                    .document("foods")
                    .collection("months")
                    .document(monthYear)
                    .collection("days")
                    .document(day)

                foodsDocument.get().addOnSuccessListener { foodsDocSnapshot ->
                    if (foodsDocSnapshot.exists()) {
                        val result = foodsDocSnapshot.toObject(FoodDiaryArrayClass::class.java)
                        if (result?.foods != null) {
                            foodsArray.addAll(result.foods)
                            adapter.notifyDataSetChanged()
                            foodsRecyclerView.visibility = View.VISIBLE
                        }
                    }
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }

        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val eventsCollection = docRef.collection("events")

                val symptomsDocument = eventsCollection
                    .document("symptoms")
                    .collection("months")
                    .document(monthYear)
                    .collection("days")
                    .document(day)

                symptomsDocument.get().addOnSuccessListener { symptomsDocSnapshot ->
                    if (symptomsDocSnapshot.exists()) {
                        val result = symptomsDocSnapshot.toObject(Symptoms::class.java)

                        if (result != null) {
                            symptomsEditText.text = SpannableStringBuilder(result.symptoms)
                        } else {
                            symptomsEditText.text =
                                SpannableStringBuilder(getString(R.string.symptoms_error))
                        }
                    } else {
                        symptomsEditText.text =
                            SpannableStringBuilder(getString(R.string.symptoms_error))
                    }

                    currentSymptomsText = symptomsEditText.text.toString()
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }
    }


    private fun loadMonthEventsFromFirestore(monthYear: String) {

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
                            events = result.events
                            setupMonthEntries()
                        }
                    }
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
            val snackbar =
                Snackbar.make(calendarView, "Could not load calendar events", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }


    private fun setupMonthEntries() {

        events.forEach {
            eventPointers[LocalDate.parse(it.day)] = it
        }

        calendarView.notifyCalendarChanged()
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


    private fun saveSymptoms(monthYear: String, day: String, date: String) {

        if (symptomsEditText.text.toString() != currentSymptomsText) {
            val db = FirebaseFirestore.getInstance()
            val users = db.collection("users")

            val symptomsData = hashMapOf(
                "symptoms" to symptomsEditText.text.toString()
            )

            users.document(Firebase.auth.uid.toString()).collection("events").document("symptoms")
                .collection("months").document(monthYear).collection("days").document(day).set(symptomsData)

            var exists = false

            events.forEach {
                if (it.day == date) {
                    it.symptom = true
                    exists = true
                    addPointerToFirestore(monthYear)
                }
            }

            if (!exists) {
                val newEvent = CalendarEvent(date, food = false, symptom = true)
                events.add(newEvent)
                addPointerToFirestore(monthYear)
            }
        }
    }


    private fun addFoodPointer(monthYear: String, date: String) {
        var exists = false

        events.forEach {
            if (it.day == date) {
                it.food = true
                exists = true
                addPointerToFirestore(monthYear)
            }
        }

        if (!exists) {
            val newEvent = CalendarEvent(date, food = true, symptom = false)
            events.add(newEvent)
            addPointerToFirestore(monthYear)
        }
    }


    private fun addPointerToFirestore(monthYear: String) {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("events").document("eventPointers")
            .collection("months").document(monthYear).set(mapOf("events" to events)).addOnSuccessListener { setupMonthEntries() }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            val monthYear = dateFormatMonth.format(firestoreDayFormat.parse(data!!.getStringExtra("date")))
            val date = data.getStringExtra("date")

            addFoodPointer(monthYear, date!!)
            loadDiaryRecords(dateFormatMonth.format(selectedDate), selectedDate!!.dayOfMonth.toString())
        }
    }


    companion object {
        const val ADD_FOOD_REQUEST_CODE = 2001
    }
}

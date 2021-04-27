package com.coaker.foodlabelapp

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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

/**
 * A class used to handle how the diary feature in the application operates. The calendar view was
 * built using the calendar view library here https://github.com/kizitonwose/CalendarView.
 *
 * @author Sean Coaker
 * @since 1.0
 */
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


    /**
     * A function that is called when the fragment is created.
     *
     * @param[inflater] Inflater used to inflate the layout in this fragment.
     * @param[container] Contains the content of the fragment.
     * @param[savedInstanceState] Any previous saved instance of the fragment.
     *
     * @return[View] The view that has been created.
     */
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

        // Switches layouts when the user taps either the food or symptom tab in each day
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

        // Sets the range of months to be shown
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(11)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, currentMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        foodsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Sets the adapter for the food list of each day
        adapter = FoodsAdapter(this, foodsArray)
        foodsRecyclerView.adapter = adapter

        /**
         * An inner class used to setup each day container of the calendar view
         */
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

                    // Sets the appearance of a day depending on what type of day it is.
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
                                foodNotifView.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor(Variables.foodColour))
                                symptomNotifView.isVisible = eventPointers[day.date]!!.symptom
                                symptomNotifView.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor(Variables.symptomColour))

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

        // Updates fragment title and reads from firestore when month is changed by user
        calendarView.monthScrollListener = {
            activity?.title = dateFormatMonth.format(it.yearMonth)
            loadMonthEventsFromFirestore(dateFormatMonth.format(it.yearMonth))
        }

        /**
         * An inner class used to setup a layout to handle the month view.
         */
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val parentLayout = view.findViewById<ViewGroup>(R.id.dayLinearLayout)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                // Updates the date displayed in the textbox at the base of the calendar view
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


    /**
     * A function used when the fragment is resumed from a paused state.
     */
    override fun onResume() {
        super.onResume()

        // Selects today's date automatically
        selectDate(today)
    }


    /**
     * Function used to change the selected date in the calendar view.
     *
     * @param[date] The date to change to
     */
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


    /**
     * A function used to import records of foods and symptoms for the selected date
     *
     * @param[monthYear] The month and year to read from
     * @param[day] The day to be read from
     */
    private fun loadDiaryRecords(monthYear: String, day: String) {
        foodsArray.clear()

        foodsRecyclerView.visibility = View.GONE
        symptomsTextLayout.visibility = View.GONE
        symptomsSubmitButton.visibility = View.GONE

        val docRef = db.collection("users").document(user!!.uid)

        // Loads food records
        docRef.get().addOnSuccessListener {

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

        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }

        // Loads symptom records
        docRef.get().addOnSuccessListener {

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

        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }
    }


    /**
     * A function used to load event pointers for the month being viewed
     *
     * @param[monthYear] The month and year to read data from
     */
    private fun loadMonthEventsFromFirestore(monthYear: String) {

        val docRef = db.collection("users").document(user!!.uid)
        docRef.get().addOnSuccessListener {

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

        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
            val snackbar =
                Snackbar.make(calendarView, "Could not load calendar events", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }


    /**
     * A function used to add the event pointers read from firestore into the calendar view.
     */
    private fun setupMonthEntries() {

        events.forEach {
            eventPointers[LocalDate.parse(it.day)] = it
        }

        calendarView.notifyCalendarChanged()
    }


    /**
     * A function used to setup days of the week from the users locale. This method can be found here
     * https://github.com/kizitonwose/CalendarView.
     *
     * @return[Array<DayOfWeek>] An array of the days of the week
     */
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


    /**
     * A function used to save a new symptom record to Firestore
     *
     * @param[monthYear] The month and year to save the data in
     * @param[day] The day to save the data in
     * @param[date] The full date to save the data in
     */
    private fun saveSymptoms(monthYear: String, day: String, date: String) {

        // Checks to see if the symptoms have been edited before it overwrites old data.
        if (symptomsEditText.text.toString() != currentSymptomsText) {
            val db = FirebaseFirestore.getInstance()
            val users = db.collection("users")

            val symptomsData = hashMapOf(
                "symptoms" to symptomsEditText.text.toString()
            )

            users.document(Firebase.auth.uid.toString()).collection("events").document("symptoms")
                .collection("months").document(monthYear).collection("days").document(day).set(symptomsData)

            var exists = false

            // Checks to see if an event exists already for the current date and adds a symptom pointer if it exists
            events.forEach {
                if (it.day == date) {
                    it.symptom = true
                    exists = true
                    addPointerToFirestore(monthYear)
                }
            }

            // Creates a new event if the event doesn't exist already for this date.
            if (!exists) {
                val newEvent = CalendarEvent(date, food = false, symptom = true)
                events.add(newEvent)
                addPointerToFirestore(monthYear)
            }
        }
    }


    /**
     * A function used to add a food pointer to the list of events that already exists
     *
     * @param[monthYear] The month and year to save the pointer to
     * @param[date] The full date to save the pointer to
     */
    private fun addFoodPointer(monthYear: String, date: String) {
        var exists = false

        // Checks to see if an event exists already for the current date and adds a food pointer if it exists
        events.forEach {
            if (it.day == date) {
                it.food = true
                exists = true
                addPointerToFirestore(monthYear)
            }
        }

        // Creates a new event if the event doesn't exist already for this date.
        if (!exists) {
            val newEvent = CalendarEvent(date, food = true, symptom = false)
            events.add(newEvent)
            addPointerToFirestore(monthYear)
        }
    }


    /**
     * A function used to add all event pointers to the Firestore database
     *
     * @param[monthYear] The month and year to store the pointers to
     */
    private fun addPointerToFirestore(monthYear: String) {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("events").document("eventPointers")
            .collection("months").document(monthYear).set(mapOf("events" to events)).addOnSuccessListener { setupMonthEntries() }
    }


    /**
     * A function used to handle data when the user returns from adding a food item dialog.
     *
     * @param[requestCode] The request code sent to the add food dialog
     * @param[resultCode] The code returned from the add food dialog
     * @param[data] The data returned from the add food dialog
     */
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

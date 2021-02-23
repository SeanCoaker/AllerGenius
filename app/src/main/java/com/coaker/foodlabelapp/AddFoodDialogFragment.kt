package com.coaker.foodlabelapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class AddFoodDialogFragment(private val date: String, private val food: FoodDiaryItem?) :
    DialogFragment() {

    private val dateFormatMonth = DateTimeFormatter.ofPattern("MMMM - yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.add_food_layout, container, false)

        val datePicker: DatePicker = root.findViewById(R.id.datePicker)
        val dateTextLayout: TextInputLayout = root.findViewById(R.id.dateTextLayout)
        val dateEditText: TextInputEditText = root.findViewById(R.id.dateEditText)
        val itemEditText: TextInputEditText = root.findViewById(R.id.itemEditText)
        val barcodeEditText: TextInputEditText = root.findViewById(R.id.barcodeEditText)
        val quantityEditText: TextInputEditText = root.findViewById(R.id.quantityEditText)
        val ingredientsEditText: TextInputEditText = root.findViewById(R.id.ingredientsEditText)
        val tracesEditText: TextInputEditText = root.findViewById(R.id.tracesEditText)
        val addFoodButton: Button = root.findViewById(R.id.foodAddButton)

        val dateSplit = date.split("-")

        if (targetRequestCode == 2001) {

            datePicker.visibility = View.GONE
            dateTextLayout.visibility = View.VISIBLE
            dateEditText.text = SpannableStringBuilder(date)
        } else {

            datePicker.visibility = View.VISIBLE
            dateTextLayout.visibility = View.GONE

            val year = dateSplit[0].toInt()
            val month = dateSplit[1].toInt()

            datePicker.updateDate(year, month, dateSplit[2].toInt())
            datePicker.maxDate = System.currentTimeMillis()
            val twelveMonthsAgo = Calendar.getInstance()

            if (month == 12) {
                twelveMonthsAgo.set(year - 1, month - 1, 1)
            } else {
                twelveMonthsAgo.set(year - 1, month, 1)
            }

            datePicker.minDate = twelveMonthsAgo.timeInMillis
        }

        if (food != null) {
            itemEditText.text = SpannableStringBuilder(food.item)
            barcodeEditText.text = SpannableStringBuilder(food.barcode)
            ingredientsEditText.text = SpannableStringBuilder(food.ingredients)
            tracesEditText.text = SpannableStringBuilder(food.traces)
        }

        addFoodButton.setOnClickListener {
            var editTextError = false

            if (TextUtils.isEmpty(itemEditText.text)) {
                editTextError = true
                itemEditText.error = "This field cannot be blank"
            }

            if (TextUtils.isEmpty(barcodeEditText.text)) {
                editTextError = true
                barcodeEditText.error = "This field cannot be blank"
            }

            if (TextUtils.isEmpty(quantityEditText.text)) {
                editTextError = true
                quantityEditText.error = "This field cannot be blank"
            }

            if (TextUtils.isEmpty(ingredientsEditText.text)) {
                editTextError = true
                ingredientsEditText.error = "This field cannot be blank"
            }

            if (TextUtils.isEmpty(tracesEditText.text)) {
                editTextError = true
                tracesEditText.error = "This field cannot be blank"
            }

            if (editTextError) {
                val snackbar =
                    Snackbar.make(it, "One or more entries are empty", Snackbar.LENGTH_LONG)
                snackbar.show()
            } else {

                val enteredDate: String
                val day: String

                if (targetRequestCode == 2001) {
                    enteredDate = date
                    day = dateSplit[2].toInt().toString()
                } else {
                    day = String.format("%02d", datePicker.dayOfMonth)
                    enteredDate = datePicker.year.toString() + "-" + String.format("%02d", datePicker.month + 1) + "-" + day
                }

                val food = FoodDiaryItem(
                    barcodeEditText.text.toString(),
                    itemEditText.text.toString(), ingredientsEditText.text.toString(),
                    tracesEditText.text.toString(), quantityEditText.text.toString()
                )

                val newDate = LocalDate.parse(enteredDate)
                saveFood(dateFormatMonth.format(newDate), day, food)
                val intent = Intent()
                intent.putExtra("date", enteredDate)
                intent.putExtra("day", day)
                targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                dismiss()
            }

        }

        return root
    }


    override fun onResume() {
        super.onResume()

        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    private fun saveFood(monthYear: String, day: String, food: FoodDiaryItem) {

        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        var exists = false

        users.document(Firebase.auth.uid.toString()).collection("events").document("foods")
            .collection("months").document(monthYear).collection("days")
            .document(day.toInt().toString()).get().addOnSuccessListener {
                exists = it.exists()
            }

        if (exists) {
            users.document(Firebase.auth.uid.toString()).collection("events").document("foods")
                .collection("months").document(monthYear).collection("days")
                .document(day.toInt().toString()).update("foods", FieldValue.arrayUnion(food))
        } else {
            val tempArrayObj = ArrayList<FoodDiaryItem>()
            tempArrayObj.add(food)

            users.document(Firebase.auth.uid.toString()).collection("events").document("foods")
                .collection("months").document(monthYear).collection("days")
                .document(day.toInt().toString()).set(mapOf("foods" to tempArrayObj))
        }
    }
}
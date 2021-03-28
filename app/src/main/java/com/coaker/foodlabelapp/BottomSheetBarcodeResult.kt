package com.coaker.foodlabelapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.underline
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.errorprone.annotations.Var
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class BottomSheetBarcodeResult(private val fragment: ScannerFragment?) : BottomSheetDialogFragment() {

    companion object {
        const val KCAL_TO_KJ_FACTOR = 4.184
        const val ADD_FOOD_REQUEST_CODE = 2002
    }

    private lateinit var root: View

    private lateinit var servingSize: String
    private lateinit var energyUnit: String
    private lateinit var foodScoreText: TextView
    private var energy100 = 0.0
    private var fat100 = 0.0
    private var saturatedFat100 = 0.0
    private var carbs100 = 0.0
    private var sugar100 = 0.0
    private var fibre100 = 0.0
    private var protein100 = 0.0
    private var salt100 = 0.0

    private val firestoreDayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateFormatMonth = DateTimeFormatter.ofPattern("MMMM - yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        root = inflater.inflate(R.layout.barcode_sheet_fragment, container, false)

        val bundle = arguments

        val productId = bundle!!.getString("productId")
        val displayId = bundle.getString("displayValue")

        val productName = bundle.getString("product_name")

        servingSize = bundle.getString("servingSize").toString()
        energyUnit = bundle.getString("energyUnit").toString()
        energy100 = bundle.getDouble("energy100")
        fat100 = bundle.getDouble("fat100")
        saturatedFat100 = bundle.getDouble("saturatedFat100")
        carbs100 = bundle.getDouble("carbs100")
        sugar100 = bundle.getDouble("sugar100")
        fibre100 = bundle.getDouble("fibre100")
        protein100 = bundle.getDouble("protein100")
        salt100 = bundle.getDouble("salt100")

        val ingredients = bundle.getString("ingredients")
        val additives = bundle.getString("additives")

        val allergens = bundle.getString("allergens")
        val traces = bundle.getString("traces")

        foodScoreText = root.findViewById(R.id.textViewFoodScore)

        when (val scoreLetter = calculateFoodScore(fat100, saturatedFat100, sugar100, salt100)) {
            "A" -> {
                foodScoreText.text = scoreLetter
                foodScoreText.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(Variables.trafficLightA))
            }

            "B" -> {
                foodScoreText.text = scoreLetter
                foodScoreText.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(Variables.trafficLightB))
            }

            "C" -> {
                foodScoreText.text = scoreLetter
                foodScoreText.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(Variables.trafficLightC))
            }
        }

        val displayValText: TextView = root.findViewById(R.id.editTextID)
        displayValText.isEnabled = false
        displayValText.inputType = InputType.TYPE_NULL
        displayValText.text = displayId

        val productIdText: TextView = root.findViewById(R.id.editTextDisplayID)
        productIdText.isEnabled = false
        productIdText.inputType = InputType.TYPE_NULL
        productIdText.text = productId

        val productNameText: TextView = root.findViewById(R.id.editTextFoodName)
        productNameText.text = productName


        val tabLayout: TabLayout = root.findViewById(R.id.tabLayout)

        val ingredientsLayout: View = root.findViewById(R.id.ingredientsLayout)
        val ingredientsListTextView: TextView =
            ingredientsLayout.findViewById(R.id.ingredientsListTextView)
        val additivesListTextView: TextView = ingredientsLayout.findViewById(R.id.additivesTextView)

        findAllergens(ingredients!!, ingredientsListTextView)

        val additivesList =
            SpannableStringBuilder().bold { append("Additives:") }.append("\n$additives")
        additivesListTextView.text = additivesList


        val nutritionLayout: View = root.findViewById(R.id.nutritionLayout)
        val nutritionTable: TableLayout = nutritionLayout.findViewById(R.id.nutritionTable)

        setupNutritionTable(nutritionTable)


        val allergensLayout: View = root.findViewById(R.id.allergensLayout)
        val allergensTextView: TextView = allergensLayout.findViewById(R.id.allergensListTextView)
        val tracesTextView: TextView = allergensLayout.findViewById(R.id.tracesTextView)

        val allergensList =
            SpannableStringBuilder().bold { append("Allergens:") }.append("\n$allergens")
        allergensTextView.text = allergensList

        val tracesArray = traces!!.split(",")

        val tracesList = SpannableStringBuilder()

        if (tracesArray[0].contains("en:")) {
            tracesArray.forEach {
                tracesList.append(it.drop(3).capitalize(Locale.UK) + ", ")
            }
        } else {
            tracesArray.forEach {
                val formattedTrace = it.capitalize(Locale.UK)
                tracesList.append("$formattedTrace, ")
            }
        }

        tracesTextView.text =
            SpannableStringBuilder().bold { append("Traces: ") }.append("$tracesList\n")

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("SimpleDateFormat")
            override fun onTabSelected(tab: TabLayout.Tab) {

                when (tab.text) {
                    "Ingredients" -> {
                        nutritionLayout.visibility = View.GONE
                        allergensLayout.visibility = View.GONE
                        ingredientsLayout.visibility = View.VISIBLE
                    }

                    "Nutrition" -> {
                        ingredientsLayout.visibility = View.GONE
                        allergensLayout.visibility = View.GONE
                        nutritionLayout.visibility = View.VISIBLE
                    }

                    "Allergens" -> {
                        nutritionLayout.visibility = View.GONE
                        ingredientsLayout.visibility = View.GONE
                        allergensLayout.visibility = View.VISIBLE
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        val addFoodButton = root.findViewById<Button>(R.id.bottomSheetAddFoodButton)
        addFoodButton.setOnClickListener {
            val food = FoodDiaryItem(
                productId,
                productName,
                ingredients.dropLast(1),
                tracesList.toString().dropLast(1),
                "0g"
            )
            val dialog = AddFoodDialogFragment(firestoreDayFormat.format(LocalDate.now()), food)
            dialog.setTargetFragment(this, ADD_FOOD_REQUEST_CODE)
            dialog.show(parentFragmentManager, "Add Food")
        }


        return root
    }

    private fun findAllergens(ingredients: String, ingredientsBox: TextView) {
        val output = SpannableStringBuilder().bold { append("Ingredients:\n") }
        val ingredientList = ingredients.split(",")
        var containsAllergen = false;

        ingredientList.forEach { ingredient ->
            var isAllergen = false
            Variables.allergyList.forEach { allergy ->
                if (ingredient.contains(allergy, true)) {

                    val colour = Color.parseColor(Variables.allergyColour)

                    when (Variables.allergyBIU) {
                        "NIL" -> {
                            output.color(colour) { append(ingredient) }.append(",")
                        }

                        "B" -> {
                            output.color(colour) { bold { append(ingredient) } }.append(",")
                        }

                        "I" -> {
                            output.color(colour) { italic { append(ingredient) } }.append(",")
                        }

                        "U" -> {
                            output.color(colour) { underline { append(ingredient) } }.append(",")
                        }

                        "BI" -> {
                            output.color(colour) { bold { italic { append(ingredient) } } }
                                .append(",")
                        }

                        "BU" -> {
                            output.color(colour) { bold { underline { append(ingredient) } } }
                                .append(",")
                        }

                        "IU" -> {
                            output.color(colour) { italic { underline { append(ingredient) } } }
                                .append(",")
                        }

                        "BIU" -> {
                            output.color(colour) { bold { italic { underline { append(ingredient) } } } }
                                .append(",")
                        }
                    }

                    isAllergen = true
                    containsAllergen = true;
                }
            }

            if (!isAllergen) {
                output.append("$ingredient,")
            }
        }

        if (containsAllergen) {
            foodScoreText.text = "X"
            foodScoreText.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(Variables.trafficLightX))
        }

        ingredientsBox.text = output.delete(output.length - 2, output.length)
    }

    private fun setupNutritionTable(table: TableLayout) {
        val servingSizeSplitArray = servingSize.split("g")

        val energyKj100TextView = table.findViewById<TextView>(R.id.energyKj100)
        val energyKjServingTextView = table.findViewById<TextView>(R.id.energyKjServing)
        val energyKcal100TextView = table.findViewById<TextView>(R.id.energyKcal100Text)
        val energyKcalServingTextView = table.findViewById<TextView>(R.id.energyKcalServingText)

        val stringEnergyKj100: String
        val stringEnergyKjServing: String
        val stringEnergyKcal100: String
        val stringEnergyKcalServing: String

        if (servingSizeSplitArray[0] != "") {
            val servingSizeAsInt = servingSizeSplitArray[0].toInt()
            val servingFactor = 100 / servingSizeAsInt

            val servingText = "Per Serving (" + servingSizeAsInt + "g)"
            table.findViewById<TextView>(R.id.perServingTextView).text = servingText

            if (energyUnit == "kJ") {
                stringEnergyKj100 = "$energy100 $energyUnit"
                energyKj100TextView.text = stringEnergyKj100


                val energyServingKj =
                    BigDecimal(energy100 / servingFactor).setScale(1, RoundingMode.HALF_UP)

                stringEnergyKjServing = "$energyServingKj $energyUnit"
                energyKjServingTextView.text = stringEnergyKjServing


                val energyKcal100FromKj =
                    BigDecimal(energy100 / KCAL_TO_KJ_FACTOR).setScale(1, RoundingMode.HALF_UP)
                stringEnergyKcal100 = "$energyKcal100FromKj kcal"
                energyKcal100TextView.text = stringEnergyKcal100

                val energyServingKcalFromKj =
                    BigDecimal((energy100 / KCAL_TO_KJ_FACTOR) / servingFactor).setScale(
                        1,
                        RoundingMode.HALF_UP
                    )
                stringEnergyKcalServing = "$energyServingKcalFromKj kcal"
                energyKcalServingTextView.text = stringEnergyKcalServing
            } else {
                stringEnergyKcal100 = "$energy100 $energyUnit"
                energyKcal100TextView.text = stringEnergyKcal100

                val energyServingKcal = energy100 / servingFactor

                stringEnergyKcalServing = "$energyServingKcal $energyUnit"
                energyKcalServingTextView.text = stringEnergyKcalServing


                val energyKj100FromKcal =
                    BigDecimal(energy100 * KCAL_TO_KJ_FACTOR).setScale(1, RoundingMode.HALF_UP)
                stringEnergyKj100 = "$energyKj100FromKcal kJ"
                energyKj100TextView.text = stringEnergyKj100

                val energyServingKjFromKcal =
                    BigDecimal((energy100 * KCAL_TO_KJ_FACTOR) / servingFactor).setScale(
                        1,
                        RoundingMode.HALF_UP
                    )
                stringEnergyKjServing = "$energyServingKjFromKcal kJ"
                energyKjServingTextView.text = stringEnergyKjServing
            }

            val stringFat100 = fat100.toString() + "g"
            table.findViewById<TextView>(R.id.fat100Text).text = stringFat100
            val stringFatServing =
                BigDecimal(fat100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.fatServingText).text = stringFatServing

            val stringSaturates100 = saturatedFat100.toString() + "g"
            table.findViewById<TextView>(R.id.saturates100Text).text = stringSaturates100
            val stringSaturatesServing =
                BigDecimal(saturatedFat100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.saturatesServingText).text = stringSaturatesServing

            val stringCarbs100 = carbs100.toString() + "g"
            table.findViewById<TextView>(R.id.carb100Text).text = stringCarbs100
            val stringCarbsServing =
                BigDecimal(carbs100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.carbServingText).text = stringCarbsServing

            val stringSugar100 = sugar100.toString() + "g"
            table.findViewById<TextView>(R.id.sugars100Text).text = stringSugar100
            val stringSugarServing =
                BigDecimal(sugar100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.sugarsServingText).text = stringSugarServing

            val stringFibre100 = fibre100.toString() + "g"
            table.findViewById<TextView>(R.id.fibre100Text).text = stringFibre100
            val stringFibreServing =
                BigDecimal(fibre100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.fibreServingText).text = stringFibreServing

            val stringProtein100 = protein100.toString() + "g"
            table.findViewById<TextView>(R.id.protein100Text).text = stringProtein100
            val stringProteinServing =
                BigDecimal(protein100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.proteinServingText).text = stringProteinServing

            val stringSalt100 = salt100.toString() + "g"
            table.findViewById<TextView>(R.id.salt100Text).text = stringSalt100
            val stringSaltServing =
                BigDecimal(salt100 / servingFactor).setScale(1, RoundingMode.HALF_UP)
                    .toString() + "g"
            table.findViewById<TextView>(R.id.saltServingText).text = stringSaltServing

        } else {

            if (energyUnit == "kJ") {
                stringEnergyKj100 = "$energy100 $energyUnit"
                energyKj100TextView.text = stringEnergyKj100

                val energyKcal100FromKj =
                    BigDecimal(energy100 / KCAL_TO_KJ_FACTOR).setScale(1, RoundingMode.HALF_UP)
                stringEnergyKcal100 = "$energyKcal100FromKj kcal"
                energyKcal100TextView.text = stringEnergyKcal100

            } else {
                stringEnergyKcal100 = "$energy100 $energyUnit"
                energyKcal100TextView.text = stringEnergyKcal100

                val energyKj100FromKcal =
                    BigDecimal(energy100 * KCAL_TO_KJ_FACTOR).setScale(1, RoundingMode.HALF_UP)
                stringEnergyKj100 = "$energyKj100FromKcal kJ"
                energyKj100TextView.text = stringEnergyKj100
            }

            val stringFat100 = fat100.toString() + "g"
            table.findViewById<TextView>(R.id.fat100Text).text = stringFat100

            val stringSaturates100 = saturatedFat100.toString() + "g"
            table.findViewById<TextView>(R.id.saturates100Text).text = stringSaturates100

            val stringCarbs100 = carbs100.toString() + "g"
            table.findViewById<TextView>(R.id.carb100Text).text = stringCarbs100

            val stringSugar100 = sugar100.toString() + "g"
            table.findViewById<TextView>(R.id.sugars100Text).text = stringSugar100

            val stringFibre100 = fibre100.toString() + "g"
            table.findViewById<TextView>(R.id.fibre100Text).text = stringFibre100

            val stringProtein100 = protein100.toString() + "g"
            table.findViewById<TextView>(R.id.protein100Text).text = stringProtein100

            val stringSalt100 = salt100.toString() + "g"
            table.findViewById<TextView>(R.id.salt100Text).text = stringSalt100
        }
    }

    private fun calculateFoodScore(
        fat: Double,
        saturates: Double,
        sugar: Double,
        salt: Double
    ): String {
        var greenValue = 0
        var amberValue = 0
        var redValue = 0

        when {
            fat <= Variables.fatLow -> greenValue++
            fat > Variables.fatHigh -> redValue++
            else -> amberValue++
        }

        when {
            saturates <= Variables.saturatesLow -> greenValue++
            saturates > Variables.saturatesHigh -> redValue++
            else -> amberValue++
        }

        when {
            sugar <= Variables.sugarLow -> greenValue++
            sugar > Variables.sugarHigh -> redValue++
            else -> amberValue++
        }

        when {
            salt <= Variables.saltLow -> greenValue++
            salt > Variables.saltHigh -> redValue++
            else -> amberValue++
        }

        when (greenValue) {
            4 -> {
                return "A"
            }
            3 -> {
                return if (redValue == 1) {
                    "B"
                } else {
                    "A"
                }
            }
            2 -> {
                return "B"
            }
            1 -> {
                return if (amberValue >= 2) {
                    "B"
                } else {
                    "C"
                }
            }
            else -> {
                return "C"
            }
        }
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        fragment?.setupAnalyser()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            val monthYear =
                dateFormatMonth.format(firestoreDayFormat.parse(data!!.getStringExtra("date")))
            val date = data.getStringExtra("date")

            addFoodPointer(monthYear, date!!)
        }
    }


    private fun addFoodPointer(monthYear: String, date: String) {

        var events = ArrayList<CalendarEvent>()
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(Firebase.auth.currentUser!!.uid)

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
                        }

                        var exists = false

                        events.forEach {
                            if (it.day == date) {
                                it.food = true
                                exists = true
                                addPointerToFirestore(monthYear, events)
                            }
                        }

                        if (!exists) {
                            val newEvent = CalendarEvent(date, food = true, symptom = false)
                            events.add(newEvent)
                            addPointerToFirestore(monthYear, events)
                        }
                    }
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
            val snackbar =
                Snackbar.make(
                    requireView(),
                    "Could not save food to the cloud",
                    Snackbar.LENGTH_LONG
                )
            snackbar.show()
        }
    }

    private fun addPointerToFirestore(monthYear: String, events: ArrayList<CalendarEvent>) {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("events").document("eventPointers")
            .collection("months").document(monthYear).set(mapOf("events" to events))
    }
}
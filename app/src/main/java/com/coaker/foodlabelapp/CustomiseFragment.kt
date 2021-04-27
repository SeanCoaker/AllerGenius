package com.coaker.foodlabelapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * A class designed to handle the customisation feature of the application.
 *
 * @author Sean Coaker
 * @since 1.0
 */
class CustomiseFragment : Fragment(), View.OnClickListener {

    private lateinit var root: View

    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var underlineButton: Button

    private lateinit var foodScoreA: TextView
    private lateinit var foodScoreB: TextView
    private lateinit var foodScoreC: TextView
    private lateinit var foodScoreX: TextView

    private lateinit var lowFatSeek: SeekBar
    private lateinit var highFatSeek: SeekBar
    private lateinit var lowSaturatesSeek: SeekBar
    private lateinit var highSaturatesSeek: SeekBar
    private lateinit var lowSugarSeek: SeekBar
    private lateinit var highSugarSeek: SeekBar
    private lateinit var lowSaltSeek: SeekBar
    private lateinit var highSaltSeek: SeekBar

    private lateinit var lowFatText: TextView
    private lateinit var highFatText: TextView
    private lateinit var lowSaturatesText: TextView
    private lateinit var highSaturatesText: TextView
    private lateinit var lowSugarText: TextView
    private lateinit var highSugarText: TextView
    private lateinit var lowSaltText: TextView
    private lateinit var highSaltText: TextView

    private var allergyColour = Color.parseColor(Variables.allergyColour)
    private var foodColour = Color.parseColor(Variables.foodColour)
    private var symptomColour = Color.parseColor(Variables.symptomColour)


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
    ): View {

        root = inflater.inflate(R.layout.fragment_customise, container, false)

        val colorsButton = root.findViewById<Button>(R.id.colorsButton)

        colorsButton.setBackgroundColor(allergyColour)

        colorsButton.setOnClickListener {
            ColorPickerFragment("allergy").show(childFragmentManager, "Colour Picker")
        }

        boldButton = root.findViewById(R.id.boldButton)
        italicButton = root.findViewById(R.id.italicButton)
        underlineButton = root.findViewById(R.id.underlineButton)

        foodScoreA = root.findViewById(R.id.foodScoreA)
        foodScoreB = root.findViewById(R.id.foodScoreB)
        foodScoreC = root.findViewById(R.id.foodScoreC)
        foodScoreX = root.findViewById(R.id.foodScoreX)

        foodScoreA.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreA")
        }

        foodScoreB.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreB")
        }

        foodScoreC.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreC")
        }

        foodScoreX.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreX")
        }

        // Sets the colours of food scores for display
        foodScoreA.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightA))
        foodScoreB.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightB))
        foodScoreC.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightC))
        foodScoreX.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightX))

        boldButton.text = SpannableStringBuilder().bold { append("B") }
        italicButton.text = SpannableStringBuilder().italic { append("I") }
        underlineButton.text = SpannableStringBuilder().underline { append("U") }

        setupAllergyButtons()

        selectButton(boldButton)
        selectButton(italicButton)
        selectButton(underlineButton)

        boldButton.setOnClickListener(this)
        italicButton.setOnClickListener(this)
        underlineButton.setOnClickListener(this)

        setAllergyPreview()

        lowFatSeek = root.findViewById(R.id.lowFatSeek)
        highFatSeek = root.findViewById(R.id.highFatSeek)
        lowSaturatesSeek = root.findViewById(R.id.lowSaturatesSeek)
        highSaturatesSeek = root.findViewById(R.id.highSaturatesSeek)
        lowSugarSeek = root.findViewById(R.id.lowSugarSeek)
        highSugarSeek = root.findViewById(R.id.highSugarSeek)
        lowSaltSeek = root.findViewById(R.id.lowSaltSeek)
        highSaltSeek = root.findViewById(R.id.highSaltSeek)

        lowFatText = root.findViewById(R.id.textLowFat)
        highFatText = root.findViewById(R.id.textHighFat)
        lowSaturatesText = root.findViewById(R.id.textLowSaturates)
        highSaturatesText = root.findViewById(R.id.textHighSaturates)
        lowSugarText = root.findViewById(R.id.textLowSugar)
        highSugarText = root.findViewById(R.id.textHighSugar)
        lowSaltText = root.findViewById(R.id.textLowSalt)
        highSaltText = root.findViewById(R.id.textHighSalt)

        setupSeekBars()
        setupHelpButtons()

        val foodButton = root.findViewById<Button>(R.id.foodColorButton)
        val symptomButton = root.findViewById<Button>(R.id.symptomColorButton)
        val calendarDayPreview = root.findViewById<View>(R.id.calendarDayPreview)

        // Sets up the text and pointer colours for a diary day preview example
        calendarDayPreview.findViewById<TextView>(R.id.calendarDayText).text = getString(R.string.day_example)
        calendarDayPreview.findViewById<View>(R.id.foodNotifView).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.foodColour))
        calendarDayPreview.findViewById<View>(R.id.symptomNotifView).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.symptomColour))

        foodButton.setBackgroundColor(foodColour)

        foodButton.setOnClickListener {
            ColorPickerFragment("food").show(childFragmentManager, "Colour Picker")
        }

        symptomButton.setBackgroundColor(symptomColour)

        symptomButton.setOnClickListener {
            ColorPickerFragment("symptom").show(childFragmentManager, "Colour Picker")
        }

        return root
    }


    /**
     * A function called to setup the help buttons displayed in each card view to display
     * instructions to the user of how the customisation works.
     */
    private fun setupHelpButtons() {
        val helpButtonAllergies: TextView = root.findViewById(R.id.helpButtonAllergies)
        val helpButtonTraffic: TextView = root.findViewById(R.id.helpButtonTraffic)
        val helpButtonNutrition: TextView = root.findViewById(R.id.helpButtonNutrition)
        val helpButtonDiary: TextView = root.findViewById(R.id.helpButtonDiary)

        helpButtonAllergies.setOnClickListener {
            val helpText: TextView = root.findViewById(R.id.helpTextAllergies)
            val colorsButton: Button = root.findViewById(R.id.colorsButton)
            val allergyPreviewText: TextView = root.findViewById(R.id.allergyPreviewText)

            if (helpText.isVisible) {
                helpButtonAllergies.text = resources.getString(R.string.help)
                helpText.visibility = View.GONE
                colorsButton.visibility = View.VISIBLE
                boldButton.visibility = View.VISIBLE
                italicButton.visibility = View.VISIBLE
                underlineButton.visibility = View.VISIBLE
                allergyPreviewText.visibility = View.VISIBLE
            } else {
                helpButtonAllergies.text = resources.getString(R.string.x)
                helpText.visibility = View.VISIBLE
                colorsButton.visibility = View.GONE
                boldButton.visibility = View.GONE
                italicButton.visibility = View.GONE
                underlineButton.visibility = View.GONE
                allergyPreviewText.visibility = View.GONE
            }
        }

        helpButtonTraffic.setOnClickListener {
            val helpText: TextView = root.findViewById(R.id.helpTextTraffic)
            val trafficTableLayout: TableLayout = root.findViewById(R.id.trafficTableLayout)

            if (helpText.isVisible) {
                helpButtonTraffic.text = resources.getString(R.string.help)
                helpText.visibility = View.GONE
                trafficTableLayout.visibility = View.VISIBLE
            } else {
                helpButtonTraffic.text = resources.getString(R.string.x)
                helpText.visibility = View.VISIBLE
                trafficTableLayout.visibility = View.GONE
            }
        }

        helpButtonNutrition.setOnClickListener {
            val helpText: TextView = root.findViewById(R.id.helpTextNutrition)
            val textFat: TextView = root.findViewById(R.id.textFat)
            val textSaturates: TextView = root.findViewById(R.id.textSaturates)
            val textSugar: TextView = root.findViewById(R.id.textSugar)
            val textSalt: TextView = root.findViewById(R.id.textSalt)

            if (helpText.isVisible) {
                helpButtonNutrition.text = resources.getString(R.string.help)
                helpText.visibility = View.GONE

                textFat.visibility = View.VISIBLE
                textSaturates.visibility = View.VISIBLE
                textSugar.visibility = View.VISIBLE
                textSalt.visibility = View.VISIBLE

                lowFatText.visibility = View.VISIBLE
                highFatText.visibility = View.VISIBLE
                lowSaturatesText.visibility = View.VISIBLE
                highSaturatesText.visibility = View.VISIBLE
                lowSugarText.visibility = View.VISIBLE
                highSugarText.visibility = View.VISIBLE
                lowSaltText.visibility = View.VISIBLE
                highSaltText.visibility = View.VISIBLE

                lowFatSeek.visibility = View.VISIBLE
                highFatSeek.visibility = View.VISIBLE
                lowSaturatesSeek.visibility = View.VISIBLE
                highSaturatesSeek.visibility = View.VISIBLE
                lowSugarSeek.visibility = View.VISIBLE
                highSugarSeek.visibility = View.VISIBLE
                lowSaltSeek.visibility = View.VISIBLE
                highSaltSeek.visibility = View.VISIBLE

            } else {
                helpButtonNutrition.text = resources.getString(R.string.x)
                helpText.visibility = View.VISIBLE

                textFat.visibility = View.GONE
                textSaturates.visibility = View.GONE
                textSugar.visibility = View.GONE
                textSalt.visibility = View.GONE

                lowFatText.visibility = View.GONE
                highFatText.visibility = View.GONE
                lowSaturatesText.visibility = View.GONE
                highSaturatesText.visibility = View.GONE
                lowSugarText.visibility = View.GONE
                highSugarText.visibility = View.GONE
                lowSaltText.visibility = View.GONE
                highSaltText.visibility = View.GONE

                lowFatSeek.visibility = View.GONE
                highFatSeek.visibility = View.GONE
                lowSaturatesSeek.visibility = View.GONE
                highSaturatesSeek.visibility = View.GONE
                lowSugarSeek.visibility = View.GONE
                highSugarSeek.visibility = View.GONE
                lowSaltSeek.visibility = View.GONE
                highSaltSeek.visibility = View.GONE
            }
        }

        helpButtonDiary.setOnClickListener {
            val helpText: TextView = root.findViewById(R.id.helpTextDiary)
            val diaryFoodCustomiseFrame: FrameLayout = root.findViewById(R.id.diaryFoodCustomiseFrame)
            val diarySymptomCustomiseFrame: FrameLayout = root.findViewById(R.id.diarySymptomCustomiseFrame)
            val dayPreview: View = root.findViewById(R.id.calendarDayPreview)

            if (helpText.isVisible) {
                helpButtonDiary.text = resources.getString(R.string.help)
                helpText.visibility = View.GONE
                diarySymptomCustomiseFrame.visibility = View.VISIBLE
                diaryFoodCustomiseFrame.visibility = View.VISIBLE
                dayPreview.visibility = View.VISIBLE
            } else {
                helpButtonDiary.text = resources.getString(R.string.x)
                helpText.visibility = View.VISIBLE
                diaryFoodCustomiseFrame.visibility = View.GONE
                diarySymptomCustomiseFrame.visibility = View.GONE
                dayPreview.visibility = View.GONE
            }
        }
    }


    /**
     * A function created to setup the seek bars in the nutrition values customisation section.
     */
    private fun setupSeekBars() {

        val lowFatString = "${resources.getText(R.string.low)}:  ${Variables.fatLow}g"
        lowFatText.text = lowFatString
        val highFatString = "${resources.getText(R.string.high)}:  ${Variables.fatHigh}g"
        highFatText.text = highFatString
        val lowSaturatesString = "${resources.getText(R.string.low)}:  ${Variables.saturatesLow}g"
        lowSaturatesText.text = lowSaturatesString
        val highSaturatesString = "${resources.getText(R.string.high)}:  ${Variables.saturatesHigh}g"
        highSaturatesText.text = highSaturatesString
        val lowSugarString = "${resources.getText(R.string.low)}:  ${Variables.sugarLow}g"
        lowSugarText.text = lowSugarString
        val highSugarString = "${resources.getText(R.string.high)}:  ${Variables.sugarHigh}g"
        highSugarText.text = highSugarString
        val lowSaltString = "${resources.getText(R.string.low)}:  ${Variables.saltLow}g"
        lowSaltText.text = lowSaltString
        val highSaltString = "${resources.getText(R.string.high)}:  ${Variables.saltHigh}g"
        highSaltText.text = highSaltString

        // Sets up all seek bars to previously saved values
        lowFatSeek.progress = Variables.fatLow
        highFatSeek.progress = Variables.fatHigh
        lowSaturatesSeek.progress = Variables.saturatesLow
        highSaturatesSeek.progress = Variables.saturatesHigh
        lowSugarSeek.progress = Variables.sugarLow
        highSugarSeek.progress = Variables.sugarHigh
        lowSaltSeek.progress = Variables.saltLow
        highSaltSeek.progress = Variables.saltHigh

        // Seek bar changes are handled

        lowFatSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g"
                lowFatText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highFatSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g"
                highFatText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSaturatesSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g"
                lowSaturatesText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSaturatesSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g"
                highSaturatesText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSugarSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g"
                lowSugarText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSugarSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g"
                highSugarText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSaltSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g"
                lowSaltText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSaltSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            /**
             * A function called when the seek bar value is changed.
             *
             * @param[seekBar] The seek bar being changed
             * @param[progress] The value the seek bar is being changed to
             * @param[fromUser] Whether the change came from the user
             */
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g"
                highSaltText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }


    /**
     * A function used to load the color picker based on the food score being changed
     *
     * @param[id] An identification of the food score being changed
     */
    private fun loadFoodScoreColorPicker(id: String) {
        ColorPickerFragment(id).show(childFragmentManager, "Colour Picker")
    }


    /**
     * A function called to handle clicks on bold, italic or underline buttons.
     *
     * @param[v] The button being clicked on
     */
    override fun onClick(v: View) {
        selectButton(v as Button)
        setAllergyBIU()
        setAllergyPreview()
    }


    /**
     * A function called to change the display of the bold, italic and underline buttons depending
     * on if the user has selected them or not.
     *
     * @param[button] The button to be changed
     */
    private fun selectButton(button: Button) {
        if (button.isSelected) {
            button.setBackgroundColor(requireContext().getColor(R.color.light_purple))
        } else {
            button.setBackgroundColor(requireContext().getColor(R.color.dark_purple))
        }

        button.isSelected = !button.isSelected
    }


    /**
     * A function used to setup initial appearance and selections of the bold, italic or underline
     * buttons using the previously saved preferences of the user.
     */
    private fun setupAllergyButtons() {
        when (Variables.allergyBIU) {
            "NIL" -> {
                boldButton.isSelected = true
                italicButton.isSelected = true
                underlineButton.isSelected = true
            }

            "B" -> {
                boldButton.isSelected = false
                italicButton.isSelected = true
                underlineButton.isSelected = true
            }

            "I" -> {
                boldButton.isSelected = true
                italicButton.isSelected = false
                underlineButton.isSelected = true
            }

            "U" -> {
                boldButton.isSelected = true
                italicButton.isSelected = true
                underlineButton.isSelected = false
            }

            "BI" -> {
                boldButton.isSelected = false
                italicButton.isSelected = false
                underlineButton.isSelected = true
            }

            "BU" -> {
                boldButton.isSelected = false
                italicButton.isSelected = true
                underlineButton.isSelected = false
            }

            "IU" -> {
                boldButton.isSelected = true
                italicButton.isSelected = false
                underlineButton.isSelected = false
            }

            "BIU" -> {
                boldButton.isSelected = false
                italicButton.isSelected = false
                underlineButton.isSelected = false
            }
        }
    }


    /**
     * A function used to save the user's bold, italic and underline preferences globally.
     */
    private fun setAllergyBIU() {
        // Clears the previous save before adding the new preference
        Variables.allergyBIU = ""

        if (!boldButton.isSelected && !italicButton.isSelected && !underlineButton.isSelected) {
            Variables.allergyBIU = "NIL"
        } else {
            if (boldButton.isSelected) {
                Variables.allergyBIU += "B"
            }

            if (italicButton.isSelected) {
                Variables.allergyBIU += "I"
            }

            if (underlineButton.isSelected) {
                Variables.allergyBIU += "U"
            }
        }
    }


    /**
     * A function used to setup the allergy preview included in the allergy section.
     */
    fun setAllergyPreview() {
        val allergyPreviewText = root.findViewById<TextView>(R.id.allergyPreviewText)
        val ssb = SpannableStringBuilder()
        ssb.clear()

        // Allergens are shown in the preview using the preferences of the user
        when (Variables.allergyBIU) {
            "NIL" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { append("sugar") }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { append("dried yeast") }
                    .append(", dried onion, ")
                    .color(allergyColour) { append("vale of evesham tomato extract") }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "B" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { bold { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "I" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { italic { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { italic { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { italic { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "U" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { underline { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { underline { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { underline { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BI" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { bold { italic { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { italic { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { italic { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { bold { underline { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { underline { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { underline { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "IU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { italic { underline { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { italic { underline { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { italic { underline { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BIU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append(
                        "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                                "prawn cocktail seasoning contains: flavouring, "
                    )
                    .color(allergyColour) { bold { italic { underline { append("sugar") } } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { italic { underline { append("dried yeast") } } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { italic { underline { append("vale of evesham tomato extract") } } } }
                    .append(", color (paprika extract), sweetener (sucralose)")
            }
        }
    }


    /**
     * A function used to reset the allergy colour preview.
     */
    fun reloadAllergyColour() {
        allergyColour = Color.parseColor(Variables.allergyColour)
    }


    /**
     * A function used to add customisation data from the application to the user's Firestore
     * document.
     */
    private fun updateFirestore() {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("data")
            .document("customisation")
            .set(
                mapOf(
                    "allergyColour" to Variables.allergyColour,
                    "allergyBIU" to Variables.allergyBIU,
                    "trafficLightA" to Variables.trafficLightA,
                    "trafficLightB" to Variables.trafficLightB,
                    "trafficLightC" to Variables.trafficLightC,
                    "trafficLightX" to Variables.trafficLightX,
                    "fatLow" to Variables.fatLow,
                    "fatHigh" to Variables.fatHigh,
                    "saturatesLow" to Variables.saturatesLow,
                    "saturatesHigh" to Variables.saturatesHigh,
                    "sugarLow" to Variables.sugarLow,
                    "sugarHigh" to Variables.sugarHigh,
                    "saltLow" to Variables.saltLow,
                    "saltHigh" to Variables.saltHigh,
                    "foodColour" to Variables.foodColour,
                    "symptomColour" to Variables.symptomColour
                )
            )
    }


    /**
     * A function that is called when the fragment is paused.
     */
    override fun onPause() {
        super.onPause()

        // All seek bar levels are saved
        Variables.fatLow = lowFatSeek.progress
        Variables.fatHigh = highFatSeek.progress
        Variables.saturatesLow = lowSaturatesSeek.progress
        Variables.saturatesHigh = highSaturatesSeek.progress
        Variables.sugarLow = lowSugarSeek.progress
        Variables.sugarHigh = highSugarSeek.progress
        Variables.saltLow = lowSaltSeek.progress
        Variables.saltHigh = highSaltSeek.progress

        updateFirestore()
    }
}
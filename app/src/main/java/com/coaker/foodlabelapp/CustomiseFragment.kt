package com.coaker.foodlabelapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


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


    private var allergyColour = Color.parseColor(Variables.allergyColour)

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

        setupSeekBars()

        return root
    }


    private fun setupSeekBars() {

        val lowFatText: TextView = root.findViewById(R.id.textLowFat)
        val highFatText: TextView = root.findViewById(R.id.textHighFat)
        val lowSaturatesText: TextView = root.findViewById(R.id.textLowSaturates)
        val highSaturatesText: TextView = root.findViewById(R.id.textHighSaturates)
        val lowSugarText: TextView = root.findViewById(R.id.textLowSugar)
        val highSugarText: TextView = root.findViewById(R.id.textHighSugar)
        val lowSaltText: TextView = root.findViewById(R.id.textLowSalt)
        val highSaltText: TextView = root.findViewById(R.id.textHighSalt)

        val lowFatString = "${resources.getText(R.string.low)}:  ${Variables.fatLow}g/ml"
        lowFatText.text = lowFatString
        val highFatString = "${resources.getText(R.string.high)}:  ${Variables.fatHigh}g/ml"
        highFatText.text = highFatString
        val lowSaturatesString = "${resources.getText(R.string.low)}:  ${Variables.saturatesLow}g/ml"
        lowSaturatesText.text = lowSaturatesString
        val highSaturatesString = "${resources.getText(R.string.high)}:  ${Variables.saturatesHigh}g/ml"
        highSaturatesText.text = highSaturatesString
        val lowSugarString = "${resources.getText(R.string.low)}:  ${Variables.sugarLow}g/ml"
        lowSugarText.text = lowSugarString
        val highSugarString = "${resources.getText(R.string.high)}:  ${Variables.sugarHigh}g/ml"
        highSugarText.text = highSugarString
        val lowSaltString = "${resources.getText(R.string.low)}:  ${Variables.saltLow}g/ml"
        lowSaltText.text = lowSaltString
        val highSaltString = "${resources.getText(R.string.high)}:  ${Variables.saltHigh}g/ml"
        highSaltText.text = highSaltString

        lowFatSeek.progress = Variables.fatLow
        highFatSeek.progress = Variables.fatHigh
        lowSaturatesSeek.progress = Variables.saturatesLow
        highSaturatesSeek.progress = Variables.saturatesHigh
        lowSugarSeek.progress = Variables.sugarLow
        highSugarSeek.progress = Variables.sugarHigh
        lowSaltSeek.progress = Variables.saltLow
        highSaltSeek.progress = Variables.saltHigh

        lowFatSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g/ml"
                lowFatText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highFatSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g/ml"
                highFatText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSaturatesSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g/ml"
                lowSaturatesText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSaturatesSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g/ml"
                highSaturatesText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSugarSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g/ml"
                lowSugarText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSugarSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g/ml"
                highSugarText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        lowSaltSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.low)}:  ${progress}g/ml"
                lowSaltText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        highSaltSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                val string  = "${resources.getText(R.string.high)}:  ${progress}g/ml"
                highSaltText.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }


    private fun loadFoodScoreColorPicker(id: String) {
        ColorPickerFragment(id).show(childFragmentManager, "Colour Picker")
    }


    override fun onClick(v: View) {
        selectButton(v as Button)
        setAllergyBIU()
        setAllergyPreview()
    }


    private fun selectButton(button: Button) {
        if (button.isSelected) {
            button.setBackgroundColor(requireContext().getColor(R.color.light_purple))
        } else {
            button.setBackgroundColor(requireContext().getColor(R.color.dark_purple))
        }

        button.isSelected = !button.isSelected
    }

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

    private fun setAllergyBIU() {
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

    fun setAllergyPreview() {
        val allergyPreviewText = root.findViewById<TextView>(R.id.allergyPreviewText)
        val ssb = SpannableStringBuilder()
        ssb.clear()

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

    fun reloadAllergyColour() {
        allergyColour = Color.parseColor(Variables.allergyColour)
    }

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
                    "saltHigh" to Variables.saltHigh
                )
            )
    }

    override fun onPause() {
        super.onPause()

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
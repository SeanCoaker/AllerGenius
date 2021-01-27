package com.coaker.foodlabelapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CustomiseFragment: Fragment(), View.OnClickListener {

    private lateinit var root: View

    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var underlineButton: Button

    private lateinit var foodScoreA: TextView
    private lateinit var foodScoreB: TextView
    private lateinit var foodScoreC: TextView

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

        foodScoreA.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreA")
        }

        foodScoreB.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreB")
        }

        foodScoreC.setOnClickListener {
            loadFoodScoreColorPicker("foodScoreC")
        }

        foodScoreA.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightA))
        foodScoreB.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightB))
        foodScoreC.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightC))

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

        return root
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
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { append("sugar") }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { append("dried yeast") }
                    .append(", dried onion, ")
                    .color(allergyColour) { append("vale of evesham tomato extract") }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "B" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { bold { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "I" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { italic { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { italic { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { italic { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "U" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { underline { append("sugar") } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { underline { append("dried yeast") } }
                    .append(", dried onion, ")
                    .color(allergyColour) { underline { append("vale of evesham tomato extract") } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BI" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { bold { italic { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { italic { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { italic { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { bold { underline { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { bold { underline { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { bold { underline { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "IU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
                    .color(allergyColour) { italic { underline { append("sugar") } } }
                    .append(", glucose, salt, citric acid, potassium chloride, ")
                    .color(allergyColour) { italic { underline { append("dried yeast") } } }
                    .append(", dried onion, ")
                    .color(allergyColour) { italic { underline { append("vale of evesham tomato extract") } } }
                    .append(", color (paprika extract), sweetener (sucralose)")

            }
            "BIU" -> {
                allergyPreviewText.text = ssb.bold { append("Ingredients:\n") }
                    .append("potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                            "prawn cocktail seasoning contains: flavouring, ")
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
            .set(mapOf("allergyColour" to Variables.allergyColour, "allergyBIU" to Variables.allergyBIU,
                "trafficLightA" to Variables.trafficLightA, "trafficLightB" to Variables.trafficLightB,
                "trafficLightC" to Variables.trafficLightC))
    }

    override fun onPause() {
        super.onPause()

        updateFirestore()
    }
}
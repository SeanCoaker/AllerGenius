package com.coaker.foodlabelapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar
import com.larswerkman.holocolorpicker.SVBar


class ColorPickerFragment(private val sourceId: String): DialogFragment() {

    private lateinit var parent: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.color_picker_layout, container, false)

        parent = activity as MainActivity

        val colorPicker = root.findViewById<ColorPicker>(R.id.picker)
        val svBar = root.findViewById<SVBar>(R.id.svbar)
        val opacityBar = root.findViewById<OpacityBar>(R.id.opacitybar)

        when (sourceId) {
            "allergy" -> {
                colorPicker.color = parseColor(Variables.allergyColour)
                colorPicker.oldCenterColor = parseColor(Variables.allergyColour)
            }

            "foodScoreA" -> {
                colorPicker.color = parseColor(Variables.trafficLightA)
                colorPicker.oldCenterColor = parseColor(Variables.trafficLightA)
            }

            "foodScoreB" -> {
                colorPicker.color = parseColor(Variables.trafficLightB)
                colorPicker.oldCenterColor = parseColor(Variables.trafficLightB)
            }

            "foodScoreC" -> {
                colorPicker.color = parseColor(Variables.trafficLightC)
                colorPicker.oldCenterColor = parseColor(Variables.trafficLightC)
            }

            "foodScoreX" -> {
                colorPicker.color = parseColor(Variables.trafficLightX)
                colorPicker.oldCenterColor = parseColor(Variables.trafficLightX)
            }
        }

        colorPicker.addSVBar(svBar)
        colorPicker.addOpacityBar(opacityBar)

        val okButton = root.findViewById<Button>(R.id.okButton)

        okButton.setOnClickListener {
            val hexColor = Integer.toHexString(colorPicker.color)
            val customiseFragment = parentFragment as CustomiseFragment

            when (sourceId) {
                "allergy" -> {
                    Variables.allergyColour = "#$hexColor"

                    parent.findViewById<Button>(R.id.colorsButton).setBackgroundColor(
                        parseColor(
                            Variables.allergyColour
                        )
                    )

                    customiseFragment.reloadAllergyColour()
                    customiseFragment.setAllergyPreview()
                }

                "foodScoreA" -> {
                    Variables.trafficLightA = "#$hexColor"

                    parent.findViewById<TextView>(R.id.foodScoreA).backgroundTintList =
                        ColorStateList.valueOf(parseColor(Variables.trafficLightA))
                }

                "foodScoreB" -> {
                    Variables.trafficLightB = "#$hexColor"

                    parent.findViewById<TextView>(R.id.foodScoreB).backgroundTintList =
                        ColorStateList.valueOf(parseColor(Variables.trafficLightB))
                }

                "foodScoreC" -> {
                    Variables.trafficLightC = "#$hexColor"

                    parent.findViewById<TextView>(R.id.foodScoreC).backgroundTintList =
                        ColorStateList.valueOf(parseColor(Variables.trafficLightC))
                }

                "foodScoreX" -> {
                    Variables.trafficLightX = "#$hexColor"

                    parent.findViewById<TextView>(R.id.foodScoreX).backgroundTintList =
                        ColorStateList.valueOf(parseColor(Variables.trafficLightX))
                }
            }

            dismiss()
        }

        return root
    }
}
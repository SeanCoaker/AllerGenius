package com.coaker.foodlabelapp

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList

/**
 * An adapter class that displays an allergy in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class FoodsAdapter(
    private val fragment: DiaryFragment,
    private var dataList: ArrayList<FoodDiaryItem>
) : RecyclerView.Adapter<FoodsAdapter.ViewHolder>() {


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val itemNameTextView = view.findViewById(R.id.itemNameTextView) as TextView
        val barcodeTextView = view.findViewById(R.id.barcodeTextView) as TextView
        val quantityTextView = view.findViewById(R.id.foodQuantityText) as TextView
        val ingredientsTextView = view.findViewById(R.id.foodIngredientsText) as TextView
        val tracesTextView = view.findViewById(R.id.foodTracesText) as TextView

        private val arrowButton = view.findViewById(R.id.arrowButton) as ImageButton

        init {
            arrowButton.tag = "MINIMISED"
            arrowButton.setOnClickListener(this)
            view.isClickable = true
        }

        // Sets up the onclick for the cross button.
        override fun onClick(v: View) {
            when (v.id) {
                arrowButton.id -> {
                    if (arrowButton.tag == "MINIMISED") {
                        quantityTextView.visibility = View.VISIBLE
                        ingredientsTextView.visibility = View.VISIBLE
                        tracesTextView.visibility = View.VISIBLE
                        arrowButton.tag = "EXPANDED"
                        arrowButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                    } else {
                        quantityTextView.visibility = View.GONE
                        ingredientsTextView.visibility = View.GONE
                        tracesTextView.visibility = View.GONE
                        arrowButton.tag = "MINIMISED"
                        arrowButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    }
                }
            }
        }
    }


    /**
     * A method that inflates the article preview layout as the view of each recycler view item.
     *
     * @param[parent] The parent of the view as a ViewGroup
     * @param[viewType] The view type of the view as an int
     *
     * @return[ViewHolder] The inflated view as a ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.food_diary_item_layout,
            parent,
            false
        )

        return ViewHolder(itemView)
    }


    /**
     * A method that sets the values to be displayed in each view included in the recycler view.
     *
     * @param[holder] The view holder to be configured.
     * @param[position] The position of the view holder in the recycler view.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = dataList[position]

        holder.itemNameTextView.text = entry.item
        holder.barcodeTextView.text = entry.barcode

        val quantityText = "Quantity: ${entry.quantity}"
        holder.quantityTextView.text = quantityText

        holder.ingredientsTextView.text = findAllergens(entry.ingredients)

        val tracesText = "Traces: ${entry.traces}"
        holder.tracesTextView.text = tracesText

    }


    /**
     * A method that returns how many items are in the data list.
     *
     * @return[Int]
     */
    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun findAllergens(ingredients: String?): SpannableStringBuilder {
        val output = SpannableStringBuilder().append("Ingredients:\n")

        ingredients?.split(",")?.forEach { ingredient ->
            var isAllergen = false
            Variables.allergyList.forEach { allergy ->
                if (ingredient.contains(allergy, true)) {

                    val colour = Color.parseColor(Variables.allergyColour)

                    when (Variables.allergyBIU) {
                        "NIL" -> {
                            output.color(colour) { append(ingredient)  }.append(",")
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
                            output.color(colour) { bold { italic { append(ingredient) } } }.append(",")
                        }

                        "BU" -> {
                            output.color(colour) { bold { underline { append(ingredient) } } }.append(",")
                        }

                        "IU" -> {
                            output.color(colour) { italic { underline { append(ingredient) } } }.append(",")
                        }

                        "BIU" -> {
                            output.color(colour) { bold { italic { underline { append(ingredient) } } } }.append(",")
                        }
                    }

                    isAllergen = true
                }
            }

            if (!isAllergen) {
                output.append("$ingredient,")
            }
        }

        return output
    }
}
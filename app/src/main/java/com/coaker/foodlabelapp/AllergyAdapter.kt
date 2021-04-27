package com.coaker.foodlabelapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList

/**
 * An adapter class that displays an allergy in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class AllergyAdapter(
    private val fragment: AllergiesFragment,
    private var dataList: ArrayList<String>
) : RecyclerView.Adapter<AllergyAdapter.ViewHolder>() {


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val allergyTextView = view.findViewById(R.id.allergyTextView) as TextView

        init {
            view.isClickable = true

            val crossButton = view.findViewById<ImageButton>(R.id.crossButton)
            crossButton.setOnClickListener(this)
        }

        // Sets up the onclick for the cross button.
        override fun onClick(v: View) {
            when (v.id) {
                R.id.crossButton -> {
                    removeItem(absoluteAdapterPosition)
                }
            }
        }
    }


    /**
     * A function that inflates the article preview layout as the view of each recycler view item.
     *
     * @param[parent] The parent of the view as a ViewGroup
     * @param[viewType] The view type of the view as an int
     *
     * @return[ViewHolder] The inflated view as a ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.allergy_list_item,
            parent,
            false
        )

        return ViewHolder(itemView)
    }


    /**
     * A function that sets the values to be displayed in each view included in the recycler view.
     *
     * @param[holder] The view holder to be configured.
     * @param[position] The position of the view holder in the recycler view.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val allergy = dataList[position]

        holder.allergyTextView.text = allergy
    }


    /**
     * A method that returns how many items are in the data list.
     *
     * @return[Int]
     */
    override fun getItemCount(): Int {
        return dataList.size
    }


    /**
     * A function to remove an allergy from the dataList and allergyList.
     *
     * @param[position] The position in the array lists to remove the allergy from.
     */
    fun removeItem(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
        fragment.updateAllergyList(dataList)
        fragment.updateFirestore()
    }
}
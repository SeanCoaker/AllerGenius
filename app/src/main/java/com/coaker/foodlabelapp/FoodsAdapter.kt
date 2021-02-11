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
class FoodsAdapter(
    private val fragment: DiaryDialogFragment,
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

        init {
            view.isClickable = true
        }

        // Sets up the onclick for the cross button.
        override fun onClick(v: View) {

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

    }


    /**
     * A method that returns how many items are in the data list.
     *
     * @return[Int]
     */
    override fun getItemCount(): Int {
        return dataList.size
    }
}
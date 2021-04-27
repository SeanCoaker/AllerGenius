package com.coaker.foodlabelapp

/**
 * A data class that stores data about a food item that is stored in Firestore and displayed in
 * the diary feature.
 *
 * @param[barcode] The food's barcode
 * @param[item] The food's name
 * @param[ingredients] The food's ingredients
 * @param[traces] The traces in the food
 * @param[quantity] The quantity of the food the user had eaten
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class FoodDiaryItem(val barcode: String? = null, val item: String? = null, val ingredients: String? = null, val traces: String? = null, val quantity: String? = null)
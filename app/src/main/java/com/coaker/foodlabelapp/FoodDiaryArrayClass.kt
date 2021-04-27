package com.coaker.foodlabelapp

/**
 * A data class used to store an ArrayList of food diary items
 *
 * @param[foods] The foods to be stored for a day of a month in the calendar view
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class FoodDiaryArrayClass(val foods: ArrayList<FoodDiaryItem>? = null)
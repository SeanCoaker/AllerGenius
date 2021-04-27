package com.coaker.foodlabelapp

/**
 * A data class used to store data about calendar event pointers.
 *
 * @param[day] The day the event pointer is stored for
 * @param[food] Whether the day contains a food item
 * @param[symptom] Whether the day contains a symptom record
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class CalendarEvent(var day: String = "", var food: Boolean = false, var symptom: Boolean = false)
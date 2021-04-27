package com.coaker.foodlabelapp

/**
 * A data class used to store an ArrayList of calendar events pointers
 *
 * @param[events] Events pointers for a month in the calendar view
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class EventPointers(val events: ArrayList<CalendarEvent>? = null)
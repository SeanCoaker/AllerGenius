package com.coaker.foodlabelapp

/**
 * A data class used to store data for symptoms to be shown in the diary feature.
 *
 * @param[symptoms] A string of the user's symptoms for a given day
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class Symptoms(var symptoms: String? = null)
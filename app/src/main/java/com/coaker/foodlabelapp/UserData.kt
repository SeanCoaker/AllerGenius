package com.coaker.foodlabelapp

/**
 * A data class used to store allergies for the user when read from Firestore.
 *
 * @param[allergies] The allergies from Firestore to be stored when using the app
 *
 * @author Sean Coaker
 * @since 1.0
 */
data class UserData(val allergies: ArrayList<String>? = null)
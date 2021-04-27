package com.coaker.foodlabelapp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * This class continues to check whether the user has an internet connection throughout their use of
 * the app.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class NetworkCallbackMonitor(application: Application) {
    private val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * This method starts the connectivity manager callback which checks if the user is connected to
     * a network. The boolean variable isConnected changes when network connection changes occur.
     * Each time the cm connects, customisation data is loaded in, just in case it couldn't be loaded
     * on initial boot of the application.
     */
    fun start() {

        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Variables.isConnected = true

                val user = Firebase.auth.currentUser

                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(user.uid)
                    docRef.get().addOnSuccessListener {

                        val dataCollection = docRef.collection("data")

                        val allergiesDoc = dataCollection.document("allergies")
                        allergiesDoc.get().addOnSuccessListener { allergiesDocSnapshot ->
                            if (allergiesDocSnapshot.exists()) {
                                val result = allergiesDocSnapshot.toObject(UserData::class.java)
                                Variables.allergyList = result!!.allergies!!
                            }
                        }

                        val customisationDoc = dataCollection.document("customisation")
                        customisationDoc.get().addOnSuccessListener { customisationDocSnapshot ->
                            if (customisationDocSnapshot.exists()) {
                                val result =
                                    customisationDocSnapshot.toObject(CustomisationData::class.java)
                                Variables.allergyColour = result!!.allergyColour
                                Variables.allergyBIU = result.allergyBIU
                                Variables.trafficLightA = result.trafficLightA
                                Variables.trafficLightB = result.trafficLightB
                                Variables.trafficLightC = result.trafficLightC
                                Variables.trafficLightX = result.trafficLightX
                                Variables.fatLow = result.fatLow
                                Variables.fatHigh = result.fatHigh
                                Variables.saturatesLow = result.saturatesLow
                                Variables.saturatesHigh = result.saturatesHigh
                                Variables.sugarLow = result.sugarLow
                                Variables.sugarHigh = result.sugarHigh
                                Variables.saltLow = result.saltLow
                                Variables.saltHigh = result.saltHigh
                                Variables.foodColour = result.foodColour
                                Variables.symptomColour = result.symptomColour
                            }
                        }
                    }.addOnFailureListener {
                        Log.i("Firestore Read: ", "Failed")
                    }
                }

            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Variables.isConnected = false
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Variables.isConnected = false
            }
        })
    }

    /**
     * This method stops the network callback and is called when the app is closed.
     */
    fun stop() {
        cm.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }
}
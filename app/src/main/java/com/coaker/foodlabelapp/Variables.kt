package com.coaker.foodlabelapp

import android.graphics.Color


/**
 * Static variables that are associated with the whole app.
 */
object Variables {
    var allergyList = ArrayList<String>()
    var allergyColour = "#FF0000"
    var allergyBIU = "NIL"

    var foodColour = "#2dc937"
    var symptomColour = "#cc3232"

    var trafficLightA = "#2dc937"
    var trafficLightB = "#e7b416"
    var trafficLightC = "#cc3232"
    var trafficLightX = "#000000"

    var fatLow = 3
    var fatHigh = 18

    var saturatesLow = 2
    var saturatesHigh = 5

    var sugarLow = 5
    var sugarHigh = 23

    var saltLow = 1
    var saltHigh = 2

    var arAvailable = false
}
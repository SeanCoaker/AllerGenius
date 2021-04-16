package com.coaker.foodlabelapp

data class CustomisationData(
    val allergyColour: String = "#FF0000",
    val allergyBIU: String = "NIL",
    val trafficLightA: String = "#2dc937",
    val trafficLightB: String = "#e7b416",
    val trafficLightC: String = "#cc3232",
    val trafficLightX: String = "#000000",
    val fatLow: Int = 3,
    val fatHigh: Int = 18,
    val saturatesLow: Int = 2,
    val saturatesHigh: Int = 5,
    val sugarLow: Int = 5,
    val sugarHigh: Int = 23,
    val saltLow: Int = 1,
    val saltHigh: Int = 2,
    val foodColour: String = "#2dc937",
    val symptomColour: String = "#cc3232"
)
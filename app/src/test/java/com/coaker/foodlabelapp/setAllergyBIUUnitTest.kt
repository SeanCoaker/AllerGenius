package com.coaker.foodlabelapp

import org.junit.Assert
import org.junit.Test


class setAllergyBIUUnitTest {

    @Test
    fun setAllergyBIU_isCorrect() {
        Assert.assertEquals("NIL", setAllergyBIU(false, false, false))
        Assert.assertEquals("B", setAllergyBIU(true, false, false))
        Assert.assertEquals("I", setAllergyBIU(false, true, false))
        Assert.assertEquals("U", setAllergyBIU(false, false, true))
        Assert.assertEquals("BI", setAllergyBIU(true, true, false))
        Assert.assertEquals("BU", setAllergyBIU(true, false, true))
        Assert.assertEquals("IU", setAllergyBIU(false, true, true))
        Assert.assertEquals("BIU", setAllergyBIU(true, true, true))
    }

    private fun setAllergyBIU(boldButtonSelected: Boolean, italicButtonSelected: Boolean, underlineButtonSelected: Boolean): String {

        var allergyBIU = ""

        if (!boldButtonSelected && !italicButtonSelected && !underlineButtonSelected) {
            allergyBIU = "NIL"
        } else {
            if (boldButtonSelected) {
                allergyBIU += "B"
            }

            if (italicButtonSelected) {
                allergyBIU += "I"
            }

            if (underlineButtonSelected) {
                allergyBIU += "U"
            }
        }

        return allergyBIU
    }
}
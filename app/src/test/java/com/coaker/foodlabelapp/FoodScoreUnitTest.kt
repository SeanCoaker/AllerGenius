package com.coaker.foodlabelapp

import org.junit.Test

import org.junit.Assert.*

class FoodScoreUnitTest {
    @Test
    fun calculateFoodScore_isCorrect() {
        assertEquals("A", calculateFoodScore(2.0, 1.0, 4.0, 0.5))
        assertEquals("A", calculateFoodScore(2.0, 3.0, 4.0, 0.5))

        assertEquals("B", calculateFoodScore(20.0, 1.0, 4.0, 0.5))
        assertEquals("B", calculateFoodScore(2.0, 1.0, 6.0, 1.5))
        assertEquals("B", calculateFoodScore(2.0, 1.0, 6.0, 3.0))
        assertEquals("B", calculateFoodScore(2.0, 1.0, 24.0, 1.5))
        assertEquals("B", calculateFoodScore(2.0, 1.0, 24.0, 3.0))

        assertEquals("B", calculateFoodScore(2.0, 3.0, 6.0, 3.0))
        assertEquals("B", calculateFoodScore(2.0, 3.0, 6.0, 1.5))

        assertEquals("B", calculateFoodScore(4.0, 3.0, 6.0, 1.5))

        assertEquals("C", calculateFoodScore(2.0, 3.0, 24.0, 3.0))
        assertEquals("C", calculateFoodScore(2.0, 6.0, 24.0, 3.0))

        assertEquals("C", calculateFoodScore(5.0, 3.0, 10.0, 3.0))
        assertEquals("C", calculateFoodScore(5.0, 3.0, 24.0, 3.0))
        assertEquals("C", calculateFoodScore(5.0, 6.0, 24.0, 3.0))
        assertEquals("C", calculateFoodScore(20.0, 6.0, 24.0, 3.0))
    }


    private fun calculateFoodScore(
        fat: Double,
        saturates: Double,
        sugar: Double,
        salt: Double
    ): String {

        var greenValue = 0
        var amberValue = 0
        var redValue = 0

        when {
            fat <= Variables.fatLow -> greenValue++
            fat > Variables.fatHigh -> redValue++
            else -> amberValue++
        }

        when {
            saturates <= Variables.saturatesLow -> greenValue++
            saturates > Variables.saturatesHigh -> redValue++
            else -> amberValue++
        }

        when {
            sugar <= Variables.sugarLow -> greenValue++
            sugar > Variables.sugarHigh -> redValue++
            else -> amberValue++
        }

        when {
            salt <= Variables.saltLow -> greenValue++
            salt > Variables.saltHigh -> redValue++
            else -> amberValue++
        }

        when (greenValue) {
            4 -> {
                return "A"
            }

            3 -> {
                return if (redValue == 1) {
                    "B"
                } else {
                    "A"
                }
            }

            2 -> {
                return "B"
            }

            1 -> {
                return if (amberValue >= 2) {
                    "B"
                } else {
                    "C"
                }
            }

            else -> {
                return if (amberValue > 3) {
                    "B"
                } else {
                    "C"
                }
            }
        }
    }
}
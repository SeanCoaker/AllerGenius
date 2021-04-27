package com.coaker.foodlabelapp

import org.junit.Assert
import org.junit.Test

class FindAllergensUnitTest {

    companion object {
        val ingredients: String =
            "potatoes, sunflower oil (24%), rapeseed oil, prawn cocktail seasoning,\n" +
                    "prawn cocktail seasoning contains: flavouring, sugar, glucose, salt, " +
                    "citric acid, potassium chloride, dried yeast, dried onion, vale of evesham " +
                    "tomato extract, color (paprika extract), sweetener (sucralose),"
    }

    @Test
    fun findAllergens_isCorrect() {
        val allergens = ArrayList<String>()
        allergens.add("invalid")

        Assert.assertEquals(false, findAllergens(ingredients, allergens))

        allergens.clear()
        allergens.add("sugar")
        Assert.assertEquals(true, findAllergens(ingredients, allergens))

        allergens.clear()
        allergens.add("SuGaR")
        Assert.assertEquals(true, findAllergens(ingredients, allergens))

        allergens.clear()
        allergens.add("sugar")
        allergens.add("glucose")
        Assert.assertEquals(true, findAllergens(ingredients, allergens))

        allergens.clear()
        allergens.add("invalid")
        allergens.add("glucose")
        Assert.assertEquals(true, findAllergens(ingredients, allergens))

    }

    private fun findAllergens(ingredients: String, allergens: ArrayList<String>): Boolean {
        val ingredientList = ingredients.split(",")

        ingredientList.forEach { ingredient ->
            allergens.forEach { allergy ->
                if (ingredient.contains(allergy, true)) {
                    return true
                }
            }
        }

        return false
    }
}
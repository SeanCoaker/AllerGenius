package com.coaker.foodlabelapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * A class made to handle the allergy list fragment. Where users can add allergens
 * to their allergy list to have them tracked across the application.
 *
 * @author Sean Coaker
 * @since 1.0
 */
class AllergiesFragment : Fragment() {

    private lateinit var parent: MainActivity
    private lateinit var addButton: Button
    private lateinit var addAllergyTextLayout: TextInputLayout


    /**
     * A function that is called when the fragment is created.
     * 
     * @param[inflater] Inflater used to inflate the layout in this fragment.
     * @param[container] Contains the content of the fragment.
     * @param[savedInstanceState] Any previous saved instance of the fragment.
     *
     * @return[View] The view that has been created.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_allergy_list, container, false)

        parent = activity as MainActivity

        val recyclerView = root.findViewById<RecyclerView>(R.id.allergyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = AllergyAdapter(this, Variables.allergyList)
        recyclerView.adapter = adapter

        addButton = root.findViewById(R.id.addButton)
        addAllergyTextLayout = root.findViewById(R.id.allergyTextLayout)

        root.findViewById<TextView>(R.id.textViewAddAllergy).setOnClickListener {
            addButton.visibility = View.VISIBLE
            addAllergyTextLayout.visibility = View.VISIBLE
        }

        addButton.setOnClickListener {
            populateAllergyList()
            // Updates the list in the allergy adapter
            adapter.notifyDataSetChanged()
            addButton.visibility = View.GONE
            addAllergyTextLayout.visibility = View.GONE
            parent.findViewById<TextInputEditText>(R.id.editTextAllergies).text!!.clear()
        }

        return root
    }


    /**
     * A function used to add allergens to the user's allergy list.
     */
    private fun populateAllergyList() {
        val allergyText = parent.findViewById<TextInputEditText>(R.id.editTextAllergies).text
        val allergies = allergyText!!.split(",")
        var alreadyExists = false

        if (allergies.first() != "") {
            allergies.forEach {

                // Checks if the allergen is already in the list.
                if (Variables.allergyList.stream().noneMatch { s ->
                        s.equals(it, true)
                    }) {

                    // Removes leading and trailing whitespace from allergen.
                    Variables.allergyList.add(it.trimStart().trimEnd())
                } else {
                    alreadyExists = true
                }
            }
        }

        if (alreadyExists) {
            Snackbar.make(
                requireView(),
                "One or more allergies already exist in this list",
                Snackbar.LENGTH_LONG
            ).show()
        }

        updateFirestore()
    }


    /**
     * A function used to update the allergen list stored in the user's Firestore documents.
     */
    fun updateFirestore() {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("data")
            .document("allergies").set(mapOf("allergies" to Variables.allergyList))
    }


    /**
     * A function used to overwrite the existing allergen list with the new entries.
     * 
     * @param[dataList] List of allergens.
     */
    fun updateAllergyList(dataList: ArrayList<String>) {
        Variables.allergyList = dataList
    }
}
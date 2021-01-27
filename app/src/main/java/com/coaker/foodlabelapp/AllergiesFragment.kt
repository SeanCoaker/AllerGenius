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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AllergiesFragment : Fragment() {

    private lateinit var parent: MainActivity
    private lateinit var addButton: Button
    private lateinit var addAllergyTextLayout: TextInputLayout

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
            adapter.notifyDataSetChanged()
            addButton.visibility = View.GONE
            addAllergyTextLayout.visibility = View.GONE
            parent.findViewById<TextInputEditText>(R.id.editTextAllergies).text!!.clear()
        }

        return root
    }

    private fun populateAllergyList() {
        val allergyText = parent.findViewById<TextInputEditText>(R.id.editTextAllergies).text
        val allergies = allergyText!!.split(",")

        if (allergies.first() != "") {
            allergies.forEach {
                Variables.allergyList.add(it)
            }
        }

        updateFirestore()
    }

    fun updateFirestore() {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).collection("data")
            .document("allergies").set(mapOf("allergies" to Variables.allergyList))
    }

    fun updateAllergyList(dataList: ArrayList<String>) {
        Variables.allergyList = dataList
    }
}
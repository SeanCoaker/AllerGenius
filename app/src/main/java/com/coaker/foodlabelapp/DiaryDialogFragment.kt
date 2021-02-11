package com.coaker.foodlabelapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class DiaryDialogFragment(private val monthYear: String, private val day: String): DialogFragment() {

    private lateinit var root: View
    private lateinit var parent: MainActivity
    private lateinit var adapter: FoodsAdapter

    private var foodsEntries = ArrayList<FoodDiaryItem>()

    private val user = Firebase.auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        root = inflater.inflate(R.layout.food_symptom_record_layout, container, false)

        parent = activity as MainActivity

        val dayTextView: TextView = root.findViewById(R.id.dayTextView)
        val dateString = "$day $monthYear"
        dayTextView.text = dateString

        val recyclerView: RecyclerView = root.findViewById(R.id.foodsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = FoodsAdapter(this, foodsEntries)
        recyclerView.adapter = adapter

        loadEntries()

        return root
    }

    override fun onResume() {
        super.onResume()

        val window = dialog!!.window ?: return
        val params = window.attributes
        params.width = 1060
        window.attributes = params
    }

    private fun loadEntries() {
        val docRef = db.collection("users").document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val eventsCollection = docRef.collection("events")

                val foodsDay = eventsCollection
                    .document("foods")
                    .collection("months")
                    .document(monthYear)
                    .collection("days")
                    .document(day.dropLast(2))

                foodsDay.get().addOnSuccessListener { foodsDayDocSnapshot ->
                    if (foodsDayDocSnapshot.exists()) {
                        val result = foodsDayDocSnapshot.toObject(FoodDiaryArrayClass::class.java)
                        if (result?.foods != null) {
                            foodsEntries.clear()
                            foodsEntries.addAll(result.foods)

                            adapter.notifyDataSetChanged()
                        }
                    }
                }

            }
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }
    }
}
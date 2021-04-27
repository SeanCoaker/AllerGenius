package com.coaker.foodlabelapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.ArCoreApk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * A class used to handle the main features of the application
 *
 * @author Sean Coaker
 * @since 1.0
 */
class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var navView: NavigationView


    /**
     * A method called when the activity is being created. This method sets up the navigation aspects
     * of the app and loads the user's customisation saved data.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("AllerGenius Preferences", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("username", Firebase.auth.currentUser!!.displayName)
        editor.apply()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        drawer = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.navView)

        navView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

        val user = Firebase.auth.currentUser

        val docRef = db.collection("users").document(user!!.uid)
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


        val scannerFragment = ScannerFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.flContent, scannerFragment).commit()
    }


    /**
     * A function created to handle the user selecting different windows of the application.
     *
     * @param[item] An item signalling which window the user wants to switch to.
     */
    private fun selectDrawerItem(item: MenuItem) {

        when (item.itemId) {
            R.id.nav_home -> {
                val scannerFragment = ScannerFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flContent, scannerFragment)
                    .commit()
            }

            R.id.nav_allergies -> {
                val allergiesFragment = AllergiesFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flContent, allergiesFragment)
                    .commit()
            }

            R.id.nav_customise -> {
                val customiseFragment = CustomiseFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flContent, customiseFragment)
                    .commit()
            }

            R.id.nav_foods_eaten -> {
                val calendarFragment = DiaryFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flContent, calendarFragment)
                    .commit()
            }

            R.id.nav_logout -> {
                logout()
            }

        }

        // Displays error message upon switching screens if the device is not connected to network
        if (!Variables.isConnected) {
            Snackbar.make(
                findViewById(android.R.id.content),
                R.string.network_connection_error,
                Snackbar.LENGTH_LONG
            ).show()
        }

        item.isChecked = true
        title = item.title
        drawer.closeDrawers()
    }


    /**
     * A function called to handle what happens when the hamburger button is pressed.
     *
     * @param[item] The item clicked
     * @return[Boolean] The return of super.onOptionsItemSelected(item)
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawer.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A function called to log the user out of Firebase
     */
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val logoutIntent = Intent(this, LoginActivity::class.java)
        logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(logoutIntent)
        finish()
    }


    /**
     * A function called to setup the options menu when it is created. In this instance, the user's
     * name and email are displayed to confirm their identity.
     *
     * @param[menu] The menu to edit.
     * @return[Boolean] The return of super.onCreateOptionsMenu(menu)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val user = Firebase.auth.currentUser

        findViewById<TextView>(R.id.nameText).text = user!!.displayName
        findViewById<TextView>(R.id.emailText).text = user.email

        return super.onCreateOptionsMenu(menu)
    }


    /**
     * A function called to switch from normal barcode scanning to AR barcode scanning.
     */
    fun switchToArFragment() {
        val arFragment = ArFragmentController()
        supportFragmentManager.beginTransaction().replace(R.id.flContent, arFragment)
            .commit()
    }


    /**
     * A function called to switch from AR barcode scanning to normal barcode scanning.
     */
    fun switchToCamFragment() {
        val scannerFragment = ScannerFragment()
        supportFragmentManager.beginTransaction().replace(R.id.flContent, scannerFragment)
            .commit()
    }
}
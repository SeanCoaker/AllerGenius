package com.coaker.foodlabelapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

/**
 * A class created to display the splash screen disclaimer message to the user.
 *
 * @author Sean Coaker
 * @since 1.0
 */
class SplashScreenActivity : AppCompatActivity() {

    /**
     * A method called when the activity is being created. This method sets up the navigation aspects
     * of the app and loads the user's customisation saved data.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        supportActionBar?.hide()

        Handler(Looper.myLooper()!!).postDelayed({

            // Checks if the user is already logged in.
            if (FirebaseAuth.getInstance().currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }


            finish()
        }, 5000)
    }
}
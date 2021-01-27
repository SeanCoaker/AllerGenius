package com.coaker.foodlabelapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * an activity class to handle the user logging in with Facebook or Google.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    // A constant Google sign in identifier
    companion object {
        private const val GOOGLE_CODE = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient


    /**
     * A method run when the activity is being created. This method configures how the login button
     * choices are displayed to the user.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val googleCard = findViewById<CardView>(R.id.googleCard)
        val emailCard = findViewById<CardView>(R.id.emailCard)

        googleCard.setOnClickListener(this)
        emailCard.setOnClickListener(this)

        auth = Firebase.auth
    }


    /**
     * This method checks if a user is already logged in. If they are then they can continue to the
     * home screen.
     */
    override fun onStart() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            updateUI(currentUser)
        } else {
            updateUI(null)
        }

        super.onStart()
    }


    /**
     * This method takes the user from the login activity to the main activity.
     *
     * @param[user] The user logging in.
     */
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("custom", false)
            startActivity(intent)
        }
    }


    /**
     * This method configures the onClick method for the Google, Facebook and Email log in card views.
     *
     * @param[v] The card view clicked.
     */
    override fun onClick(v: View) {
        when(v.id) {
            R.id.googleCard -> {
                googleSignIn()
            }

            R.id.emailCard -> {
                val emailIntent = Intent(this, EmailLoginActivity::class.java)
                startActivity(emailIntent)
            }
        }
    }


    /**
     * This method handles the Google sign in feature.
     */
    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_CODE)
    }


    /**
     * This method identifies whether the user has signed into Google successfully on returning form the
     * Google sign in intent.
     *
     * @param[requestCode] The request code sent with the startActivityForResult
     * @param[resultCode] The code returned from the intent.
     * @param[data] The data returned from the intent.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)!!
                googleAuth(account.idToken!!)
            } catch (e: ApiException) {
                updateUI(null)
            }
        }
    }


    /**
     * This method attempts to authorise the Google sign in and updates the user interface accordingly.
     */
    private fun googleAuth(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Authentication Failed", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
}
package com.platzi.android.firestore.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.networ.Callback
import com.platzi.android.firestore.networ.FirestoreService
import com.platzi.android.firestore.networ.USERS_COLLECTION_NAME
import kotlinx.android.synthetic.main.activity_login.*

/**
 * @author Brayan Bermudez
 * github brayancaselles
 * https://github.com/brayancaselles/App-de-criptos-con-Firebase-Firestore
 */

const val USERNAME_KEY = "username_key"

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var firestoreService: FirestoreService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
    }

    fun onStartClicked(view: View) {
        view.isEnabled = false
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userName = username.text.toString()

                firestoreService.findUserById(
                    userName,
                    object : Callback<User> {
                        override fun onSuccess(result: User?) {
                            if (result == null) {
                                val user = User()
                                user.userName = userName
                                saveUserAndStartMainActivity(user, view)
                            } else {
                                startMainActivity(userName)
                            }
                        }

                        override fun onFailed(exception: Exception) {
                            showErrorMessage(view)
                        }
                    }
                )

                val user = User()
                user.userName = userName
                saveUserAndStartMainActivity(user, view)
                println("El nombre del Usuario es: $userName")
                startMainActivity(userName)
            } else {
                showErrorMessage(view)
                view.isEnabled = true
            }
        }
    }

    private fun saveUserAndStartMainActivity(user: User, view: View) {
        firestoreService.setDocument(
            user,
            USERS_COLLECTION_NAME,
            user.userName,
            object : Callback<Void> {
                override fun onSuccess(result: Void?) {
                    startMainActivity(user.userName)
                }

                override fun onFailed(exception: Exception) {
                    showErrorMessage(view)
                    Log.e(TAG, "Error", exception)
                    view.isEnabled = true
                }
            }
        )
    }

    private fun showErrorMessage(view: View) {
        Snackbar.make(
            view,
            getString(R.string.error_while_connecting_to_the_server),
            Snackbar.LENGTH_LONG
        )
            .setAction("Info", null).show()
    }

    private fun startMainActivity(username: String) {
        val intent = Intent(this@LoginActivity, TraderActivity::class.java)
        intent.putExtra(USERNAME_KEY, username)
        startActivity(intent)
        finish()
    }
}

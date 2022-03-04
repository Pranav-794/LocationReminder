package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var mBinding: ActivityAuthenticationBinding

    private val authViewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        mBinding.loginButton.setOnClickListener {
            launchSignInFlow()
        }
        authViewModel.authenticationState.observe(this, Observer { authState ->
            when(authState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                } else -> {

                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Timber.i("Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.map)
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }
}

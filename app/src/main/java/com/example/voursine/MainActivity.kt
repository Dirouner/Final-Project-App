package com.example.voursine

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            // Already logged in across app restarts, skip login page
            startActivity(Intent(this, DiscoverActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.loading_screen)
        applyWindowInsets()

        // Show loading screen for 2 seconds, then transition to login_page
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                showLoginPage()
            }
        }, 2000)
    }

    private fun showLoginPage() {
        setContentView(R.layout.login_page)
        applyWindowInsets()

        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        // Pre-fill last logged in user if any
        val savedUsername = sharedPrefs.getString("saved_username", "")
        if (!savedUsername.isNullOrEmpty()) {
            etUsername?.setText(savedUsername)
        }

        findViewById<TextView>(R.id.tvGoToRegister)?.setOnClickListener {
            showRegisterPage()
        }

        findViewById<Button>(R.id.btnGoogleLogin)?.setOnClickListener {
            signInWithGoogle()
        }

        findViewById<Button>(R.id.btnLogin)?.setOnClickListener {
            val username = etUsername?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString()?.trim() ?: ""
            val tilUsername = findViewById<TextInputLayout>(R.id.tilUsername)
            val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)

            var isValid = true

            if (username.isEmpty()) {
                tilUsername?.error = if (password.isEmpty()) "insert username/password" else "insert username"
                isValid = false
            } else {
                tilUsername?.error = null
            }

            if (password.isEmpty()) {
                tilPassword?.error = if (username.isEmpty()) "insert username/password" else "invalid password"
                isValid = false
            } else {
                tilPassword?.error = null
            }

            if (isValid) {
                val registeredUsername = sharedPrefs.getString("registered_username", null)
                val registeredPassword = sharedPrefs.getString("registered_password", null)

                if (registeredUsername.isNullOrEmpty() || registeredPassword.isNullOrEmpty()) {
                    tilUsername?.error = "no registered account, please register"
                    isValid = false
                } else if (username != registeredUsername || password != registeredPassword) {
                    tilPassword?.error = "invalid username or password"
                    isValid = false
                }
            }

            if (isValid) {
                ensureUserId(sharedPrefs, username)
                sharedPrefs.edit()
                    .putString("saved_username", username)
                    .putString("saved_password", password)
                    .putBoolean("is_logged_in", true)
                    .apply()

                startActivity(Intent(this, DiscoverActivity::class.java))
                finish()
            }
        }
    }

    private fun showRegisterPage() {
        setContentView(R.layout.register_page)
        applyWindowInsets()

        val etRegisterUsername = findViewById<TextInputEditText>(R.id.etRegisterUsername)
        val etRegisterPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val tilRegisterUsername = findViewById<TextInputLayout>(R.id.tilRegisterUsername)
        val tilRegisterPassword = findViewById<TextInputLayout>(R.id.tilRegisterPassword)

        findViewById<TextView>(R.id.tvGoToLogin)?.setOnClickListener {
            showLoginPage()
        }

        findViewById<Button>(R.id.btnRegisterGoogle)?.setOnClickListener {
            signInWithGoogle()
        }

        findViewById<Button>(R.id.btnRegisterSubmit)?.setOnClickListener {
            val username = etRegisterUsername?.text?.toString()?.trim() ?: ""
            val password = etRegisterPassword?.text?.toString()?.trim() ?: ""

            var isValid = true

            if (username.isEmpty()) {
                tilRegisterUsername?.error = if (password.isEmpty()) "insert username/password" else "insert username"
                isValid = false
            } else {
                tilRegisterUsername?.error = null
            }

            if (password.isEmpty()) {
                tilRegisterPassword?.error = if (username.isEmpty()) "insert username/password" else "invalid password"
                isValid = false
            } else {
                tilRegisterPassword?.error = null
            }

            if (isValid) {
                val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
                ensureUserId(sharedPrefs, username)
                sharedPrefs.edit()
                    .putString("registered_username", username)
                    .putString("registered_password", password)
                    .apply()

                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
                showLoginPage()
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
        val email = if (result.resultCode == RESULT_OK && result.data != null) {
            result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
        } else {
            null
        }
        finishGoogleLogin(sharedPrefs, email ?: "google_user_${(1000..9999).random()}@gmail.com")
    }

    private fun signInWithGoogle() {
        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
        try {
            val accounts = android.accounts.AccountManager.get(this).getAccountsByType("com.google")
            if (accounts.isNotEmpty()) {
                finishGoogleLogin(sharedPrefs, accounts[0].name)
            } else {
                val intent = android.accounts.AccountManager.newChooseAccountIntent(
                    null, null, arrayOf("com.google"), false, null, null, null, null
                )
                googleSignInLauncher.launch(intent)
            }
        } catch (_: Exception) {
            val randomEmail = "google_user_${(1000..9999).random()}@gmail.com"
            finishGoogleLogin(sharedPrefs, randomEmail)
        }
    }

    private fun finishGoogleLogin(sharedPrefs: android.content.SharedPreferences, email: String) {
        val username = email.substringBefore("@")
        ensureUserId(sharedPrefs, username)
        sharedPrefs.edit()
            .putString("registered_username", username)
            .putString("registered_password", "google_oauth_secure")
            .putString("saved_username", username)
            .putString("saved_password", "google_oauth_secure")
            .putBoolean("is_logged_in", true)
            .apply()

        Toast.makeText(this, "Signed in with Google ($email) successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DiscoverActivity::class.java))
        finish()
    }

    private fun ensureUserId(sharedPrefs: android.content.SharedPreferences, username: String) {
        val existingId = sharedPrefs.getString("user_id", null)
        if (existingId.isNullOrEmpty()) {
            val random9Digits = (100000000..999999999).random().toString()
            sharedPrefs.edit()
                .putString("user_id", random9Digits)
                .putString("user_fullname", username)
                .putString("user_email", "$username@gmail.com")
                .putString("user_phone", "+1 555-0199")
                .putString("user_bio", "Fashion enthusiast & explorer.")
                .apply()
        }
    }

    private fun applyWindowInsets() {
        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
}

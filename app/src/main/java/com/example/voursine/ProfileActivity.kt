package com.example.voursine

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvUserIdDisplay = findViewById<TextView>(R.id.tvUserIdDisplay)
        val etProfileName = findViewById<TextInputEditText>(R.id.etProfileName)
        val etProfileGender = findViewById<TextInputEditText>(R.id.etProfileGender)
        val etProfileBirthdate = findViewById<TextInputEditText>(R.id.etProfileBirthdate)
        val etProfileBio = findViewById<TextInputEditText>(R.id.etProfileBio)
        val etProfilePhone = findViewById<TextInputEditText>(R.id.etProfilePhone)
        val etProfileEmail = findViewById<TextInputEditText>(R.id.etProfileEmail)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        // Load existing profile info
        val userId = sharedPrefs.getString("user_id", "123456789") ?: "123456789"
        tvUserIdDisplay.text = "User ID: $userId"
        etProfileName?.setText(sharedPrefs.getString("user_fullname", ""))
        etProfileGender?.setText(sharedPrefs.getString("user_gender", ""))
        etProfileBirthdate?.setText(sharedPrefs.getString("user_birthdate", ""))
        etProfileBio?.setText(sharedPrefs.getString("user_bio", ""))
        etProfilePhone?.setText(sharedPrefs.getString("user_phone", ""))
        etProfileEmail?.setText(sharedPrefs.getString("user_email", ""))

        // Gender Selection Dialog
        etProfileGender?.isFocusable = false
        etProfileGender?.isClickable = true
        etProfileGender?.setOnClickListener {
            val genders = arrayOf("he/him", "she/her")
            AlertDialog.Builder(this)
                .setTitle("Select Gender")
                .setItems(genders) { _, which ->
                    etProfileGender.setText(genders[which])
                }
                .show()
        }

        // Birthdate DatePickerDialog
        etProfileBirthdate?.isFocusable = false
        etProfileBirthdate?.isClickable = true
        etProfileBirthdate?.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentText = etProfileBirthdate.text?.toString()
            if (!currentText.isNullOrEmpty()) {
                try {
                    val parts = currentText.split("/")
                    if (parts.size == 3) {
                        calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                    }
                } catch (_: Exception) {}
            }
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    etProfileBirthdate.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSaveProfile.setOnClickListener {
            val name = etProfileName?.text?.toString()?.trim() ?: ""
            val gender = etProfileGender?.text?.toString()?.trim() ?: ""
            val birthdate = etProfileBirthdate?.text?.toString()?.trim() ?: ""
            val bio = etProfileBio?.text?.toString()?.trim() ?: ""
            val phone = etProfilePhone?.text?.toString()?.trim() ?: ""
            val email = etProfileEmail?.text?.toString()?.trim() ?: ""

            sharedPrefs.edit()
                .putString("user_fullname", name)
                .putString("user_gender", gender)
                .putString("user_birthdate", birthdate)
                .putString("user_bio", bio)
                .putString("user_phone", phone)
                .putString("user_email", email)
                .apply()

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

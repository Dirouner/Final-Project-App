package com.example.voursine

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AccountActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        updateUserInfo()

        findViewById<LinearLayout>(R.id.menuProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuWishlist).setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuAddressList).setOnClickListener {
            startActivity(Intent(this, AddressListActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val name = sharedPrefs.getString("user_fullname", "User Name") ?: "User Name"
        val email = sharedPrefs.getString("user_email", "user@gmail.com") ?: "user@gmail.com"
        val userId = sharedPrefs.getString("user_id", "123456789") ?: "123456789"

        findViewById<TextView>(R.id.tvUserName).text = name
        findViewById<TextView>(R.id.tvUserEmail).text = email
        findViewById<TextView>(R.id.tvUserUniqueId).text = "ID: $userId"
    }
}

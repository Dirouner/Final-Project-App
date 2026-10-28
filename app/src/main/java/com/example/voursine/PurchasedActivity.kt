package com.example.voursine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray

class PurchasedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_purchased)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)
        val packageId = sharedPrefs.getString("last_package_id", "J123456789EU") ?: "J123456789EU"
        val activeAddressId = sharedPrefs.getString("active_address_id", "default_1") ?: "default_1"
        val jsonStr = sharedPrefs.getString("saved_addresses_json", null)

        var displayAddress = "Le Sardanapale, 2 Av. Princesse Grace, 98000 Monaco, Monako"
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("id") == activeAddressId) {
                        val owner = obj.getString("owner")
                        val details = obj.getString("details")
                        val phone = obj.getString("phone")
                        displayAddress = "$owner ($phone) - $details"
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        findViewById<TextView>(R.id.tvPackageIdVal).text = packageId
        findViewById<TextView>(R.id.tvShippingAddressVal).text = displayAddress

        // Home Page button returns to DiscoverActivity
        findViewById<Button>(R.id.btnHomePage)?.setOnClickListener {
            val intent = Intent(this, DiscoverActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}

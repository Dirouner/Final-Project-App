package com.example.voursine

import android.content.Intent
import android.graphics.Color
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

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 1)
        val sharedPrefs = getSharedPreferences("voursine_login_prefs", MODE_PRIVATE)

        val ivProductDetail = findViewById<ImageView>(R.id.ivProductDetail)
        val tvDetailItemName = findViewById<TextView>(R.id.tvItemName)
        val tvDetailItemPrice = findViewById<TextView>(R.id.tvItemPrice)
        val tvDetailTotalPrice = findViewById<TextView>(R.id.tvTotalValue)
        val ivWishlistToggle = findViewById<ImageView>(R.id.ivWishlistToggle)

        when (productId) {
            1 -> {
                ivProductDetail.setImageResource(R.drawable.p1_s)
                tvDetailItemName.text = getString(R.string.product_1)
                tvDetailItemPrice.text = "76.95"
                tvDetailTotalPrice.text = "€ 92.68"
            }
            2 -> {
                ivProductDetail.setImageResource(R.drawable.p2_s)
                tvDetailItemName.text = getString(R.string.product_2)
                tvDetailItemPrice.text = "126.00"
                tvDetailTotalPrice.text = "€ 143.21"
            }
            3 -> {
                ivProductDetail.setImageResource(R.drawable.p3_s)
                tvDetailItemName.text = getString(R.string.product_3)
                tvDetailItemPrice.text = "83.02"
                tvDetailTotalPrice.text = "€ 98.94"
            }
            4 -> {
                ivProductDetail.setImageResource(R.drawable.p4_s)
                tvDetailItemName.text = getString(R.string.product_4)
                tvDetailItemPrice.text = "110.52"
                tvDetailTotalPrice.text = "€ 127.26"
            }
            5 -> {
                ivProductDetail.setImageResource(R.drawable.p5_s)
                tvDetailItemName.text = getString(R.string.product_5)
                tvDetailItemPrice.text = "35.77"
                tvDetailTotalPrice.text = "€ 50.27"
            }
            6 -> {
                ivProductDetail.setImageResource(R.drawable.p6_s)
                tvDetailItemName.text = getString(R.string.product_6)
                tvDetailItemPrice.text = "52.45"
                tvDetailTotalPrice.text = "€ 68.90"
            }
            7 -> {
                ivProductDetail.setImageResource(R.drawable.p7_s)
                tvDetailItemName.text = getString(R.string.product_7)
                tvDetailItemPrice.text = "125.00"
                tvDetailTotalPrice.text = "€ 143.60"
            }
            8 -> {
                ivProductDetail.setImageResource(R.drawable.p8_s)
                tvDetailItemName.text = getString(R.string.product_8)
                tvDetailItemPrice.text = "195.00"
                tvDetailTotalPrice.text = "€ 214.42"
            }
        }

        fun updateWishlistIcon(isWishlisted: Boolean) {
            ivWishlistToggle.setImageResource(R.drawable.ic_heart)
            if (isWishlisted) {
                ivWishlistToggle.setColorFilter(Color.RED)
            } else {
                ivWishlistToggle.setColorFilter(Color.parseColor("#3A3A3A"))
            }
        }

        val wishlistSet = sharedPrefs.getStringSet("wishlist_product_ids", emptySet()) ?: emptySet()
        var isWishlisted = wishlistSet.contains(productId.toString())
        updateWishlistIcon(isWishlisted)

        ivWishlistToggle.setOnClickListener {
            val set = sharedPrefs.getStringSet("wishlist_product_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (isWishlisted) {
                set.remove(productId.toString())
                isWishlisted = false
                Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
            } else {
                set.add(productId.toString())
                isWishlisted = true
                Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
            }
            sharedPrefs.edit().putStringSet("wishlist_product_ids", set).apply()
            updateWishlistIcon(isWishlisted)
        }

        findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnBuyNow)?.setOnClickListener {
            val randomPackageId = "J" + (100000000..999999999).random().toString() + "EU"
            sharedPrefs.edit().putString("last_package_id", randomPackageId).apply()
            startActivity(Intent(this, PurchasedActivity::class.java))
        }
    }
}

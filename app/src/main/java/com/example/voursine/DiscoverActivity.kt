package com.example.voursine

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.delay

class DiscoverActivity : AppCompatActivity() {

    private var isBalanceHidden = false

    // Carousel slide pairs (Variant A, Variant B)
    private val slidePairs = listOf(
        Pair(R.drawable.p1_a, R.drawable.p1_b),
        Pair(R.drawable.p2_a, R.drawable.p2_b),
        Pair(R.drawable.p3_a, R.drawable.p3_b),
        Pair(R.drawable.p4_a, R.drawable.p4_b),
        Pair(R.drawable.p5_a, R.drawable.p5_b),
    )

    private var currentSlideIndex = 0
    private var isVariantB = false
    private var showingPrimary = true

    private lateinit var ivCarouselPrimary: ImageView
    private lateinit var ivCarouselSecondary: ImageView
    private lateinit var flCarouselContainer: View
    private lateinit var btnChevronUp: View
    private lateinit var btnChevronDown: View

    private val handler = Handler(Looper.getMainLooper())
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            // Auto-scroll ONLY when Variant A is showing (not in Variant B mode)
            if (!isVariantB) {
                slideVerticalToNext()
            }
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_discover)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val tvBalanceAmount = findViewById<TextView>(R.id.tvBalanceAmount)
        val btnHideBalance = findViewById<Button>(R.id.btnHideBalance)
        val btnTopUp = findViewById<Button>(R.id.btnTopUp)

        // Account Balance Hide/Show toggle
        btnHideBalance.setOnClickListener {
            isBalanceHidden = !isBalanceHidden
            if (isBalanceHidden) {
                tvBalanceAmount.text = getString(R.string.censored_balance)
                btnHideBalance.text = getString(R.string.show)
            } else {
                tvBalanceAmount.text = getString(R.string.default_balance)
                btnHideBalance.text = getString(R.string.hide)
            }
        }

        // Top Up button directs to MaintenanceActivity
        btnTopUp.setOnClickListener {
            startActivity(Intent(this, MaintenanceActivity::class.java))
        }

        // Account button directs to AccountActivity
        findViewById<View>(R.id.btnAccount)?.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        // Search bar click listener
        findViewById<View>(R.id.tvSearchBar)?.setOnClickListener { showSearchDialog() }

        // Carousel initialization
        ivCarouselPrimary = findViewById(R.id.ivCarouselPrimary)
        ivCarouselSecondary = findViewById(R.id.ivCarouselSecondary)
        flCarouselContainer = findViewById(R.id.flCarouselContainer)
        btnChevronUp = findViewById(R.id.btnChevronUp)
        btnChevronDown = findViewById(R.id.btnChevronDown)
        val ivCartOverlay = findViewById<View>(R.id.ivCartOverlay)

        ivCarouselPrimary.setImageResource(slidePairs[0].first)
        ivCarouselPrimary.alpha = 1f
        ivCarouselSecondary.alpha = 0f

        // Cart Icon on carousel directs to product detail page
        ivCartOverlay?.setOnClickListener {
            val productId = currentSlideIndex + 1
            val intent = Intent(this, ProductDetailActivity::class.java).apply {
                putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId)
            }
            startActivity(intent)
        }

        // Click image to fade transition between variant A and B
        flCarouselContainer.setOnClickListener {
            toggleImageVariant()
        }

        var lastClickTime = 0L
        val clickDelay = 600L // 600ms

// Ascending Scroll Button (Chevron Up) -> Slide to previous image
        btnChevronUp.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= clickDelay) {
                lastClickTime = currentTime
                if (!isVariantB) {
                    resetAutoSlideTimer()
                    slideVerticalToPrevious()
                }
            }
        }

// Descending Scroll Button (Chevron Down) -> Slide to next image
        btnChevronDown.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= clickDelay) {
                lastClickTime = currentTime
                if (!isVariantB) {
                    resetAutoSlideTimer()
                    slideVerticalToNext()
                }
            }
        }

        // Product Grid item click listeners -> Open ProductDetailActivity
        setupProductGridListeners()

        startAutoSlideTimer()
    }

    private fun setupProductGridListeners() {
        val itemClickMap = mapOf(
            R.id.itemProduct1 to 1,
            R.id.itemProduct2 to 2,
            R.id.itemProduct3 to 3,
            R.id.itemProduct4 to 4,
            R.id.itemProduct5 to 5,
            R.id.itemProduct6 to 6,
            R.id.itemProduct7 to 7,
            R.id.itemProduct8 to 8,
        )

        for ((viewId, productId) in itemClickMap) {
            findViewById<View>(viewId)?.setOnClickListener {
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId)
                }
                startActivity(intent)
            }
        }
    }

    private fun startAutoSlideTimer() {
        handler.removeCallbacks(autoSlideRunnable)
        handler.postDelayed(autoSlideRunnable, 5000)
    }

    private fun resetAutoSlideTimer() {
        handler.removeCallbacks(autoSlideRunnable)
        handler.postDelayed(autoSlideRunnable, 5000)
    }

    private fun slideVerticalToNext() {
        if (slidePairs.isEmpty()) return

        currentSlideIndex = (currentSlideIndex + 1) % slidePairs.size
        isVariantB = false

        val currentView = if (showingPrimary) ivCarouselPrimary else ivCarouselSecondary
        val nextView = if (showingPrimary) ivCarouselSecondary else ivCarouselPrimary

        val height = if (flCarouselContainer.height > 0) flCarouselContainer.height.toFloat() else 600f
        val nextRes = slidePairs[currentSlideIndex].first

        nextView.setImageResource(nextRes)
        nextView.translationY = height
        nextView.alpha = 1f

        nextView.animate()
            .translationY(0f)
            .setDuration(600)
            .start()

        currentView.animate()
            .translationY(-height)
            .setDuration(600)
            .withEndAction {
                currentView.translationY = 0f
                currentView.alpha = 0f
                showingPrimary = !showingPrimary
            }
            .start()
    }

    private fun slideVerticalToPrevious() {
        if (slidePairs.isEmpty()) return

        currentSlideIndex = ((currentSlideIndex - 1) + slidePairs.size) % slidePairs.size
        isVariantB = false

        val currentView = if (showingPrimary) ivCarouselPrimary else ivCarouselSecondary
        val nextView = if (showingPrimary) ivCarouselSecondary else ivCarouselPrimary

        val height = if (flCarouselContainer.height > 0) flCarouselContainer.height.toFloat() else 600f
        val nextRes = slidePairs[currentSlideIndex].first

        nextView.setImageResource(nextRes)
        nextView.translationY = -height
        nextView.alpha = 1f

        nextView.animate()
            .translationY(0f)
            .setDuration(600)
            .start()

        currentView.animate()
            .translationY(height)
            .setDuration(600)
            .withEndAction {
                currentView.translationY = 0f
                currentView.alpha = 0f
                showingPrimary = !showingPrimary
            }
            .start()
    }

    private fun toggleImageVariant() {
        isVariantB = !isVariantB

        if (isVariantB) {
            // STOP auto-sliding & HIDE scroll buttons when Variant B (e.g. p1_b) is showing
            handler.removeCallbacks(autoSlideRunnable)
            btnChevronUp.visibility = View.INVISIBLE
            btnChevronDown.visibility = View.INVISIBLE
        } else {
            // RESUME auto-sliding & SHOW scroll buttons when toggled back to Variant A (e.g. p1_a)
            btnChevronUp.visibility = View.VISIBLE
            btnChevronDown.visibility = View.VISIBLE
            resetAutoSlideTimer()
        }

        val targetRes = if (isVariantB) {
            slidePairs[currentSlideIndex].second
        } else {
            slidePairs[currentSlideIndex].first
        }

        val currentView = if (showingPrimary) ivCarouselPrimary else ivCarouselSecondary
        val targetView = if (showingPrimary) ivCarouselSecondary else ivCarouselPrimary

        targetView.setImageResource(targetRes)
        targetView.translationY = 0f
        targetView.alpha = 0f

        targetView.animate()
            .alpha(1f)
            .setDuration(450)
            .start()

        currentView.animate()
            .alpha(0f)
            .setDuration(450)
            .withEndAction {
                showingPrimary = !showingPrimary
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoSlideRunnable)
    }

    private fun showSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search, null)
        val etSearchQuery = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchQuery)
        val rvSearchResults = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSearchResults)
        val tvNoResults = dialogView.findViewById<TextView>(R.id.tvNoResults)

        val allProducts = listOf(
            WishlistActivity.ProductItem(1, "Heritage Replica Long Sleeve Polo", "€ 76.95", R.drawable.p1_s),
            WishlistActivity.ProductItem(2, "Midweight Fleece Icon", "€ 126.00", R.drawable.p2_s),
            WishlistActivity.ProductItem(3, "North Dome Wind Jacket", "€ 83.02", R.drawable.p3_s),
            WishlistActivity.ProductItem(4, "Water-Repellent Hooded Windbreaker", "€ 110.52", R.drawable.p4_s),
            WishlistActivity.ProductItem(5, "Pullover Windbreaker", "€ 35.77", R.drawable.p5_s),
            WishlistActivity.ProductItem(6, "Utility Cotton Blend Parka", "€ 68.90", R.drawable.p6_s),
            WishlistActivity.ProductItem(7, "Seamless Down Parka", "€ 143.60", R.drawable.p7_s),
            WishlistActivity.ProductItem(8, "Schott Parka", "€ 214.42", R.drawable.p8_s),
        )

        rvSearchResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        fun filterProducts(query: String) {
            val trimmed = query.trim()
            val filtered = if (trimmed.isEmpty()) {
                allProducts
            } else {
                allProducts.filter { product ->
                    val name = product.name
                    val matchesKeyword = name.contains(trimmed, ignoreCase = true)
                    val matchesInitial = name.split(" ").any { it.startsWith(trimmed, ignoreCase = true) }
                    matchesKeyword || matchesInitial
                }
            }

            if (filtered.isEmpty()) {
                rvSearchResults.visibility = View.GONE
                tvNoResults.visibility = View.VISIBLE
            } else {
                rvSearchResults.visibility = View.VISIBLE
                tvNoResults.visibility = View.GONE
                rvSearchResults.adapter = WishlistActivity.WishlistAdapter(
                    items = filtered,
                    onClick = { product ->
                        dialog.dismiss()
                        val intent = Intent(this, ProductDetailActivity::class.java).apply {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                        }
                        startActivity(intent)
                    },
                    onRemove = { product ->
                        dialog.dismiss()
                        val intent = Intent(this, ProductDetailActivity::class.java).apply {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }

        filterProducts("")

        etSearchQuery.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                filterProducts(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialog.show()
    }
}

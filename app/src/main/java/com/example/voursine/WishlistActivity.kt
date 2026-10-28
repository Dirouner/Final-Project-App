package com.example.voursine

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WishlistActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    data class ProductItem(val id: Int, val name: String, val price: String, val imageRes: Int)

    private val allProducts = listOf(
        ProductItem(1, "Heritage Replica Long Sleeve Polo", "€ 76.95", R.drawable.p1_s),
        ProductItem(2, "Midweight Fleece Icon", "€ 126.00", R.drawable.p2_s),
        ProductItem(3, "North Dome Wind Jacket", "€ 83.02", R.drawable.p3_s),
        ProductItem(4, "Water-Repellent Hooded Windbreaker", "€ 110.52", R.drawable.p4_s),
        ProductItem(5, "Pullover Windbreaker", "€ 35.77", R.drawable.p5_s),
        ProductItem(6, "Utility Cotton Blend Parka", "€ 68.90", R.drawable.p6_s),
        ProductItem(7, "Seamless Down Parka", "€ 143.60", R.drawable.p7_s),
        ProductItem(8, "Schott Parka", "€ 214.42", R.drawable.p8_s),
    )

    private lateinit var rvWishlist: RecyclerView
    private lateinit var tvEmptyWishlist: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wishlist)

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

        rvWishlist = findViewById(R.id.rvWishlist)
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist)
        rvWishlist.layoutManager = LinearLayoutManager(this)

        loadWishlist()
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }

    private fun loadWishlist() {
        val wishlistSet = sharedPrefs.getStringSet("wishlist_product_ids", emptySet()) ?: emptySet()
        val wishlistedProducts = allProducts.filter { wishlistSet.contains(it.id.toString()) }

        if (wishlistedProducts.isEmpty()) {
            rvWishlist.visibility = View.GONE
            tvEmptyWishlist.visibility = View.VISIBLE
        } else {
            rvWishlist.visibility = View.VISIBLE
            tvEmptyWishlist.visibility = View.GONE
            rvWishlist.adapter = WishlistAdapter(
                items = wishlistedProducts,
                onClick = { product ->
                    val intent = Intent(this, ProductDetailActivity::class.java).apply {
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                    }
                    startActivity(intent)
                },
                onRemove = { product ->
                    val updatedSet = wishlistSet.toMutableSet()
                    updatedSet.remove(product.id.toString())
                    sharedPrefs.edit().putStringSet("wishlist_product_ids", updatedSet).apply()
                    loadWishlist()
                }
            )
        }
    }

    class WishlistAdapter(
        private val items: List<ProductItem>,
        private val onClick: (ProductItem) -> Unit,
        private val onRemove: (ProductItem) -> Unit
    ) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivProduct: ImageView = view.findViewById(R.id.ivWishlistProduct)
            val tvName: TextView = view.findViewById(R.id.tvWishlistProductName)
            val tvPrice: TextView = view.findViewById(R.id.tvWishlistProductPrice)
            val btnRemove: ImageView = view.findViewById(R.id.btnRemoveWishlist)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wishlist, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.ivProduct.setImageResource(item.imageRes)
            holder.tvName.text = item.name
            holder.tvPrice.text = item.price

            holder.itemView.setOnClickListener { onClick(item) }
            holder.btnRemove.setOnClickListener { onRemove(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}

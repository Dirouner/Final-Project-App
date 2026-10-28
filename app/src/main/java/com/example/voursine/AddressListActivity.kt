package com.example.voursine

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class AddressListActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var rvAddresses: RecyclerView
    private lateinit var tvEmptyAddresses: TextView

    data class AddressItem(
        val id: String,
        val ownerName: String,
        val phone: String,
        val addressDetails: String,
        val coords: String,
        val info: String,
        val isActive: Boolean
    )

    private val addressList = mutableListOf<AddressItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_address_list)

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

        rvAddresses = findViewById(R.id.rvAddresses)
        tvEmptyAddresses = findViewById(R.id.tvEmptyAddresses)
        rvAddresses.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnAddAddress).setOnClickListener {
            showAddAddressDialog()
        }

        loadAddresses()
    }

    override fun onResume() {
        super.onResume()
        loadAddresses()
    }

    private fun loadAddresses() {
        addressList.clear()
        val jsonStr = sharedPrefs.getString("saved_addresses_json", null)
        val activeId = sharedPrefs.getString("active_address_id", "default_1") ?: "default_1"

        if (jsonStr.isNullOrEmpty()) {
            // Add default address matching original shipping address
            val defaultAddress = AddressItem(
                id = "default_1",
                ownerName = sharedPrefs.getString("user_fullname", "Monaco Resident") ?: "Monaco Resident",
                phone = sharedPrefs.getString("user_phone", "+1 555-0199") ?: "+1 555-0199",
                addressDetails = "Le Sardanapale, 2 Av. Princesse Grace, 98000 Monaco",
                coords = "43.7384° N, 7.4246° E (Map Simulation)",
                info = "Main residence, ring doorbell.",
                isActive = true
            )
            addressList.add(defaultAddress)
            saveAddressesToPrefs()
            sharedPrefs.edit().putString("active_address_id", "default_1").apply()
        } else {
            try {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getString("id")
                    val owner = obj.getString("owner")
                    val phone = obj.getString("phone")
                    val details = obj.getString("details")
                    val coords = obj.getString("coords")
                    val info = obj.getString("info")
                    val isActive = (id == activeId)
                    addressList.add(AddressItem(id, owner, phone, details, coords, info, isActive))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (addressList.isEmpty()) {
            rvAddresses.visibility = View.GONE
            tvEmptyAddresses.visibility = View.VISIBLE
        } else {
            rvAddresses.visibility = View.VISIBLE
            tvEmptyAddresses.visibility = View.GONE
            rvAddresses.adapter = AddressAdapter(
                items = addressList,
                onSelectActive = { address ->
                    sharedPrefs.edit().putString("active_address_id", address.id).apply()
                    loadAddresses()
                    Toast.makeText(this, "Active address updated!", Toast.LENGTH_SHORT).show()
                },
                onDelete = { address ->
                    if (addressList.size <= 1) {
                        Toast.makeText(this, "Cannot delete the last address.", Toast.LENGTH_SHORT).show()
                        return@AddressAdapter
                    }
                    addressList.remove(address)
                    if (address.id == activeId && addressList.isNotEmpty()) {
                        sharedPrefs.edit().putString("active_address_id", addressList[0].id).apply()
                    }
                    saveAddressesToPrefs()
                    loadAddresses()
                }
            )
        }
    }

    private fun saveAddressesToPrefs() {
        val jsonArray = JSONArray()
        for (item in addressList) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("owner", item.ownerName)
                put("phone", item.phone)
                put("details", item.addressDetails)
                put("coords", item.coords)
                put("info", item.info)
            }
            jsonArray.put(obj)
        }
        sharedPrefs.edit().putString("saved_addresses_json", jsonArray.toString()).apply()
    }

    private fun showAddAddressDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val etOwner = EditText(this).apply { hint = "Owner Full Name"; setText(sharedPrefs.getString("user_fullname", "")) }
        val etPhone = EditText(this).apply { hint = "Phone Number"; setText(sharedPrefs.getString("user_phone", "")) }
        val etDetails = EditText(this).apply { hint = "Address (Street, City, Country)" }
        val etCoords = EditText(this).apply { hint = "Map Coordinates (Simulation)"; setText("48.8566° N, 2.3522° E (Paris)") }
        val etInfo = EditText(this).apply { hint = "Additional Info (Apt, Floor, Instructions)" }

        layout.addView(etOwner)
        layout.addView(etPhone)
        layout.addView(etDetails)
        layout.addView(etCoords)
        layout.addView(etInfo)

        AlertDialog.Builder(this)
            .setTitle("Add New Address")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val owner = etOwner.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val details = etDetails.text.toString().trim()
                val coords = etCoords.text.toString().trim()
                val info = etInfo.text.toString().trim()

                if (details.isEmpty()) {
                    Toast.makeText(this, "Address details cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newId = System.currentTimeMillis().toString()
                val newItem = AddressItem(
                    id = newId,
                    ownerName = if (owner.isEmpty()) "Resident" else owner,
                    phone = if (phone.isEmpty()) "+1 555-0199" else phone,
                    addressDetails = details,
                    coords = if (coords.isEmpty()) "43.7384° N, 7.4246° E" else coords,
                    info = info,
                    isActive = false
                )

                addressList.add(newItem)
                saveAddressesToPrefs()
                if (addressList.size == 1) {
                    sharedPrefs.edit().putString("active_address_id", newId).apply()
                }
                loadAddresses()
                Toast.makeText(this, "Address added successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class AddressAdapter(
        private val items: List<AddressItem>,
        private val onSelectActive: (AddressItem) -> Unit,
        private val onDelete: (AddressItem) -> Unit
    ) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvOwner: TextView = view.findViewById(R.id.tvAddressOwner)
            val tvPhone: TextView = view.findViewById(R.id.tvAddressPhone)
            val tvDetails: TextView = view.findViewById(R.id.tvAddressDetails)
            val tvCoords: TextView = view.findViewById(R.id.tvAddressCoords)
            val tvInfo: TextView = view.findViewById(R.id.tvAddressInfo)
            val tvActiveBadge: TextView = view.findViewById(R.id.tvActiveBadge)
            val btnSelect: Button = view.findViewById(R.id.btnSelectAddress)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvOwner.text = item.ownerName
            holder.tvPhone.text = item.phone
            holder.tvDetails.text = item.addressDetails
            holder.tvCoords.text = item.coords
            holder.tvInfo.text = item.info

            if (item.isActive) {
                holder.tvActiveBadge.visibility = View.VISIBLE
                holder.btnSelect.text = "Active Address"
                holder.btnSelect.isEnabled = false
                holder.btnSelect.alpha = 0.6f
            } else {
                holder.tvActiveBadge.visibility = View.GONE
                holder.btnSelect.text = "Select Active"
                holder.btnSelect.isEnabled = true
                holder.btnSelect.alpha = 1.0f
            }

            holder.btnSelect.setOnClickListener {
                onSelectActive(item)
            }

            holder.itemView.setOnLongClickListener {
                onDelete(item)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }
}

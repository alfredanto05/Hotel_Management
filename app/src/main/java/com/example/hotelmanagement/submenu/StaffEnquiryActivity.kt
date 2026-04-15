package com.example.hotelmanagement.submenu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffEnquiryActivity : AppCompatActivity() {

    private lateinit var etStaffId: EditText
    private lateinit var btnSearchStaff: Button
    private lateinit var tvStaffResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_enquiry)

        // 1. Link UI elements
        etStaffId = findViewById(R.id.etStaffId)
        btnSearchStaff = findViewById(R.id.btnSearchStaff)
        tvStaffResults = findViewById(R.id.tvStaffResults)

        // 2. Set up the Search Button click listener
        btnSearchStaff.setOnClickListener {
            val staffId = etStaffId.text.toString().trim()

            if (staffId.isEmpty()) {
                Toast.makeText(this, "Please enter a valid Staff ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            tvStaffResults.text = "Searching database for $staffId..."
            tvStaffResults.setTextColor(android.graphics.Color.parseColor("#AAAAAA")) // Gray text while loading

            // 3. Launch Coroutine for Network Request
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Make the API call to your Python server
                    val response = NetworkManager.api.getStaff(staffId)

                    // Switch back to Main Thread to update UI
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val staffDetails = response.body()!!.data

                            if (staffDetails == null) {
                                // No data returned for that ID
                                tvStaffResults.text = "No active staff member found matching ID: $staffId"
                                tvStaffResults.setTextColor(android.graphics.Color.parseColor("#FF5555")) // Red text for error
                            } else {
                                // Format the Map data cleanly
                                tvStaffResults.setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // White text for success
                                var resultText = "--- OFFICIAL PROFILE ---\n\n"

                                for ((key, value) in staffDetails) {
                                    // Clean up the keys (e.g., changes "first_name" to "FIRST NAME")
                                    val cleanKey = key.replace("_", " ").uppercase()
                                    resultText += "$cleanKey:\n$value\n\n"
                                }
                                tvStaffResults.text = resultText
                            }
                        } else {
                            tvStaffResults.text = "Server Error: Could not retrieve data."
                        }
                    }
                } catch (e: Exception) {
                    // Handle network timeout or server offline
                    withContext(Dispatchers.Main) {
                        tvStaffResults.text = "Network Error: Cannot connect to server.\nMake sure the Python backend is running."
                        tvStaffResults.setTextColor(android.graphics.Color.parseColor("#FF5555"))
                    }
                }
            }
        }
    }
}
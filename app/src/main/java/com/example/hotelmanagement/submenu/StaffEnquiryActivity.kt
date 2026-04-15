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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_enquiry)

        val etStaffId = findViewById<EditText>(R.id.etStaffId)
        val btnSearchStaff = findViewById<Button>(R.id.btnSearchStaff)
        val tvStaffResults = findViewById<TextView>(R.id.tvStaffResults)

        btnSearchStaff.setOnClickListener {
            val staffId = etStaffId.text.toString().trim()
            if (staffId.isEmpty()) {
                Toast.makeText(this, "Please enter a Staff ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStaffResults.text = "Searching..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = NetworkManager.api.getStaff(staffId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val staffDetails = response.body()!!.data
                            if (staffDetails == null) {
                                tvStaffResults.text = "No staff member found with ID: $staffId"
                            } else {
                                var resultText = "--- Staff Profile ---\n\n"
                                for ((key, value) in staffDetails) {
                                    resultText += "${key.uppercase()}: $value\n"
                                }
                                tvStaffResults.text = resultText
                            }
                        } else {
                            tvStaffResults.text = "Error fetching data."
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { tvStaffResults.text = "Network Error" }
                }
            }        }
    }
}
package com.example.hotelmanagement.submenu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelmanagement.GuestHistoryRequest
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuestEnquiryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_enquiry)

        val etSearchName = findViewById<EditText>(R.id.etSearchName)
        val etSearchPhone = findViewById<EditText>(R.id.etSearchPhone)
        val btnSearchGuest = findViewById<Button>(R.id.btnSearchGuest)
        val tvGuestResults = findViewById<TextView>(R.id.tvGuestResults)

        btnSearchGuest.setOnClickListener {
            val name = etSearchName.text.toString().trim()
            val phone = etSearchPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvGuestResults.text = "Searching records..."
            val request = GuestHistoryRequest(name, phone)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = NetworkManager.api.getGuestHistory(request)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val data = response.body()!!.data
                            if (data.isEmpty()) {
                                tvGuestResults.text = "No past bookings found."
                            } else {
                                var resultText = "--- Booking History (${data.size} Stays) ---\n\n"
                                for ((index, row) in data.withIndex()) {
                                    resultText += "Stay #${index + 1}\n"
                                    resultText += "Room No: ${row["room_no"]}\n"
                                    resultText += "Check-In: ${row["check_in"]}\n"
                                    resultText += "Check-Out: ${row["check_out"]}\n"
                                    resultText += "---------------------------\n"
                                }
                                tvGuestResults.text = resultText
                            }
                        } else {
                            tvGuestResults.text = "Server Error fetching data."
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { tvGuestResults.text = "Network Error" }
                }
            }
        }
    }
}
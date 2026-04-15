package com.example.hotelmanagement.submenu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelmanagement.MainActivity
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import com.example.hotelmanagement.SqlQueries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinalBookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_booking)

        // 1. Get the data passed from the previous screen
        val roomNo = intent.getStringExtra("ROOM_NO") ?: "Unknown"
        val checkIn = intent.getStringExtra("CHECK_IN") ?: "Unknown"
        val checkOut = intent.getStringExtra("CHECK_OUT") ?: "Unknown"

        // 2. Link UI elements
        val tvSummaryRoom = findViewById<TextView>(R.id.tvSummaryRoom)
        val tvSummaryDates = findViewById<TextView>(R.id.tvSummaryDates)
        val etGuestName = findViewById<EditText>(R.id.etGuestName)
        val etGuestPhone = findViewById<EditText>(R.id.etGuestPhone)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        // 3. Display the read-only data
        tvSummaryRoom.text = "Room: $roomNo"
        tvSummaryDates.text = "Dates: $checkIn to $checkOut"

        // 4. Handle the Book button click
        btnConfirmBooking.setOnClickListener {
            val guestName = etGuestName.text.toString().trim()
            val guestPhone = etGuestPhone.text.toString().trim()

            // Rookie-friendly validation
            if (guestName.isEmpty() || guestPhone.isEmpty()) {
                Toast.makeText(this, "Please fill in all guest details!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = BookingRequest(roomNo, guestName, guestPhone, checkIn, checkOut)
            // Disable button to prevent double-clicking
            btnConfirmBooking.isEnabled = false
            btnConfirmBooking.text = "Booking..."

            // Execute the network request
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = NetworkManager.api.createBooking(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val bookingId = response.body()?.booking_id
                            Toast.makeText(this@FinalBookingActivity, "Booked! ID: $bookingId", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@FinalBookingActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@FinalBookingActivity, "Server Error", Toast.LENGTH_SHORT).show()
                            btnConfirmBooking.isEnabled = true
                            btnConfirmBooking.text = "Confirm & Book"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FinalBookingActivity, "Network Error", Toast.LENGTH_SHORT).show()
                        btnConfirmBooking.isEnabled = true
                        btnConfirmBooking.text = "Confirm & Book"
                    }
                }
            }        }
    }
}
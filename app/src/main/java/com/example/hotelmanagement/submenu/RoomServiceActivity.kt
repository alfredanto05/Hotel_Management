package com.example.hotelmanagement.submenu

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class RoomServiceActivity : AppCompatActivity() {

    private lateinit var etServiceRoomNo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_service)

        etServiceRoomNo = findViewById(R.id.etServiceRoomNo)

        val btnRequestCleaning = findViewById<Button>(R.id.btnRequestCleaning)
        val itemPaneer = findViewById<FrameLayout>(R.id.itemPaneer)
        val itemBiryani = findViewById<FrameLayout>(R.id.itemBiryani)
        val itemChai = findViewById<FrameLayout>(R.id.itemChai)
        val itemDessert = findViewById<FrameLayout>(R.id.itemDessert)
        val menuContainer = findViewById<LinearLayout>(R.id.menuContainer)

        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        menuContainer.startAnimation(slideUpAnim)

        // Housekeeping Click Listener
        btnRequestCleaning.setOnClickListener { processHousekeeping() }

        // Food Menu Click Listeners
        itemPaneer.setOnClickListener { processOrder("Paneer Tikka Masala", 450.00) }
        itemBiryani.setOnClickListener { processOrder("Awadhi Mutton Biryani", 650.00) }
        itemChai.setOnClickListener { processOrder("Masala Chai Pot", 150.00) }
        itemDessert.setOnClickListener { processOrder("Kesari Rasmalai", 200.00) }
    }

    private fun processHousekeeping() {
        val roomNo = etServiceRoomNo.text.toString().trim()
        if (roomNo.isEmpty()) {
            Toast.makeText(this, "Enter Room Number to request cleaning!", Toast.LENGTH_SHORT).show()
            etServiceRoomNo.requestFocus()
            return
        }

        Toast.makeText(this, "Notifying housekeeping staff...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkManager.api.requestCleaning(CleaningRequest(roomNo))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RoomServiceActivity, "Cleaning requested for Room $roomNo!", Toast.LENGTH_LONG).show()
                        etServiceRoomNo.text.clear()
                    } else {
                        Toast.makeText(this@RoomServiceActivity, "Server Error.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@RoomServiceActivity, "Network Error", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    private fun processOrder(itemName: String, price: Double) {
        val roomNo = etServiceRoomNo.text.toString().trim()
        if (roomNo.isEmpty()) {
            Toast.makeText(this, "Please enter a Room Number first!", Toast.LENGTH_SHORT).show()
            etServiceRoomNo.requestFocus()
            return
        }

        Toast.makeText(this, "Sending order to kitchen...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkManager.api.orderFood(FoodOrderRequest(roomNo, itemName, price))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RoomServiceActivity, "$itemName ordered for Room $roomNo!", Toast.LENGTH_LONG).show()
                        etServiceRoomNo.text.clear()
                    } else {
                        Toast.makeText(this@RoomServiceActivity, "Server Error.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@RoomServiceActivity, "Network Error", Toast.LENGTH_SHORT).show() }
            }
        }
    }}
package com.example.hotelmanagement.submenu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hotelmanagement.CleaningRequest
import com.example.hotelmanagement.FoodOrderRequest
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomServiceActivity : AppCompatActivity() {

    private lateinit var etServiceRoomNo: EditText
    private lateinit var btnRequestCleaning: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_service)

        // 1. Link UI Inputs
        etServiceRoomNo = findViewById(R.id.etServiceRoomNo)
        btnRequestCleaning = findViewById(R.id.btnRequestCleaning)

        // 2. Link the Image Views
        val imgPaneer = findViewById<ImageView>(R.id.imgPaneer)
        val imgBiryani = findViewById<ImageView>(R.id.imgBiryani)
        val imgChai = findViewById<ImageView>(R.id.imgChai)
        val imgDessert = findViewById<ImageView>(R.id.imgDessert)

        // 3. Load Images from the Internet using Glide
        Glide.with(this)
            .load("https://www.cookwithkushi.com/wp-content/uploads/2023/02/tandoori_paneer_tikka_restaurant_style.jpg")
            .into(imgPaneer)

        Glide.with(this)
            .load("https://i.pinimg.com/736x/a6/40/04/a64004d2cb15613a3e9924136d027322.jpg")
            .into(imgBiryani)

        Glide.with(this)
            .load("https://thumbs.dreamstime.com/b/indian-street-foods-spicy-samosa-tea-fried-served-milk-masla-chai-exotic-recipes-155782751.jpg")
            .into(imgChai)

        Glide.with(this)
            .load("https://cdn.zeptonow.com/production/tr:w-640,ar-5198-5198,pr-true,f-auto,q-40/cms/product_variant/877caa67-3037-4e5f-bf03-bfb76d2a7413.jpeg")
            .into(imgDessert)

        // 4. Setup Housekeeping Click
        btnRequestCleaning.setOnClickListener {
            processHousekeeping()
        }

        // 5. Setup Food Order Clicks
        findViewById<FrameLayout>(R.id.itemPaneer).setOnClickListener {
            processOrder("Paneer Tikka Masala", 450.0)
        }
        findViewById<FrameLayout>(R.id.itemBiryani).setOnClickListener {
            processOrder("Awadhi Mutton Biryani", 650.0)
        }
        findViewById<FrameLayout>(R.id.itemChai).setOnClickListener {
            processOrder("Masala Chai Pot", 150.0)
        }
        findViewById<FrameLayout>(R.id.itemDessert).setOnClickListener {
            processOrder("Kesari Rasmalai", 200.0)
        }
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
                // Call the new Python API endpoint
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RoomServiceActivity, "Network Error. Is the server running?", Toast.LENGTH_SHORT).show()
                }
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
                // Call the new Python API endpoint
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RoomServiceActivity, "Network Error. Is the server running?", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
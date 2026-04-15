package com.example.hotelmanagement

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.graphics.drawable.AnimationDrawable
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardDashboard = findViewById<FrameLayout>(R.id.cardDashboard)        // 1. Find your cards by their IDs
        val cardRoomBooking = findViewById<FrameLayout>(R.id.cardRoomBooking)
        val cardGuestInfo = findViewById<FrameLayout>(R.id.cardGuestInfo)
        val cardRoomService = findViewById<FrameLayout>(R.id.cardRoomService)
        //val cardBilling = findViewById<CardView>(R.id.cardBilling)
        val cardStaffRoster = findViewById<FrameLayout>(R.id.cardStaffRoster)

        // Load the animation
        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)

        // Apply animation to cards (they will all play at once by default)
        cardRoomBooking.startAnimation(slideUpAnim)
        cardGuestInfo.startAnimation(slideUpAnim)
        cardRoomService.startAnimation(slideUpAnim)
       // cardBilling.startAnimation(slideUpAnim)
        // 2. Set onClickListeners for each specific card
        cardDashboard.setOnClickListener {
            val intent = Intent(this, com.example.hotelmanagement.submenu.DashboardActivity::class.java)
            startActivity(intent)
        }
        cardRoomBooking.setOnClickListener {
            Toast.makeText(this, "Loading Room Booking...", Toast.LENGTH_SHORT).show()


             val intent = Intent(this, com.example.hotelmanagement.submenu.RoomBookingActivity::class.java)
             startActivity(intent)
        }

        cardGuestInfo.setOnClickListener {
            val intent = Intent(this, com.example.hotelmanagement.submenu.GuestEnquiryActivity::class.java)
            startActivity(intent)
        }

        cardStaffRoster.setOnClickListener {
            val intent = Intent(this, com.example.hotelmanagement.submenu.StaffEnquiryActivity::class.java)
            startActivity(intent)
        }


        cardRoomService.setOnClickListener {
            startActivity(Intent(this, com.example.hotelmanagement.submenu.RoomServiceActivity::class.java))
        }

        //cardBilling.setOnClickListener {
        //    Toast.makeText(this, "Loading Billing...", Toast.LENGTH_SHORT).show()
        //}
    }
}
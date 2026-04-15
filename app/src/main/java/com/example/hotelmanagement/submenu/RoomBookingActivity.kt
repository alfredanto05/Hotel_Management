package com.example.hotelmanagement.submenu
import android.widget.EditText
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import com.example.hotelmanagement.SqlQueries
import com.example.hotelmanagement.SqlQueryRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import android.view.animation.AnimationUtils
import android.widget.LinearLayout

class RoomBookingActivity : AppCompatActivity() {

    private var checkInDate: String = ""
    private var checkOutDate: String = ""

    // Store references to our dynamically created buttons
    private val roomButtons = mutableMapOf<String, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_booking)

        // Find the new EditText fields instead of Buttons
        val etCheckIn = findViewById<EditText>(R.id.etCheckIn)
        val etCheckOut = findViewById<EditText>(R.id.etCheckOut)
        val dateContainer = findViewById<LinearLayout>(R.id.dateContainer)
        val btnViewRooms = findViewById<Button>(R.id.btnViewRooms)
        val gridRooms = findViewById<GridLayout>(R.id.gridRooms)
        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)

// Animate the elements appearing
        dateContainer.startAnimation(slideUpAnim)
        btnViewRooms.startAnimation(slideUpAnim)
        gridRooms.startAnimation(slideUpAnim)

        setupRoomGrid(gridRooms)

        etCheckIn.setOnClickListener {
            showDatePicker { selectedDate ->
                checkInDate = selectedDate
                // Update the text field to show the date in place
                etCheckIn.setText(selectedDate)
            }
        }

        etCheckOut.setOnClickListener {
            showDatePicker { selectedDate ->
                checkOutDate = selectedDate
                // Update the text field to show the date in place
                etCheckOut.setText(selectedDate)
            }
        }

        btnViewRooms.setOnClickListener {
            if (checkInDate.isEmpty() || checkOutDate.isEmpty()) {
                Toast.makeText(this, "Please select both dates!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchRoomAvailability()
        }
    }

    private fun setupRoomGrid(gridRooms: GridLayout) {
        for (floor in 1..4) {
            for (room in 1..4) {
                val roomNo = "${floor}0${room}"

                val button = Button(this).apply {
                    text = roomNo
                    setTextColor(Color.WHITE)
                    // Apply our premium dark card background with the gold border
                    setBackgroundResource(R.drawable.bg_premium_card)

                    val params = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(10, 10, 10, 10) // Slightly larger margins
                    }
                    layoutParams = params
                    setOnClickListener { handleRoomClick(roomNo) }
                }

                roomButtons[roomNo] = button
                gridRooms.addView(button)
            }
        }
    }

    private fun fetchRoomAvailability() {
        // Reset all buttons to Forest Green (Available)
        roomButtons.values.forEach {
            it.setBackgroundColor(Color.parseColor("#1B5E20"))
            it.setTextColor(Color.WHITE)
        }

        val request = AvailabilityRequest(checkInDate, checkOutDate)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkManager.api.checkAvailability(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val bookedRoomsList = response.body()!!.data

                        for (row in bookedRoomsList) {
                            val bookedRoomNo = row["room_no"]
                            if (bookedRoomNo != null && roomButtons.containsKey(bookedRoomNo)) {
                                // Mark it Deep Crimson (Booked)
                                roomButtons[bookedRoomNo]?.setBackgroundColor(android.graphics.Color.parseColor("#7A1010"))
                            }
                        }
                        Toast.makeText(this@RoomBookingActivity, "Availability Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RoomBookingActivity, "Server Error", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@RoomBookingActivity, "Network Error", Toast.LENGTH_SHORT).show() }
            }
        }    }
    private fun handleRoomClick(roomNo: String) {
        // We can check the background color to see its status.
        // In Android, extracting colors directly from the background drawable can be tricky,
        // so checking the button state based on our app flow is easier.
        // Wait, for a rookie approach, we can just check the color we set earlier!
        // A safer way is to maintain a list of booked rooms, but for simplicity we'll just intent it.

        val btn = roomButtons[roomNo]
        // Color filtering logic for rookie lab project:
        // If it's still Gray (user hasn't clicked view rooms), prompt them.
        if (checkInDate.isEmpty() || checkOutDate.isEmpty()) {
            Toast.makeText(this, "Select dates and view rooms first!", Toast.LENGTH_SHORT).show()
            return
        }

        // We check if it is available by reading its current color.
        // To avoid complex ColorDrawable casting, we'll just check if it's the exact RED color we set.
        // Note: A cleaner way is to store availability in a variable, but let's stick to the visual check you asked for.
        val currentBgColor = (btn?.background as? android.graphics.drawable.ColorDrawable)?.color
        if (currentBgColor == Color.parseColor("#FF4C4C")) {
            Toast.makeText(this, "Room $roomNo is not available", Toast.LENGTH_SHORT).show()
        } else {
            // Room is Green! Let's intent to the next activity.

            val intent = Intent(this, FinalBookingActivity::class.java).apply {
                putExtra("ROOM_NO", roomNo)
                putExtra("CHECK_IN", checkInDate)
                putExtra("CHECK_OUT", checkOutDate)
            }
            startActivity(intent)

            Toast.makeText(this, "Proceeding to book $roomNo...", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function for a quick DatePicker
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Formats to YYYY-MM-DD
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            val formattedDay = String.format("%02d", selectedDay)
            onDateSelected("$selectedYear-$formattedMonth-$formattedDay")
        }, year, month, day).show()
    }
}
package com.example.hotelmanagement.submenu

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelmanagement.NetworkManager
import com.example.hotelmanagement.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        fetchAndRenderData()
    }

    private fun fetchAndRenderData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call the new endpoints
                val occupancyRes = NetworkManager.api.getOccupancy()
                val salesRes = NetworkManager.api.getSales()

                withContext(Dispatchers.Main) {
                    if (occupancyRes.isSuccessful && salesRes.isSuccessful) {

                        // 1. Setup Pie Chart
                        val occData = occupancyRes.body()?.data
                        val occupiedCount = occData?.occupied_count ?: 0
                        val totalRooms = occData?.total_rooms ?: 16
                        val emptyRooms = totalRooms - occupiedCount

                        setupPieChart(occupiedCount, emptyRooms)
                        findViewById<TextView>(R.id.tvOccupancyNumbers).text =
                            "Occupied: $occupiedCount | Available: $emptyRooms\nTotal Capacity: $totalRooms Rooms"

                        // 2. Setup Bar Chart
                        val salesDataList = salesRes.body()?.data ?: emptyList()
                        setupBarChart(salesDataList)

                        val totalBookings = salesDataList.sumOf { it["total_bookings"]?.toDoubleOrNull()?.toInt() ?: 0 }
                        findViewById<TextView>(R.id.tvSalesNumbers).text = "Total Bookings Shown: $totalBookings"

                    } else {
                        Toast.makeText(this@DashboardActivity, "API Error", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPieChart(occupied: Int, empty: Int) {
        val pieChart = findViewById<PieChart>(R.id.pieChartOccupancy)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(occupied.toFloat(), "Occupied"))
        entries.add(PieEntry(empty.toFloat(), "Empty"))

        val dataSet = PieDataSet(entries, "")
        // Gold for Occupied, Dark Gray for Empty
        dataSet.colors = listOf(Color.parseColor("#D4AF37"), Color.parseColor("#333333"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.legend.textColor = Color.WHITE
        pieChart.setUsePercentValues(true)
        pieChart.setHoleColor(Color.parseColor("#1A1A1A")) // Matches your premium card bg
        pieChart.centerText = "Status"
        pieChart.setCenterTextColor(Color.parseColor("#D4AF37"))

        pieChart.animateY(1400) // Cool spin-up animation
        pieChart.invalidate()
    }

    private fun setupBarChart(salesDataList: List<Map<String, String>>) {
        val barChart = findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.barChart)

        // 1. Generate the strings for the last 3 months (e.g., "2026-02", "2026-03", "2026-04")
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val last3Months = mutableListOf<String>()

        for (i in 2 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            last3Months.add(format.format(cal.time))
        }

        // 2. Convert the API list into a Map so it's easy to look up booking counts
        val dataMap = salesDataList.associate {
            it["month"] as String to (it["total_bookings"]?.toFloatOrNull() ?: 0f)
        }

        // 3. Match the API data to our 3 months (put a 0 if the month is empty)
        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        val labels = ArrayList<String>()

        for ((index, monthStr) in last3Months.withIndex()) {
            val count = dataMap[monthStr] ?: 0f // Default to 0 if no bookings exist
            entries.add(com.github.mikephil.charting.data.BarEntry(index.toFloat(), count))
            labels.add(monthStr)
        }

        // 4. Create the visual dataset
        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Monthly Bookings")
        dataSet.color = android.graphics.Color.parseColor("#D4AF37") // Gold bars
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 12f

        val barData = com.github.mikephil.charting.data.BarData(dataSet)
        barChart.data = barData

        // --- 5. THE CRITICAL X-AXIS FIX ---
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = android.graphics.Color.WHITE

        // This physically stops the chart from creating duplicate half-step labels
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)

        // Final UI Polish
        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.axisLeft.axisMinimum = 0f // Ensure the chart always starts at 0
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.textColor = android.graphics.Color.WHITE

        // Refresh the chart to show the new data
        barChart.invalidate()
    }}
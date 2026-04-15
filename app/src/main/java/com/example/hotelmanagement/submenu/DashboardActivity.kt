package com.example.hotelmanagement.submenu

import android.graphics.Color
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
        val occupancyQuery = SqlQueries.getCurrentOccupancyQuery(today)
        val salesQuery = SqlQueries.getMonthlySalesQuery()

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

    private fun setupBarChart(salesData: List<Map<String, String>>) {
        val barChart = findViewById<BarChart>(R.id.barChartSales)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        salesData.forEachIndexed { index, map ->
            val month = map["month"] ?: "Unknown"
            val count = map["total_bookings"]?.toFloatOrNull() ?: 0f

            entries.add(BarEntry(index.toFloat(), count))
            labels.add(month)
        }

        val dataSet = BarDataSet(entries, "Bookings per Month")
        dataSet.color = Color.parseColor("#D4AF37") // Gold bars
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false
        barChart.legend.textColor = Color.WHITE

        // Setup X Axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setDrawGridLines(false) // Keeps it clean

        // Setup Y Axis
        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisRight.isEnabled = false // Hide right numbers

        barChart.animateY(1000) // Bar growth animation
        barChart.invalidate()
    }
}
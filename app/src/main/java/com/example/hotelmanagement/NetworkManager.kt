package com.example.hotelmanagement

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- 1. Request Data Models ---
data class AvailabilityRequest(val check_in: String, val check_out: String)
data class BookingRequest(val room_no: String, val guest_name: String, val guest_phone: String, val check_in: String, val check_out: String)
data class GuestHistoryRequest(val guest_name: String, val guest_phone: String)
data class FoodOrderRequest(val room_no: String, val item_name: String, val price: Double)
data class CleaningRequest(val room_no: String)

// --- 2. Response Data Models ---
data class SimpleResponse(val status: String, val message: String?)
data class BookingResponse(val status: String, val message: String?, val booking_id: Int?)
data class ListDataResponse(val status: String, val data: List<Map<String, String>>)
data class MapDataResponse(val status: String, val data: Map<String, String>?)
data class OccupancyData(val occupied_count: Int, val total_rooms: Int)
data class OccupancyResponse(val status: String, val data: OccupancyData)

// --- 3. The REST API Interface ---
interface ApiService {
    @GET("/api/dashboard/occupancy")
    suspend fun getOccupancy(): Response<OccupancyResponse>

    @GET("/api/dashboard/sales")
    suspend fun getSales(): Response<ListDataResponse>

    @POST("/api/rooms/availability")
    suspend fun checkAvailability(@Body request: AvailabilityRequest): Response<ListDataResponse>

    @POST("/api/bookings/create")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>

    @GET("/api/staff/{staff_id}")
    suspend fun getStaff(@Path("staff_id") staffId: String): Response<MapDataResponse>

    @POST("/api/guests/history")
    suspend fun getGuestHistory(@Body request: GuestHistoryRequest): Response<ListDataResponse>

    @POST("/api/services/food")
    suspend fun orderFood(@Body request: FoodOrderRequest): Response<SimpleResponse>

    @POST("/api/services/cleaning")
    suspend fun requestCleaning(@Body request: CleaningRequest): Response<SimpleResponse>
}

// --- 4. Retrofit Builder ---
object NetworkManager {
    // This tells the phone to talk to the USB cable
    private const val BASE_URL = "http://10.174.15.253:8080/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
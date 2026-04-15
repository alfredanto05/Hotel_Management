# Hotel Management Android App

This is a simple Android app for basic hotel operations. It helps staff view hotel status, check room availability, create bookings, search guest and staff records, and place room service or cleaning requests.

## Features

- Splash screen and main dashboard menu
- Room availability check by check-in and check-out dates
- Final booking confirmation with guest details
- Guest booking history search
- Staff profile search by staff ID
- Room service food ordering
- Housekeeping / cleaning request
- Dashboard charts for occupancy and booking trends

## Tech Stack

- Kotlin
- Android SDK
- Retrofit + Gson
- Kotlin Coroutines
- Glide
- MPAndroidChart

## Project Flow

1. `SplashActivity` opens first.
2. The user moves to `MainActivity`.
3. From the main menu, the user can open:
   - Dashboard
   - Room Booking
   - Guest Enquiry
   - Staff Enquiry
   - Room Service

## API Overview

The app uses Retrofit in [`NetworkManager.kt`](/Users/alf05/AndroidStudioProjects/HotelManagement/app/src/main/java/com/example/hotelmanagement/NetworkManager.kt) to call a backend server.

- Base URL: `http://10.174.15.253:8080/`
- Cleartext HTTP is enabled in the manifest
- Requests are made in Kotlin coroutines on `Dispatchers.IO`
- UI is updated back on the main thread using `withContext(Dispatchers.Main)`

### Endpoints Used

- `GET /api/dashboard/occupancy`
  Returns room occupancy summary for the dashboard pie chart.

- `GET /api/dashboard/sales`
  Returns booking totals used in the dashboard bar chart.

- `POST /api/rooms/availability`
  Takes `check_in` and `check_out` and returns booked rooms for those dates.

- `POST /api/bookings/create`
  Takes room number, guest name, phone, and dates, then creates a booking.

- `GET /api/staff/{staff_id}`
  Returns staff details for the entered staff ID.

- `POST /api/guests/history`
  Takes guest name and phone number and returns previous stays.

- `POST /api/services/food`
  Sends a food order with room number, item name, and price.

- `POST /api/services/cleaning`
  Sends a housekeeping request for a room.

## How The API Works

The flow is simple:

1. A screen collects user input.
2. The activity creates a request model such as `AvailabilityRequest`, `BookingRequest`, or `FoodOrderRequest`.
3. That request is sent through `NetworkManager.api`.
4. Retrofit converts Kotlin objects to JSON and sends them to the backend.
5. The backend returns JSON.
6. Gson converts the JSON response back into Kotlin data classes.
7. The activity shows the result using text updates, charts, or toast messages.

## Setup

1. Open the project in Android Studio.
2. Make sure the backend server is running and reachable at the IP in `NetworkManager.kt`.
3. Connect a device or start an emulator.
4. Build and run the app.

## Notes

- The app depends on the backend being available on the same network / reachable IP.
- If the backend IP changes, update the `BASE_URL` in [`NetworkManager.kt`](/Users/alf05/AndroidStudioProjects/HotelManagement/app/src/main/java/com/example/hotelmanagement/NetworkManager.kt).
- Error handling is basic and mostly shown through toast messages.

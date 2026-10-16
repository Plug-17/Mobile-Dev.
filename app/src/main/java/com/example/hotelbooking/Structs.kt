package com.example.hotelbooking

data class User(
    val uid: String,
    val email: String
)
data class Hotel(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val imageUrl: String = ""
)
data class Room(
    val roomId: String = "",
    val hotelId: String = "",
    val roomType: String = "",
    val price: Double = 0.0,
    val capacity: Int = 2,
)
data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val hotelName: String = "",
    val checkInDate: String = "",
    val nights: Int = 1
)
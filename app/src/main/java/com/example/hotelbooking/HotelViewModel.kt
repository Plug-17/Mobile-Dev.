package com.example.hotelbooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HotelViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _bookingStatus = MutableStateFlow<Boolean?>(null)
    val bookingStatus: StateFlow<Boolean?> = _bookingStatus.asStateFlow()

    init {
        fetchHotels()
    }

    fun fetchHotels() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("hotels").get().await()
                val hotelList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Hotel::class.java)?.copy(id = doc.id)
                }
                _hotels.value = hotelList
            } catch (e: Exception) {
                _hotels.value = emptyList()
            }
        }
    }

    fun bookHotel(hotelName: String, checkInDate: String, nights: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val bookingRef = firestore.collection("bookings").document()
                val booking = Booking(
                    bookingId = bookingRef.id,
                    userId = userId,
                    hotelName = hotelName,
                    checkInDate = checkInDate,
                    nights = nights
                )

                bookingRef.set(booking).await()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun resetBookingStatus() {
        _bookingStatus.value = null
    }


    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    fun fetchUserBookings() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val snapshot = firestore.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val bookingList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)
                }
                _userBookings.value = bookingList
            } catch (e: Exception) {
                _userBookings.value = emptyList()
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("bookings").document(bookingId).delete().await()

                fetchUserBookings()
            } catch (e: Exception) {

            }
        }
    }

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()
    var rooms_exception: Exception? = null

    fun fetchRoomsForHotel(hotelId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("rooms")
                    .whereEqualTo("hotelId", hotelId)
                    .get()
                    .await()

                val roomList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Room::class.java)?.copy(roomId = doc.id)
                }
                _rooms.value = roomList
                rooms_exception = null
            } catch (e: Exception) {
                _rooms.value = emptyList()
                rooms_exception = e
            }
        }
    }
}
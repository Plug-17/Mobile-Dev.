
package com.example.hotelbooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HotelViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ==================== HOTELS ====================

    private val _hotels =
        MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> =
        _hotels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()

    private var hotelsListener: ListenerRegistration? = null

    // ดึงข้อมูลโรงแรมจาก Firebase หนึ่งครั้ง
    fun fetchHotels() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val snapshot = firestore
                    .collection("hotels")
                    .get()
                    .await()

                _hotels.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Hotel::class.java)?.copy(
                        id = doc.id
                    )
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                    ?: "Failed to load hotels"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ฟังข้อมูลโรงแรมแบบเรียลไทม์
    fun listenHotels() {
        if (hotelsListener != null) return

        _isLoading.value = true
        _error.value = null

        hotelsListener = firestore
            .collection("hotels")
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    _error.value = exception.localizedMessage
                        ?: "Failed to load hotels"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _hotels.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Hotel::class.java)?.copy(
                            id = doc.id
                        )
                    }

                    _error.value = null
                    _isLoading.value = false
                }
            }
    }

    // ==================== BOOKING ====================

    private val _bookingStatus =
        MutableStateFlow<Boolean?>(null)
    val bookingStatus: StateFlow<Boolean?> =
        _bookingStatus.asStateFlow()

    fun bookHotel(
        hotelName: String,
        checkInDate: String,
        nights: Int,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid

            if (userId == null) {
                _bookingStatus.value = false
                onComplete(false)
                return@launch
            }

            try {
                val bookingRef = firestore
                    .collection("bookings")
                    .document()

                val booking = Booking(
                    bookingId = bookingRef.id,
                    userId = userId,
                    hotelName = hotelName,
                    checkInDate = checkInDate,
                    nights = nights
                )

                bookingRef.set(booking).await()

                _bookingStatus.value = true
                onComplete(true)

                // โหลดประวัติใหม่หลังจองสำเร็จ
                fetchUserBookings()

            } catch (e: Exception) {
                _bookingStatus.value = false
                onComplete(false)
            }
        }
    }

    fun resetBookingStatus() {
        _bookingStatus.value = null
    }

    // ==================== BOOKING HISTORY ====================

    private val _userBookings =
        MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> =
        _userBookings.asStateFlow()

    private val _bookingHistoryError =
        MutableStateFlow<String?>(null)
    val bookingHistoryError: StateFlow<String?> =
        _bookingHistoryError.asStateFlow()

    fun fetchUserBookings() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid

            if (userId == null) {
                _userBookings.value = emptyList()
                _bookingHistoryError.value =
                    "Please sign in to view your bookings"
                return@launch
            }

            try {
                _bookingHistoryError.value = null

                val snapshot = firestore
                    .collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                _userBookings.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(
                        bookingId = doc.id
                    )
                }

            } catch (e: Exception) {
                _bookingHistoryError.value =
                    e.localizedMessage ?: "Failed to load bookings"
            }
        }
    }

    // ยกเลิกการจอง
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                    ?: return@launch

                val bookingRef = firestore
                    .collection("bookings")
                    .document(bookingId)

                val snapshot = bookingRef.get().await()

                // ลบได้เฉพาะรายการที่เป็นของผู้ใช้คนปัจจุบัน
                if (snapshot.getString("userId") != userId) {
                    return@launch
                }

                bookingRef.delete().await()

                // เอารายการที่ยกเลิกออกจากหน้าจอทันที
                _userBookings.value = _userBookings.value.filter {
                    it.bookingId != bookingId
                }

                fetchUserBookings()

            } catch (e: Exception) {
                _bookingHistoryError.value =
                    e.localizedMessage ?: "Failed to cancel booking"
            }
        }
    }

    // ==================== ROOMS ====================

    private val _rooms =
        MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> =
        _rooms.asStateFlow()

    var rooms_exception: Exception? = null
        private set

    fun fetchRoomsForHotel(hotelId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore
                    .collection("rooms")
                    .whereEqualTo("hotelId", hotelId)
                    .get()
                    .await()

                _rooms.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Room::class.java)?.copy(
                        roomId = doc.id
                    )
                }

                rooms_exception = null

            } catch (e: Exception) {
                _rooms.value = emptyList()
                rooms_exception = e
            }
        }
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        hotelsListener?.remove()
        hotelsListener = null
        super.onCleared()
    }
}

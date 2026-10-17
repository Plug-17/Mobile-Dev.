package com.example.hotelbooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BookingHistoryScreen(modifier: Modifier = Modifier, hotelVM: HotelViewModel = viewModel()) {
    val bookings by hotelVM.userBookings.collectAsState()

    var bookingToDelete by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        hotelVM.fetchUserBookings()
    }

    if (bookingToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = { Text(text = "Cancel Booking") },
            text = { Text(text = "Are you sure you want to cancel your stay at ${bookingToDelete?.hotelName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        bookingToDelete?.let { hotelVM.cancelBooking(it.bookingId) }
                        bookingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToDelete = null }) {
                    Text("No")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "My Booking History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (bookings.isEmpty()) {
            item {
                Text(
                    text = "You have no bookings yet.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        } else {
            items(bookings) { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = booking.hotelName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Check-in Date: ${booking.checkInDate}", fontSize = 14.sp, color = Color.Gray)
                        Text(text = "Duration: ${booking.nights} night(s)", fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { bookingToDelete = booking },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444))
                            ) {
                                Text(text = "Cancel Booking")
                            }
                        }
                    }
                }
            }
        }
    }
}
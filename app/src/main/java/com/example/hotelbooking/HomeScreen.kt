package com.example.hotelbooking

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(modifier: Modifier = Modifier, hotelVM: HotelViewModel = viewModel()) {
    val hotels by hotelVM.hotels.collectAsState()
    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var nights by remember { mutableStateOf(1) }
    val context = LocalContext.current

    if (selectedHotel != null) {
        LaunchedEffect(selectedHotel) {
            selectedHotel?.let { hotelVM.fetchRoomsForHotel(it.id) }
        }
        val rooms by hotelVM.rooms.collectAsState()

        var selectedRoom by remember { mutableStateOf<Room?>(null) }
        var nights by remember { mutableStateOf(1) }



        val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
        val datePickerState = rememberDatePickerState()
        val selectedDateText = datePickerState.selectedDateMillis?.let {
            dateFormatter.format(Date(it))
        } ?: dateFormatter.format(Date())

        var showDatePicker by remember { mutableStateOf(false) }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }




        AlertDialog(
            onDismissRequest = { selectedHotel = null },
            title = { Text(text = "Book: ${selectedHotel?.name}") },
            text = {
                Column {
                    if (rooms.isEmpty()) {
                        if (hotelVM.rooms_exception != null) {
                            Text(text = "Error: " + hotelVM.rooms_exception!!.message, color = Color.Red)
                        } else {
                            Text(text = "Loading rooms...", color = Color.Gray)
                        }
                    } else {
                        Text(text = "Select a Room Type:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                        LazyColumn(modifier = Modifier.height(150.dp)) {
                            items(rooms) { room ->
                                val isSelected = selectedRoom?.roomId == room.roomId
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                                    ),
                                    onClick = { selectedRoom = room }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = room.roomType, fontWeight = FontWeight.Bold)
                                        Text(text = "Price: $${room.price} / night", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Check-in Date:")
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text(text = selectedDateText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nights:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { if (nights > 1) nights-- }) { Text("-") }
                            Text(text = "$nights", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                            Button(onClick = { nights++ }) { Text("+") }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val pricePerNight = selectedRoom?.price ?: 0.0
                    Text(
                        text = "Total: ฿${pricePerNight * nights}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hotel = selectedHotel ?: return@Button
                        val room = selectedRoom ?: return@Button

                        hotelVM.bookHotel(
                            hotelName = "${hotel.name} (${room.roomType})",
                            checkInDate = selectedDateText,
                            nights = nights
                        ) { success ->
                            selectedHotel = null
                        }
                    },
                    enabled = selectedRoom != null
                ) {
                    Text("Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHotel = null }) {
                    Text("Cancel")
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
                text = "Available Hotels",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(hotels) { hotel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { selectedHotel = hotel },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (hotel.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = hotel.imageUrl,
                            contentDescription = "Hotel Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            onError = { state ->
                                android.util.Log.e(
                                    "CoilDebug",
                                    "Failed to load image",
                                    state.result.throwable
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(text = hotel.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = hotel.location, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AsyncImage(
    model: String,
    contentDescription: String,
    modifier: Modifier,
    contentScale: ContentScale,
    onError: (ERROR) -> ERROR
) {
    TODO("Not yet implemented")
}


annotation class ERROR

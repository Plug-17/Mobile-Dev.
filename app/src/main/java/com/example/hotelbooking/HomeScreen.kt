package com.example.hotelbooking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hotelVM: HotelViewModel = viewModel()
) {
    val hotels by hotelVM.hotels.collectAsState()
    val isLoading by hotelVM.isLoading.collectAsState()
    val error by hotelVM.error.collectAsState()
    val rooms by hotelVM.rooms.collectAsState()

    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var checkInDate by remember { mutableStateOf("") }
    var nightsText by remember { mutableStateOf("1") }
    var bookingMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isBooking by remember { mutableStateOf(false) }

    LaunchedEffect(hotelVM) {
        hotelVM.listenHotels()
    }

    LaunchedEffect(selectedHotel?.id) {
        selectedHotel?.let { hotel ->
            selectedRoom = null
            checkInDate = ""
            nightsText = "1"
            bookingMessage = ""
            hotelVM.fetchRoomsForHotel(hotel.id)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Available Hotels",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading && hotels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (error != null) {
            item {
                Text(
                    text = "Failed to load hotels: $error",
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(onClick = { hotelVM.fetchHotels() }) {
                    Text("Try Again")
                }
            }
        }

        if (!isLoading && error == null && hotels.isEmpty()) {
            item {
                Text("No hotels found. Please add hotels to Firebase.")
            }
        }

        items(
            items = hotels,
            key = { hotel -> hotel.id }
        ) { hotel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedHotel = hotel
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AsyncImage(
                        model = hotel.imageUrl,
                        contentDescription = hotel.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = hotel.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = hotel.location,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Tap to book",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    selectedHotel?.let { hotel ->
        AlertDialog(
            onDismissRequest = {
                if (!isBooking) selectedHotel = null
            },
            title = {
                Text("Book ${hotel.name}")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = hotel.imageUrl,
                        contentDescription = hotel.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text("Location: ${hotel.location}")

                    HorizontalDivider()

                    Text(
                        text = "Select Room",
                        fontWeight = FontWeight.Bold
                    )

                    if (rooms.isEmpty()) {
                        Text("No rooms available for this hotel.")
                    } else {
                        rooms.forEach { room ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRoom = room
                                        bookingMessage = ""
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRoom?.roomId == room.roomId,
                                    onClick = {
                                        selectedRoom = room
                                        bookingMessage = ""
                                    }
                                )

                                Column {
                                    Text(
                                        text = room.roomType,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "฿${room.price} / night · Capacity ${room.capacity}",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "Check-in Date",
                        fontWeight = FontWeight.Bold
                    )

                    // แก้ไขจุดที่ 1: ห่อ OutlinedTextField ด้วย Box เพื่อให้กดได้ทั้งพื้นที่
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = checkInDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false, // ปิดการพิมพ์เพื่อรับ event จาก Box ชัดเจน
                            placeholder = {
                                Text("Select check-in date")
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.bookmark),
                                    contentDescription = "Select date"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        // Overlay สำหรับดัก Event การคลิกทั้งช่อง
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    Text(
                        text = "Number of Nights",
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nightsText,
                        onValueChange = {
                            nightsText = it.filter(Char::isDigit)
                            bookingMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nights") }
                    )

                    val nights = nightsText.toIntOrNull() ?: 0

                    if (selectedRoom != null && nights > 0) {
                        Text(
                            text = "Total: ฿${selectedRoom!!.price * nights}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    if (bookingMessage.isNotBlank()) {
                        Text(
                            text = bookingMessage,
                            color = if (bookingMessage == "Booking successful!") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedRoom != null &&
                            checkInDate.isNotBlank() &&
                            (nightsText.toIntOrNull() ?: 0) > 0 &&
                            !isBooking,
                    onClick = {
                        val room = selectedRoom
                        val nights = nightsText.toIntOrNull() ?: 0

                        if (room != null &&
                            checkInDate.isNotBlank() &&
                            nights > 0
                        ) {
                            isBooking = true
                            bookingMessage = ""

                            hotelVM.bookHotel(
                                hotelName = "${hotel.name} (${room.roomType})",
                                checkInDate = checkInDate,
                                nights = nights
                            ) { success ->
                                isBooking = false

                                bookingMessage = if (success) {
                                    "Booking successful!"
                                } else {
                                    "Booking failed. Please sign in and try again."
                                }
                            }
                        }
                    }
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm Booking")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isBooking,
                    onClick = { selectedHotel = null }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // แก้ไขจุดที่ 2 & 3: จัดการ DatePickerState ให้อัปเดตได้ถูกต้อง
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.US
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }

                            checkInDate = formatter.format(Date(millis))
                            bookingMessage = ""
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}





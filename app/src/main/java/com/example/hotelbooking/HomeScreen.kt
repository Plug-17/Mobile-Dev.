
package com.example.hotelbooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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

    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var checkInDate by remember { mutableStateOf("") }
    var nightsText by remember { mutableStateOf("1") }
    var bookingMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isBooking by remember { mutableStateOf(false) }

    LaunchedEffect(hotelVM) {
        hotelVM.listenHotels()
    }

    LaunchedEffect(selectedHotel?.id) {
        if (selectedHotel != null) {
            checkInDate = ""
            nightsText = "1"
            bookingMessage = ""
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
                Column {
                    Text(
                        text = "Failed to load hotels: $error",
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = { hotelVM.fetchHotels() }
                    ) {
                        Text("Try Again")
                    }
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
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
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
                        text = "Look Details",
                        color = Color.Blue
                    )
                }
            }
        }
    }

    selectedHotel?.let { hotel ->
        AlertDialog(
            onDismissRequest = {
                if (!isBooking) {
                    selectedHotel = null
                }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

                    // 1. Check-in date
                    Text(
                        text = "Check-in Date",
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text("Select check-in date")
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    showDatePicker = true
                                }
                            ) {
                                Icon(painterResource(R.drawable.calendar__2_),
                                    contentDescription = "date", modifier = Modifier.size(24.dp))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker = true
                            },
                        singleLine = true
                    )

                    // 2. Number of nights
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
                        label = {
                            Text("Nights")
                        }
                    )

                    if (bookingMessage.isNotBlank()) {
                        Text(
                            text = bookingMessage,
                            color = if (
                                bookingMessage == "Booking successful!"
                            ) {
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
                    enabled = checkInDate.isNotBlank() &&
                            (nightsText.toIntOrNull() ?: 0) > 0 &&
                            !isBooking,
                    onClick = {
                        val nights = nightsText.toIntOrNull() ?: 0

                        if (checkInDate.isNotBlank() && nights > 0) {
                            isBooking = true
                            bookingMessage = ""

                            hotelVM.bookHotel(
                                hotelName = hotel.name,
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
                    onClick = {
                        selectedHotel = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Date Picker
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
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

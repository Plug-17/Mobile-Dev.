
package com.example.hotelbooking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hotelVM: HotelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // ข้อมูลตัวอย่าง ไม่ต้องดึงจาก Firebase
    val hotels = remember {
        listOf(
            Hotel(
                id = "1",
                name = "Pak Pi-Run",
                location = "Sri Racha, Thailand",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQDfFyBNWvrAYJnpwDtP-YTxIMFls7xiQv_a3kRL3_AZI403IGODrDiSsw&s=10"
            ),
            Hotel(
                id = "2",
                name = "S.Sriracha Hotel",
                location = "Sri Racha, Thailand",
                imageUrl = "https://ssriracha.com/wp-content/uploads/2023/07/image-418.webp"
            ),
            Hotel(
                id = "3",
                name = "Shane Hotel",
                location = "Chiang Mai, Thailand",
                imageUrl = "https://cf.bstatic.com/xdata/images/hotel/max1024x768/590837889.jpg?k=9279469355ebc1468099a7ff956aae261634a9be65fd0a57e167a1078149ca88&o="
            )
        )
    }

    var selectedHotel by remember {
        mutableStateOf<Hotel?>(null)
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(hotels) { hotel ->
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
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "View Details",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    selectedHotel?.let { hotel ->
        AlertDialog(
            onDismissRequest = { selectedHotel = null },
            title = {
                Text(hotel.name)
            },
            text = {
                Column {
                    AsyncImage(
                        model = hotel.imageUrl,
                        contentDescription = hotel.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Location: ${hotel.location}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedHotel = null }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

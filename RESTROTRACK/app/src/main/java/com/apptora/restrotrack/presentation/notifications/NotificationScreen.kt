package com.apptora.restrotrack.presentation.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.apptora.restrotrack.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {

                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        "Mark all as read",
                        color = Color(0xFFCE7B3C),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items(notificationsList) { notification ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (notification.isRead) Color(0xFFF9F9F9) else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notification icon
                        if (notification.iconUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(notification.iconUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        } else if (notification.localIconResId != null) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = notification.localIconResId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCE7B3C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    notification.initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(notification.title, fontWeight = FontWeight.Bold)
                            Text(notification.message, fontSize = 12.sp, color = Color.Gray)
                            Text(notification.timeAgo, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

data class Notification(
    val title: String,
    val message: String,
    val timeAgo: String,
    val iconUrl: String? = null,
    val initials: String = "",
    val isRead: Boolean = false,
    val localIconResId: Int? = null
)

val notificationsList = listOf(
    Notification(
        "New Order Alert!",
        "Order #1025 has been placed by John Doe.",
        "20 minutes ago",
        localIconResId = R.drawable.logo_main
    ),
    Notification(
        "Low Stock Warning!",
        "Fresh Milk (5L) will expire in 2 days!",
        "30 minutes ago",
        initials = "RT"
    ),
    Notification(
        "Expiry Alert!",
        "Fresh Milk (5L) will expire in 2 days!",
        "1 hour ago",
        initials = "RT"
    ),
    Notification(
        "Order Cancellation Request!",
        "Customer Nethmi Jayawardena has requested to cancel Order #4120.",
        "2 hours ago",
        isRead = true,
        localIconResId = R.drawable.logo_main
    )
)
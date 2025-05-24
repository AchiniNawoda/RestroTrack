package com.apptora.restrotrack.presentation.dashoboard


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.apptora.restrotrack.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import com.apptora.restrotrack.presentation.auth.SimplePieChart
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAnalyticsScreen(drawerState: DrawerState, navController: NavController) {
    val scope = rememberCoroutineScope()

    val db = FirebaseFirestore.getInstance()
    var totalItems by remember { mutableStateOf(0) }
    var itemsRunningLow by remember { mutableStateOf(0) }
    var itemsExpiringSoon by remember { mutableStateOf(0) }
    var pendingOrders by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("inventory").get().await()
            totalItems = snapshot.size()
            val currentTime = System.currentTimeMillis()
            val thirtyDaysFromNow = currentTime + (30L * 24 * 60 * 60 * 1000)

            itemsRunningLow = snapshot.documents.count { (it.getLong("quantity") ?: 0L) <= 30 }
            itemsExpiringSoon = snapshot.documents.count {
                val expireDateStr = it.getString("expireDate") ?: ""
                if (expireDateStr.isNotEmpty()) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    try {
                        val expireDate = sdf.parse(expireDateStr)?.time ?: 0L
                        expireDate in currentTime..thirtyDaysFromNow
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    false
                }
            }

            val ordersSnapshot =
                db.collection("orders").whereEqualTo("status", "pending").get().await()
            pendingOrders = ordersSnapshot.size()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // force white over full screen
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.height(28.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("notifications")
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD98840))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                DashboardCardGrid(totalItems, itemsRunningLow, itemsExpiringSoon, pendingOrders)

                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    SalesOverviewChart()
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Inventory Usage",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    InventoryUsageSection()
                }
            }
        }
    )
}

@Composable
fun DashboardCardGrid(
    totalItems: Int,
    itemsRunningLow: Int,
    itemsExpiringSoon: Int,
    pendingOrders: Int
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(totalItems.toString(), "Total No. of Items", R.drawable.ic_lock)
            DashboardCard(
                itemsRunningLow.toString(),
                "Items Running Low",
                R.drawable.ic_favorite,
                Color.Red
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                itemsExpiringSoon.toString(),
                "Units Expiring Soon",
                R.drawable.ic_expiring
            )
            DashboardCard(
                pendingOrders.toString(),
                "Orders Pending",
                R.drawable.ic_pending,
                Color.Green
            )
        }
    }
}

@Composable
fun DashboardCard(value: String, label: String, iconResId: Int, iconColor: Color = Color.Black) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp), // size to match icon scale
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SalesOverviewChart() {
    val entries = listOf(25000f, 27000f, 39000f, 38000f, 31000f, 45000f, 50000f)
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val maxValue = 50000f
    var selectedRange by remember { mutableStateOf("Last 7 days") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Text(
            "Sales Overview",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Transaction Amount", style = MaterialTheme.typography.bodySmall)
                Text("Rs.", style = MaterialTheme.typography.bodySmall)
            }

            Box {
                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedRange)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Last 7 days") }, onClick = {
                        selectedRange = "Last 7 days"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("Last 30 days") }, onClick = {
                        selectedRange = "Last 30 days"
                        expanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / (entries.size * 2)
            val space = barWidth / 2
            val yStep = maxValue / 5

            // Y-Axis labels
            for (i in 0..5) {
                val y = size.height - (i * (size.height / 5))
                drawContext.canvas.nativeCanvas.drawText(
                    (i * yStep).toInt().toString(),
                    0f,
                    y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            // Bars
            entries.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * size.height
                drawRoundRect(
                    color = Color(0xFFCE843E),
                    topLeft = Offset(
                        x = index * (barWidth + space) + 60.dp.toPx(),
                        y = size.height - barHeight
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }

            // X-Axis labels
            days.forEachIndexed { index, day ->
                drawContext.canvas.nativeCanvas.drawText(
                    day,
                    index * (barWidth + space) + 60.dp.toPx() + barWidth / 2,
                    size.height + 30f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryUsageSection() {
    var selectedTab by remember { mutableStateOf(1) } // Items tab
    val tabs = listOf("Overview", "Items")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimplePieChart(
                    usedPercent = 54.6f,
                    modifier = Modifier
                        .size(180.dp) // Slightly smaller to match image proportions
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Used: 54.6%, Free: 45.4%",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
        } else {
            Column(modifier = Modifier.padding(8.dp)) {
                listOf(
                    "Rice - Samba" to 91.5f,
                    "Chicken" to 47.5f,
                    "Beans" to 90.5f,
                    "Potato" to 10.5f,
                    "Dhal" to 10.5f,
                    "Tomato" to 90.5f,
                    "Sausages" to 90.5f,
                    "Pork" to 90.5f,
                    "Green Chillie" to 90.5f
                ).forEach { (item, percentage) ->
                    Text("$item - ${"%.1f".format(percentage)}%")
                    LinearProgressIndicator(
                        progress = percentage / 100,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = when {
                            percentage > 80 -> Color(0xFF4CAF50)
                            percentage > 40 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}
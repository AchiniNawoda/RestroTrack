package com.apptora.restrotrack.presentation.orders

import com.google.firebase.firestore.FirebaseFirestore


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptora.restrotrack.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FilterList

import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign

import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CheckboxDefaults

data class Order(
    val orderId: String = "",
    val customer: String = "",
    val date: String = "",
    val pax: Int = 0,
    val amount: Double = 0.0,
    val status: String = "pending", // could be "pending" or "reserved"
    val tableNo: String? = null, // NEW: to store Table Number
    val reservedBy: String? = null // NEW: to store Reserved Employee ID
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf(listOf<Order>()) }
    var selectedTab by remember { mutableStateOf("Pending") }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var sortByNewest by remember { mutableStateOf(true) }
    var sortByOldest by remember { mutableStateOf(false) }
    var sortByPaxAsc by remember { mutableStateOf(false) }
    var sortByPaxDesc by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("orders").get().await()
            orders = snapshot.documents.mapNotNull { it.toObject<Order>() }
        } catch (e: Exception) {
            // Handle error appropriately
        }
    }

    Scaffold(
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD98840))
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pending", "Reserved").forEach { tab ->
                    Text(
                        text = tab,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = tab }
                            .background(
                                if (selectedTab == tab) Color(0xFFCE7B3C) else Color.LightGray,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HeaderRowOrders()

            val filtered = orders.filter {
                it.status.equals(selectedTab, ignoreCase = true) &&
                        (it.customer.contains(searchQuery, ignoreCase = true) ||
                                it.orderId.contains(searchQuery, ignoreCase = true))
            }

            val sortedFiltered = filtered.sortedWith(compareBy<Order> {
                if (sortByNewest) -it.date.hashCode() else if (sortByOldest) it.date.hashCode() else 0
            }.thenBy {
                if (sortByPaxAsc) it.pax else if (sortByPaxDesc) -it.pax else 0
            })

            LazyColumn {
                itemsIndexed(sortedFiltered) { index, order ->
                    OrderRow(order, index, navController)
                }
            }

            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            if (showFilterDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterDialog = false },
                    sheetState = bottomSheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Sort by Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByNewest,
                                onCheckedChange = {
                                    sortByNewest = it
                                    if (it) sortByOldest = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Newest on top")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByOldest,
                                onCheckedChange = {
                                    sortByOldest = it
                                    if (it) sortByNewest = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Oldest on top")
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("Sort by No. of Pax", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByPaxAsc,
                                onCheckedChange = {
                                    sortByPaxAsc = it
                                    if (it) sortByPaxDesc = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ascend")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByPaxDesc,
                                onCheckedChange = {
                                    sortByPaxDesc = it
                                    if (it) sortByPaxAsc = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Descend")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showFilterDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderRowOrders() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = Color(0xFFCE7B3C),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Order ID",
            modifier = Modifier.weight(1.5f),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Customer",
            modifier = Modifier.weight(2f),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Date",
            modifier = Modifier.weight(1.5f),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Pax",
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Amount",
            modifier = Modifier.weight(1.5f),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OrderRow(order: Order, index: Int, navController: NavController) {
    Row(
        modifier = Modifier
            .clickable { navController.navigate("orderDetail/${order.orderId}") }
            .fillMaxWidth()
            .background(if (index % 2 == 0) Color.White else Color(0xFFF9F9F9))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            order.orderId,
            modifier = Modifier.weight(1.5f),
            fontSize = 13.sp,
            color = Color(0xFF0056A6),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline
        )
        Text(
            order.customer,
            modifier = Modifier.weight(2f),
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            order.date,
            modifier = Modifier.weight(1.5f),
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            order.pax.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            String.format("%.2f", order.amount),
            modifier = Modifier.weight(1.5f),
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}
package com.example.restrotrack.ui.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.apptora.restrotrack.R

import kotlinx.coroutines.launch

// Data class
data class InventoryItem(
    val name: String = "",
    val id: String = "",
    val expireDate: String = "",
    val quantity: Int = 0,
    val isPerishable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var inventoryItems by remember { mutableStateOf(listOf<InventoryItem>()) }
    var selectedTab by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var sortNewestFirst by remember { mutableStateOf(true) }
    var filterLowStock by remember { mutableStateOf(false) }
    var filterExpiryAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("inventory").get().await()
        inventoryItems = snapshot.documents.mapNotNull { it.toObject<InventoryItem>() }
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
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(Modifier.padding(vertical = 8.dp)) {
                listOf("All", "Perishable", "Non-Perishable").forEach { tab ->
                    Text(
                        text = tab,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { selectedTab = tab }
                            .background(
                                color = if (selectedTab == tab) Color(0xFFCCC5B9) else Color.LightGray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Black,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        navController.navigate("addItem")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add new item")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDD34 Low stock", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("\uD83D\uDD39 Expiry alerts", fontSize = 12.sp, color = Color(0xFFB491C8))
                }
            }

            HeaderRow()

            LazyColumn(Modifier.fillMaxSize()) {
                val filteredItems = inventoryItems
                    .filter {
                        when (selectedTab) {
                            "Perishable" -> it.isPerishable
                            "Non-Perishable" -> !it.isPerishable
                            else -> true
                        }
                    }
                    .filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                    .filter {
                        (!filterLowStock || it.quantity < 30) &&
                                (!filterExpiryAlert || it.expireDate.contains("2023"))
                    }
                    .sortedByDescending { if (sortNewestFirst) it.expireDate else "" }

                itemsIndexed(filteredItems) { index, item ->
                    InventoryItemRow(item, index, navController)
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterBottomSheetDialog(
            onDismiss = { showFilterDialog = false },
            sortNewestFirst = sortNewestFirst,
            onSortChange = { sortNewestFirst = it },
            filterLowStock = filterLowStock,
            onLowStockChange = { filterLowStock = it },
            filterExpiryAlert = filterExpiryAlert,
            onExpiryAlertChange = { filterExpiryAlert = it },
            onApply = { showFilterDialog = false }
        )
    }
}

@Composable
fun HeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFCE7B3C), shape = RoundedCornerShape(8.dp)),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "No",
            modifier = Modifier
                .weight(0.8f)
                .padding(vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Name",
            modifier = Modifier
                .weight(2f)
                .padding(vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "ID",
            modifier = Modifier
                .weight(1.5f)
                .padding(vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Exp.",
            modifier = Modifier
                .weight(1.5f)
                .padding(vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "QTY",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InventoryItemRow(item: InventoryItem, index: Int, navController: NavController) {
    val isLowStock = item.quantity < 30
    val isExpired = item.expireDate.contains("2023")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("editItem/${item.id}") }
            .background(if (index % 2 == 0) Color.White else Color(0xFFF2F2F2))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${index + 1}",
            Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = item.name,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            color = if (isLowStock) Color.Red else Color.Black
        )
        Text(
            text = item.id,
            Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = item.expireDate,
            Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            color = if (isExpired) Color(0xFFB491C8) else Color.Black
        )
        Text(
            text = item.quantity.toString(),
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = if (isLowStock) Color.Red else Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetDialog(
    onDismiss: () -> Unit,
    sortNewestFirst: Boolean,
    onSortChange: (Boolean) -> Unit,
    filterLowStock: Boolean,
    onLowStockChange: (Boolean) -> Unit,
    filterExpiryAlert: Boolean,
    onExpiryAlertChange: (Boolean) -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Sort by", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sortNewestFirst,
                    onCheckedChange = { onSortChange(true) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                )
                Text("Newest on top")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !sortNewestFirst,
                    onCheckedChange = { onSortChange(false) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                )
                Text("Oldest on top")
            }
            Spacer(Modifier.height(8.dp))
            Text("Filter by", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = filterLowStock,
                    onCheckedChange = onLowStockChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                )
                Text("Low Stock")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = filterExpiryAlert,
                    onCheckedChange = onExpiryAlertChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                )
                Text("Expiry Alerts")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply", color = Color.White)
            }
        }
    }
}

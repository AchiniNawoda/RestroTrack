package com.apptora.restrotrack.presentation.reports


import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class SalesReportItem(
    val name: String = "",
    val itemId: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen (navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var salesItems by remember { mutableStateOf(listOf<SalesReportItem>()) }
    var filteredItems by remember { mutableStateOf(listOf<SalesReportItem>()) }
    val context = LocalContext.current

    var itemIdFilter by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    LaunchedEffect(fromDate, toDate) {
        try {
            val ordersSnapshot = db.collection("orders").get().await()
            val itemsList = mutableListOf<SalesReportItem>()
            for (orderDoc in ordersSnapshot.documents) {
                val orderDate = orderDoc.getString("date") ?: ""
                if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
                    if (orderDate >= fromDate && orderDate <= toDate) {
                        val itemsSnapshot = orderDoc.reference.collection("items").get().await()
                        itemsSnapshot.documents.forEach { itemDoc ->
                            val name = itemDoc.getString("name") ?: ""
                            val itemId = itemDoc.getString("itemId") ?: ""
                            val price = itemDoc.getDouble("price") ?: 0.0
                            val quantity = (itemDoc.getLong("quantity") ?: 0).toInt()
                            itemsList.add(SalesReportItem(name, itemId, price, quantity))
                        }
                    }
                } else {
                    val itemsSnapshot = orderDoc.reference.collection("items").get().await()
                    itemsSnapshot.documents.forEach { itemDoc ->
                        val name = itemDoc.getString("name") ?: ""
                        val itemId = itemDoc.getString("itemId") ?: ""
                        val price = itemDoc.getDouble("price") ?: 0.0
                        val quantity = (itemDoc.getLong("quantity") ?: 0).toInt()
                        itemsList.add(SalesReportItem(name, itemId, price, quantity))
                    }
                }
            }
            salesItems = itemsList
            filteredItems = salesItems
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportSalesReportToCSV(context, filteredItems)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD98840))
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = itemIdFilter,
                onValueChange = { itemIdFilter = it },
                placeholder = { Text("Enter Item ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = if (fromDate.isNotEmpty() && toDate.isNotEmpty()) "From: $fromDate To: $toDate" else "",
                    onValueChange = {},
                    placeholder = { Text("Select Date Range") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable {
                            val datePickerFrom = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    fromDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                                    val datePickerTo = DatePickerDialog(
                                        context,
                                        { _, toYear, toMonth, toDay ->
                                            toDate = "%04d-%02d-%02d".format(toYear, toMonth + 1, toDay)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    datePickerTo.show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            datePickerFrom.show()
                        }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    filteredItems = salesItems.filter { item ->
                        val matchesId = itemIdFilter.isBlank() || item.itemId.contains(itemIdFilter, ignoreCase = true)
                        // Date filtering logic (pseudo if dates are available)
                        matchesId
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
            ) {
                Text("Generate Report")
            }

            Spacer(modifier = Modifier.height(16.dp))

            SalesHeaderRow()

            LazyColumn {
                items(filteredItems) { item ->
                    SalesRow(item)
                }
            }
        }
    }
}

@Composable
fun SalesHeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFCE7B3C), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Name", Modifier.weight(2f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("ID", Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Price (Rs)", Modifier.weight(1.5f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("QTY", Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun SalesRow(item: SalesReportItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if ((item.itemId.hashCode() % 2) == 0) Color(0xFFF9F9F9) else Color.White)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.name, Modifier.weight(2f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.itemId, Modifier.weight(1f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(String.format("%.2f", item.price), Modifier.weight(1.5f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.quantity.toString(), Modifier.weight(1f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

fun exportSalesReportToCSV(context: Context, salesItems: List<SalesReportItem>) {
    val fileName = "sales_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
    val file = File(context.cacheDir, fileName)

    try {
        val writer = FileWriter(file)
        writer.append("Name,ID,Price (Rs),Quantity\n")
        salesItems.forEach { item ->
            writer.append("${item.name},${item.itemId},${String.format("%.2f", item.price)},${item.quantity}\n")
        }
        writer.flush()
        writer.close()

        val uri = Uri.fromFile(file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Download CSV"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
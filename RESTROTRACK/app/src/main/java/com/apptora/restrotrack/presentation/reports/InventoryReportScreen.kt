package com.apptora.restrotrack.presentation.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val expireDate: String = "",
    val quantity: Int = 0,
    val isPerishable: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReportScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var inventoryItems by remember { mutableStateOf(listOf<InventoryItem>()) }
    var filteredItems by remember { mutableStateOf(listOf<InventoryItem>()) }
    val context = LocalContext.current

    var itemIdFilter by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Perishable", "Non-Perishable")

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("inventory").get().await()
            inventoryItems = snapshot.documents.mapNotNull { it.toObject(InventoryItem::class.java) }
            filteredItems = inventoryItems
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportInventoryToCSV(context, filteredItems)
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

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                var expanded by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Item Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    filteredItems = inventoryItems.filter { item ->
                        val matchesId = itemIdFilter.isBlank() || item.id.contains(itemIdFilter, ignoreCase = true)
                        val matchesCategory = when (selectedCategory) {
                            "All" -> true
                            "Perishable" -> item.isPerishable
                            "Non-Perishable" -> !item.isPerishable
                            else -> true
                        }
                        matchesId && matchesCategory
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
            ) {
                Text("Generate Report")
            }

            Spacer(modifier = Modifier.height(16.dp))

            HeaderRow()

            LazyColumn {
                items(filteredItems) { item ->
                    InventoryRow(item)
                }
            }
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFCE7B3C), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Name", Modifier.weight(2f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("ID", Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Expire Date", Modifier.weight(1.5f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("QTY", Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun InventoryRow(item: InventoryItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (item.isPerishable) Color(0xFFF9F9F9) else Color.White)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.name, Modifier.weight(2f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.id, Modifier.weight(1f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.expireDate, Modifier.weight(1.5f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.quantity.toString(), Modifier.weight(1f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

fun exportInventoryToCSV(context: Context, inventoryItems: List<InventoryItem>) {
    val fileName = "inventory_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
    val file = File(context.cacheDir, fileName)

    try {
        val writer = FileWriter(file)
        writer.append("Name,ID,Expire Date,Quantity\n")
        inventoryItems.forEach { item ->
            writer.append("${item.name},${item.id},${item.expireDate},${item.quantity}\n")
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
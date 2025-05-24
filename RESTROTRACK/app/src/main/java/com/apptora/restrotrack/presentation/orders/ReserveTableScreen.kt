package com.apptora.restrotrack.presentation.orders


import com.google.firebase.firestore.FirebaseFirestore


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import kotlinx.coroutines.tasks.await

import androidx.compose.ui.draw.clip

import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveTableScreen(navController: NavController, orderId: String) {
    var reservedTables by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(orderId) {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("orders")
            .whereEqualTo("status", "reserved")
            .get()
            .await()
        reservedTables = snapshot.documents.mapNotNull { it.getString("tableNo") }
    }
    val selectedDate = "Jan 18, 2023"
    val fromTime = "7.00 pm"
    val toTime = "9.00 pm"

    val tables = mutableListOf(
        "Table 01" to "4 pax",
        "Table 02" to "4 pax",
        "Table 03" to "6 pax",
        "Table 04" to "2 pax",
        "Table 05" to "4 pax",
        "Table 06" to "4 pax",
        "Table 07" to "4 pax",
        "Table 08" to "6 pax",
        "Table 09" to "6 pax"
    )

    var selectedTable by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign a table", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(selectedDate, fontWeight = FontWeight.Medium)
                    Text("$fromTime – $toTime", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(16.dp))

                Text("Reserve a Table", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(tables.size) { index ->
                            val (label, pax) = tables[index]
                            val isReserved = reservedTables.contains(label)
                            val isSelected = selectedTable == label

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isReserved -> Color.DarkGray
                                            isSelected -> Color(0xFFCE7B3C)
                                            else -> Color.LightGray
                                        }
                                    )
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clickable(enabled = !isReserved) {
                                        selectedTable = if (selectedTable == label) null else label
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(pax, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedTable != null) {
                        coroutineScope.launch {
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val querySnapshot = db.collection("orders")
                                    .whereEqualTo("orderId", orderId)
                                    .get()
                                    .await()
                                for (document in querySnapshot.documents) {
                                    document.reference.update(
                                        mapOf(
                                            "status" to "reserved",
                                            "tableNo" to selectedTable,
                                            "reservedBy" to "e45892"
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                // Handle error if needed
                            }
                            showSuccessDialog = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Reserve Table")
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success !") },
            text = { Text("You have successfully reserved the table.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }) {
                    Text("Ok")
                }
            }
        )
    }
}
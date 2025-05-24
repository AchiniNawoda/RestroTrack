package com.apptora.restrotrack.presentation.table

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apptora.restrotrack.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val orangeColor = Color(0xFFD98840)
    val db = FirebaseFirestore.getInstance()

    var tables by remember { mutableStateOf(listOf<Table>()) }
    var searchQuery by remember { mutableStateOf("") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<Table?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTable by remember { mutableStateOf<Table?>(null) }

    LaunchedEffect(Unit) {
        launch {
            tables = loadTables(db)
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
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD98840))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Search bar with filter icon
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = orangeColor,
                            modifier = Modifier.clickable { /* Add filter logic if needed */ }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        editingTable = null
                        showAddEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add new table")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table list with header and rows
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(vertical = 8.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Table No.",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                "No. of Pax",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Actions",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        // List of tables filtered by search
                        val filteredTables =
                            if (searchQuery.isBlank()) tables else tables.filter {
                                it.tableNo.contains(searchQuery, ignoreCase = true)
                                        || it.noOfPax.toString().contains(searchQuery)
                            }

                        LazyColumn {
                            itemsIndexed(filteredTables) { index, table ->
                                val backgroundColor =
                                    if (index % 2 == 0) Color(0xFFFDF6F0) else Color.White
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundColor)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = table.tableNo,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = table.noOfPax.toString(),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingTable = table
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_edit),
                                            contentDescription = "Edit Table",
                                            tint = Color(0xFFD98840),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            deletingTable = table
                                            showDeleteDialog = true
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_delete),
                                            contentDescription = "Delete Table",
                                            tint = Color(0xFFD98840),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                }
                                Divider(color = Color.LightGray, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            if (showAddEditDialog) {
                var tableNo by remember { mutableStateOf(editingTable?.tableNo ?: "") }
                var noOfPax by remember { mutableStateOf(editingTable?.noOfPax?.toString() ?: "") }
                var errorTableNo by remember { mutableStateOf(false) }
                var errorNoOfPax by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showAddEditDialog = false },
                    title = {
                        Text(text = if (editingTable == null) "Add New Table" else "Edit Table")
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = tableNo,
                                onValueChange = {
                                    tableNo = it
                                    errorTableNo = false
                                },
                                label = { Text("Table No.") },
                                isError = errorTableNo,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (errorTableNo) {
                                Text(
                                    text = "Table No. cannot be empty",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = noOfPax,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() }) {
                                        noOfPax = it
                                        errorNoOfPax = false
                                    }
                                },
                                label = { Text("No. of Pax") },
                                isError = errorNoOfPax,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (errorNoOfPax) {
                                Text(
                                    text = "Please enter a valid number",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            var valid = true
                            if (tableNo.isBlank()) {
                                errorTableNo = true
                                valid = false
                            }
                            if (noOfPax.isBlank() || noOfPax.toIntOrNull() == null || noOfPax.toInt() <= 0) {
                                errorNoOfPax = true
                                valid = false
                            }
                            if (!valid) return@TextButton

                            if (editingTable == null) {
                                addTable(db, Table(tableNo = tableNo, noOfPax = noOfPax.toInt())) {
                                    scope.launch {
                                        tables = loadTables(db)
                                    }
                                }
                            } else {
                                updateTable(db, editingTable!!.id, tableNo, noOfPax.toInt()) {
                                    scope.launch {
                                        tables = loadTables(db)
                                    }
                                }
                            }
                            showAddEditDialog = false
                        }) {
                            Text("Save", color = orangeColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddEditDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Table") },
                    text = { Text("Are you sure you want to delete table \"${deletingTable?.tableNo}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            deletingTable?.let {
                                deleteTable(db, it.id) {
                                    scope.launch {
                                        tables = loadTables(db)
                                    }
                                }
                            }
                            showDeleteDialog = false
                        }) {
                            Text("Delete", color = orangeColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    )
}

data class Table(
    val id: String = "",
    val tableNo: String = "",
    val noOfPax: Int = 0
)

suspend fun loadTables(db: FirebaseFirestore): List<Table> {
    return try {
        val snapshot = db.collection("tables").get().await()
        snapshot.documents.map { doc ->
            Table(
                id = doc.id,
                tableNo = doc.getString("tableNo") ?: "",
                noOfPax = (doc.getLong("noOfPax") ?: 0L).toInt()
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun addTable(db: FirebaseFirestore, table: Table, onComplete: () -> Unit) {
    val data = mapOf(
        "tableNo" to table.tableNo,
        "noOfPax" to table.noOfPax
    )
    db.collection("tables").add(data).addOnSuccessListener {
        onComplete()
    }
}

fun updateTable(db: FirebaseFirestore, id: String, tableNo: String, noOfPax: Int, onComplete: () -> Unit) {
    val data = mapOf(
        "tableNo" to tableNo,
        "noOfPax" to noOfPax
    )
    db.collection("tables").document(id).update(data).addOnSuccessListener {
        onComplete()
    }
}

fun deleteTable(db: FirebaseFirestore, id: String, onComplete: () -> Unit) {
    db.collection("tables").document(id).delete().addOnSuccessListener {
        onComplete()
    }
}

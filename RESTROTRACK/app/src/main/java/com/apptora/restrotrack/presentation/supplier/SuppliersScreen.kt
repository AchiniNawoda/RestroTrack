package com.apptora.restrotrack.presentation.supplier

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptora.restrotrack.R
import com.apptora.restrotrack.data.model.Supplier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var suppliers by remember { mutableStateOf(listOf<Supplier>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("suppliers").get().await()
            suppliers = snapshot.documents.mapNotNull { it.toObject(Supplier::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
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
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Tabs (All / Active / Inactive / Preferred)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Inactive", "Preferred").forEach { tab ->
                    OutlinedButton(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTab == tab) Color(0xFFCE7B3C) else Color.White
                        )
                    ) {
                        Text(tab, color = if (selectedTab == tab) Color.White else Color.Black)
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))


            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigate("add_supplier") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add new Supplier")
                }

            }

            Spacer(Modifier.height(8.dp))

            // Status legends
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusDot(Color.Red)
                Text("Inactive", fontSize = 12.sp)
                StatusDot(Color.Green)
                Text("Active", fontSize = 12.sp)
                StatusDot(Color(0xFF800080)) // Purple
                Text("Preferred", fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Table Headers
            SupplierHeaderRow()

            // List
            LazyColumn {
                val filteredList = suppliers.filter { supplier ->
                    (selectedTab == "All" || supplier.status.equals(
                        selectedTab,
                        ignoreCase = true
                    )) &&
                            (supplier.name.contains(searchQuery, ignoreCase = true) ||
                                    supplier.id.contains(searchQuery, ignoreCase = true) ||
                                    supplier.contact.contains(searchQuery, ignoreCase = true))
                }

                itemsIndexed(filteredList) { index, supplier ->
                    SupplierRow(index, supplier)
                }
            }
        }
    }
}

@Composable
fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun SupplierHeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFCE7B3C), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#",
            Modifier.weight(0.5f),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Name",
            Modifier.weight(2f),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            "ID",
            Modifier.weight(1f),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Contact",
            Modifier.weight(1.5f),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SupplierRow(index: Int, supplier: Supplier) {
    val textColor = when (supplier.status.lowercase()) {
        "active" -> Color.Green
        "inactive" -> Color.Red
        "preferred" -> Color(0xFF800080)
        else -> Color.Black
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 0) Color.White else Color(0xFFF2F2F2))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${index + 1}", Modifier.weight(0.5f), textAlign = TextAlign.Center)
        Text(supplier.name, Modifier.weight(2f), color = textColor, textAlign = TextAlign.Center)
        Text(supplier.id, Modifier.weight(1f), color = textColor, textAlign = TextAlign.Center)
        Text(
            supplier.contact,
            Modifier.weight(1.5f),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

fun exportSuppliersToCSV(context: Context, suppliers: List<Supplier>) {
    val fileName =
        "suppliers_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
    val file = File(context.cacheDir, fileName)

    try {
        val writer = FileWriter(file)
        writer.append("Name,ID,Contact,Status\n")
        suppliers.forEach { supplier ->
            writer.append("${supplier.name},${supplier.id},${supplier.contact},${supplier.status}\n")
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
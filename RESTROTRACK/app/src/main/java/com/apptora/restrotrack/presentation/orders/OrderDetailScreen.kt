package com.apptora.restrotrack.presentation.orders

import android.graphics.Bitmap
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.ExperimentalEncodingApi

data class FoodItem(
    val itemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageBase64: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun OrderDetailScreen(navController: NavController, orderId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var order by remember { mutableStateOf<Order?>(null) }
    var orderItems by remember { mutableStateOf(listOf<FoodItem>()) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteOrderDialog by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteItemDialog by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var base64Image by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            val bitmap =
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        }
    }

    fun increaseQuantity(itemId: String) {
        orderItems = orderItems.map {
            if (it.itemId == itemId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun decreaseQuantity(itemId: String) {
        orderItems = orderItems.map {
            if (it.itemId == itemId && it.quantity > 1) it.copy(quantity = it.quantity - 1) else it
        }
    }

    fun deleteItemFromFirestore(itemName: String) {
        val snapshot = db.collection("orders")
            .whereEqualTo("orderId", order?.orderId)
            .get()

        snapshot.addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                val docRef = docs.documents.first().reference
                docRef.collection("items")
                    .whereEqualTo("name", itemName)
                    .get()
                    .addOnSuccessListener { itemDocs ->
                        itemDocs.forEach { it.reference.delete() }
                        orderItems = orderItems.filter { it.name != itemName }
                    }
            }
        }
    }

    LaunchedEffect(orderId) {
        try {
            val snapshot = db.collection("orders")
                .whereEqualTo("orderId", orderId)
                .get().await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                order = doc.toObject<Order>()

                val itemsSnapshot = doc.reference.collection("items").get().await()
                orderItems = itemsSnapshot.documents.mapNotNull { it.toObject<FoodItem>() }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Info", fontWeight = FontWeight.Bold) },
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
        order?.let {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        orderItems.forEach { item ->
                            if (isEditMode) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val imageBitmap = remember(item.imageBase64) {
                                        item.imageBase64.takeIf { it.isNotEmpty() }?.let { base64 ->
                                            val decodedBytes = android.util.Base64.decode(
                                                base64,
                                                android.util.Base64.DEFAULT
                                            )
                                            val bitmap = BitmapFactory.decodeByteArray(
                                                decodedBytes,
                                                0,
                                                decodedBytes.size
                                            )
                                            bitmap?.asImageBitmap()
                                        }
                                    }

                                    if (imageBitmap != null) {
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            Modifier
                                                .size(48.dp)
                                                .background(Color.Gray, RoundedCornerShape(8.dp))
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.itemId, fontWeight = FontWeight.SemiBold)
                                        Text(item.name)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { decreaseQuantity(item.itemId) }) {
                                            Text("-")
                                        }
                                        Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { increaseQuantity(item.itemId) }) {
                                            Text("+")
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val imageBitmap = remember(item.imageBase64) {
                                        item.imageBase64.takeIf { it.isNotEmpty() }?.let { base64 ->
                                            val decodedBytes = android.util.Base64.decode(
                                                base64,
                                                android.util.Base64.DEFAULT
                                            )
                                            val bitmap = BitmapFactory.decodeByteArray(
                                                decodedBytes,
                                                0,
                                                decodedBytes.size
                                            )
                                            bitmap?.asImageBitmap()
                                        }
                                    }

                                    if (imageBitmap != null) {
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            Modifier
                                                .size(48.dp)
                                                .background(Color.Gray, RoundedCornerShape(8.dp))
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.itemId, fontWeight = FontWeight.SemiBold)
                                        Text(item.name)
                                    }
                                    Text(
                                        "Rs. %.2f".format(item.price),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Text(
                            "Summary",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text("No. of items: ${orderItems.sumOf { it.quantity }}")
                        Text("Total: Rs. ${it.amount}", fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Text(
                            "Booking Details",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        DetailRow("Order ID", it.orderId)
                        DetailRow("Customer Name", it.customer)
                        DetailRow("Date", it.date)
                        DetailRow("From", "7.00 pm")
                        DetailRow("To", "9.00 pm")
                        DetailRow("No. of Pax", it.pax.toString())
                        if (!it.tableNo.isNullOrEmpty()) {
                            DetailRow("Table No.", it.tableNo)
                        }
                        if (!it.reservedBy.isNullOrEmpty()) {
                            DetailRow("Reserved By (Employee)", it.reservedBy)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                        IconButton(onClick = { showDeleteOrderDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = {
                            isEditMode = false
                            showSaveSuccessDialog = true
                        }) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                    Button(
                        onClick = {
                            navController.navigate("reserveTable/${it.orderId}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next")
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        if (showDeleteOrderDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteOrderDialog = false },
                title = { Text("Delete Order") },
                text = { Text("Are you sure you want to delete this order?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteOrderDialog = false
                        // handle delete
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteOrderDialog = false }) { Text("No") }
                }
            )
        }

        if (showSaveSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSaveSuccessDialog = false },
                title = { Text("Success !") },
                text = { Text("You have successfully saved the changes.") },
                confirmButton = {
                    TextButton(onClick = { showSaveSuccessDialog = false }) { Text("Ok") }
                }
            )
        }

        showDeleteItemDialog?.let { (itemName, show) ->
            if (show) {
                AlertDialog(
                    onDismissRequest = { showDeleteItemDialog = null },
                    title = { Text("Delete Item") },
                    text = { Text("Are you sure you want to delete the item \"$itemName\" from this order?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteItemDialog?.first?.let { itemName ->
                                deleteItemFromFirestore(itemName)
                            }
                            showDeleteItemDialog = null
                        }) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteItemDialog = null }) { Text("No") }
                    }
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
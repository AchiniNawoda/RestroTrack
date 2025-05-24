package com.apptora.restrotrack.presentation.inventory


import android.app.DatePickerDialog
import android.graphics.Bitmap

import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInventoryItemScreen(navController: NavController, itemId: String) {
    var name by remember { mutableStateOf("") }
    var itemIdState by remember { mutableStateOf(itemId) }
    var expireDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(0) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeletedDialog by remember { mutableStateOf(false) }


    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    // Load item data from Firestore
    LaunchedEffect(itemId) {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("inventory")
            .whereEqualTo("id", itemId)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.let { doc ->
            name = doc.getString("name") ?: ""
            itemIdState = doc.getString("id") ?: ""
            expireDate = doc.getString("expireDate") ?: ""
            quantity = doc.getLong("quantity")?.toInt() ?: 0
            val imageBase64 = doc.getString("imageBase64") ?: ""
            if (imageBase64.isNotEmpty()) {
                val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageBitmap = bitmap
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .clickable { launcher.launch("image/*") }
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = Color.DarkGray
                        )
                        Text("Tap to change image", color = Color.DarkGray)
                    }
                }

                // Overlay Edit icon at bottom right
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Image",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = itemIdState,
                onValueChange = { itemIdState = it },
                label = { Text("Item ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = expireDate,
                onValueChange = { expireDate = it },
                label = { Text("Expire Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Pick Date",
                        modifier = Modifier.clickable {
                            DatePickerDialog(
                                context,
                                { _: DatePicker, y: Int, m: Int, d: Int ->
                                    val date = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    val formatter =
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    expireDate = formatter.format(date.time)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    )
                }
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { quantity-- }) {
                    Text("-", fontSize = 20.sp)
                }
                Text(quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { quantity++ }) {
                    Text("+", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("inventory")
                            .whereEqualTo("id", itemId)
                            .get()
                            .addOnSuccessListener { result ->
                                val doc = result.documents.firstOrNull()
                                doc?.reference?.update(
                                    mapOf(
                                        "name" to name,
                                        "id" to itemIdState,
                                        "expireDate" to expireDate,
                                        "quantity" to quantity,
                                        "imageBase64" to (base64Image ?: "")
                                    )
                                )
                                showSuccessDialog = true
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(onClick = {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("inventory")
                        .whereEqualTo("id", itemId)
                        .get()
                        .addOnSuccessListener { result ->
                            result.documents.firstOrNull()?.reference?.delete()
                            showDeleteDialog = false
                            showDeletedDialog = true
                        }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success !") },
            text = { Text("Item updated successfully.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Ok")
                }
            }
        )
    }

    if (showDeletedDialog) {
        AlertDialog(
            onDismissRequest = { showDeletedDialog = false },
            title = { Text("Deleted !") },
            text = { Text("Item deleted successfully.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeletedDialog = false
                    navController.popBackStack()
                }) {
                    Text("Ok")
                }
            }
        )
    }
}
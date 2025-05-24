package com.apptora.restrotrack.presentation.usermanagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPermissionScreen(navController: NavController) {
    val modules = listOf(
        "Dashboard",
        "Inventory Management",
        "Order Management",
        "User Management",
        "Reports"
    )

    val permissionStates = remember {
        mutableStateOf(
            modules.associateWith {
                mutableStateOf(mapOf("Admin" to false, "Staff" to false))
            }
        )
    }

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val snapshot = firestore.collection("permissions").document("global_permissions").get().await()
        val permissionDoc = snapshot.data
        permissionDoc?.forEach { (key, value) ->
            val castValue = (value as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value as Boolean }
            if (castValue != null) {
                permissionStates.value = permissionStates.value.toMutableMap().apply {
                    this[key]?.value = castValue
                }
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    fun updatePermission(module: String, role: String, value: Boolean) {
        val current = permissionStates.value[module]?.value ?: mapOf("Admin" to false, "Staff" to false)
        permissionStates.value = permissionStates.value.toMutableMap().apply {
            this[module]?.value = current.toMutableMap().apply { put(role, value) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD98840))
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val firestore = FirebaseFirestore.getInstance()
                        val permissionMap = permissionStates.value.mapValues { entry ->
                            entry.value.value
                        }
                        firestore.collection("permissions")
                            .document("global_permissions")
                            .set(permissionMap)
                            .addOnSuccessListener {
                                dialogMessage = "Success! You have successfully updated the permissions."
                                showDialog = true
                            }
                            .addOnFailureListener {
                                dialogMessage = "Failed to update permissions. Please try again."
                                showDialog = true
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C))
                ) {
                    Text("Update")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(modules) { module ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = module,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        val selectedRoles = permissionStates.value[module]?.value?.filterValues { it }?.keys?.joinToString(", ")
                        Text(
                            text = selectedRoles ?: "No Access",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val adminChecked = permissionStates.value[module]?.value?.get("Admin") ?: false
                            Checkbox(
                                checked = adminChecked,
                                onCheckedChange = { checked -> updatePermission(module, "Admin", checked) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Text(text = "Admin", modifier = Modifier.padding(end = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val staffChecked = permissionStates.value[module]?.value?.get("Staff") ?: false
                            Checkbox(
                                checked = staffChecked,
                                onCheckedChange = { checked -> updatePermission(module, "Staff", checked) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Text(text = "Staff", modifier = Modifier.padding(end = 8.dp))
                        }
                    }
                }
                Divider()
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Notification") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (dialogMessage.startsWith("Success")) {
                        navController.popBackStack()
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

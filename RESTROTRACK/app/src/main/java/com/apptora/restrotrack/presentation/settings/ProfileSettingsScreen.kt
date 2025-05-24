package com.apptora.restrotrack.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptora.restrotrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var customRole by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val doc = db.collection("users").document(uid).get().await()
            firstName = doc.getString("firstName") ?: ""
            lastName = doc.getString("lastName") ?: ""
            employeeId = doc.getString("employeeId") ?: ""
            role = doc.getString("role") ?: ""
            email = doc.getString("email") ?: ""
            if (role == "Staff") {
                customRole = doc.getString("customRole") ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "$firstName $lastName",
                onValueChange = {},
                label = { Text("Employee Name") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = employeeId,
                onValueChange = { employeeId = it },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = role,
                onValueChange = {},
                label = { Text("Job Role") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            if (role == "Staff") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customRole,
                    onValueChange = { customRole = it },
                    label = { Text("Custom Role") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = role != "Staff"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email or Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("change_password") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCE7B3C))
                ) {
                    Text("Change Password")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        userId?.let { uid ->
                            val updates = mutableMapOf(
                                "employeeId" to employeeId,
                                "role" to role
                            )
                            if (role == "Staff") {
                                updates["customRole"] = customRole
                            }
                            db.collection("users").document(uid).update(updates as Map<String, Any>)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        navController.context,
                                        "Profile Updated Successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            }
        }
    }
}

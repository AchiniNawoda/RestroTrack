package com.apptora.restrotrack

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.vector.ImageVector

import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.apptora.restrotrack.presentation.dashoboard.DashboardAnalyticsScreen
import com.apptora.restrotrack.utils.PreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreenWithDrawer(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White)
            ) {
                AppDrawerContent(
                    navController = navController,
                    onClose = { scope.launch { drawerState.close() } },
                    onNavigate = { route ->
                        scope.launch {
                            drawerState.close()
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.5f),
        content = {
            DashboardAnalyticsScreen(drawerState = drawerState, navController)
        }
    )
}

@Composable
fun AppDrawerContent(
    navController: NavController,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxHeight()
        .background(Color.White)
        .padding(
            top = WindowInsets.statusBars
                .asPaddingValues()
                .calculateTopPadding()
        )
    ) {
        // User Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"
                Text(userEmail, fontWeight = FontWeight.Bold)
                Text("Admin", style = MaterialTheme.typography.bodySmall)
            }
        }

        Divider()

        // Determine user role
        var userRole by remember { mutableStateOf("staff") }
        LaunchedEffect(Unit) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.uid?.let { uid ->
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            userRole = document.getString("role") ?: "staff"
                        }
                    }
            }
        }
        val items = if (userRole.lowercase() == "admin") {
            listOf(
                "Dashboard" to Icons.Default.Dashboard,
                "Inventory" to Icons.Default.List,
                "Food Menu" to Icons.Default.MenuBook,
                "Orders" to Icons.Default.ReceiptLong,
                "Suppliers" to Icons.Default.Groups,
                "Reports" to Icons.Default.BarChart,
                "User Management" to Icons.Default.Person,
                "Table Management" to Icons.Default.TableRestaurant,
                "Settings" to Icons.Default.Settings
            )
        } else {
            listOf(
                "Dashboard" to Icons.Default.Dashboard,
                "Inventory" to Icons.Default.List,
                "Orders" to Icons.Default.ReceiptLong,
                "Table Management" to Icons.Default.TableRestaurant,
                "Settings" to Icons.Default.Settings
            )
        }

        items.forEach { (label, icon) ->
            DrawerItem(label = label, icon = icon) {
                when (label) {
                    "Inventory" -> onNavigate("inventory_screen")
                    "Food Menu" -> onNavigate("food_category_screen")
                    "Orders" -> onNavigate("orders")
                    "Suppliers" -> onNavigate("suppliers")
                    "Reports" -> onNavigate("reports")
                    "User Management" -> onNavigate("user_management")
                    "Table Management" -> onNavigate("table")
                    "Settings" -> onNavigate("settings")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = {
                PreferenceHelper.logout(context)
                Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9793A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", color = Color.White)
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFFB9793A))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.Black)
    }
}

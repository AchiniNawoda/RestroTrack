package com.apptora.restrotrack.presentation.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    var orderNotificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("order_notifications", true)) }
    var inventoryAlertsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("inventory_alerts", true)) }

    fun savePreferences(key: String, value: Boolean) {
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingRow(
                title = "Order Notifications",
                checked = orderNotificationsEnabled,
                onCheckedChange = {
                    orderNotificationsEnabled = it
                    savePreferences("order_notifications", it)
                }
            )
            Divider()
            SettingRow(
                title = "Inventory Alerts",
                checked = inventoryAlertsEnabled,
                onCheckedChange = {
                    inventoryAlertsEnabled = it
                    savePreferences("inventory_alerts", it)
                }
            )
            Divider()
        }
    }
}

@Composable
fun SettingRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.Black)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFCE7B3C),
                checkedTrackColor = Color(0xFFFDD9C5)
            )
        )
    }
}
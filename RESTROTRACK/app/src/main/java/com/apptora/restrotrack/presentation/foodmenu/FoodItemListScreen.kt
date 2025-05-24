package com.apptora.restrotrack.presentation.foodmenu

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.navigation.NavController


data class FoodItem(
    val name: String = "",
    val id: String = "",
    val price: Double = 0.0,
    val categoryId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemListScreen(navController: NavController, categoryId: String) {
    val db = FirebaseFirestore.getInstance()
    var foodItems by remember { mutableStateOf(listOf<FoodItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var sortByPaxAsc by remember { mutableStateOf(false) }
    var sortByPaxDesc by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        try {
            val snapshot = db.collection("foodItems")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .await()
            foodItems = snapshot.documents.mapNotNull { it.toObject<FoodItem>() }

            val categoryQuery = db.collection("foodCategories")
                .whereEqualTo("id", categoryId)
                .get()
                .await()
            if (!categoryQuery.isEmpty) {
                categoryName = categoryQuery.documents[0].getString("name") ?: ""
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                }
            }

            Button(
                onClick = {
                    navController.navigate("addFoodItem/$categoryId")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add new item")
            }

            Spacer(modifier = Modifier.height(12.dp))

            HeaderRowFoodItems()

            LazyColumn {
                val filtered = foodItems.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                val sortedFiltered = when {
                    sortByPaxAsc -> filtered.sortedByDescending { it.price }
                    sortByPaxDesc -> filtered.sortedBy { it.price }
                    else -> filtered
                }

                itemsIndexed(sortedFiltered) { index, item ->
                    FoodItemRow(item, index)
                }
            }

            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            if (showFilterDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterDialog = false },
                    sheetState = bottomSheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Sort by Price", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByPaxAsc,
                                onCheckedChange = {
                                    sortByPaxAsc = it
                                    if (it) sortByPaxDesc = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("High to low")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sortByPaxDesc,
                                onCheckedChange = {
                                    sortByPaxDesc = it
                                    if (it) sortByPaxAsc = false
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFCE7B3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Low to high")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showFilterDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderRowFoodItems() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCE7B3C)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "No",
                Modifier.weight(0.8f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Name",
                Modifier.weight(2f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Item ID",
                Modifier.weight(2f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Price (Rs)",
                Modifier.weight(1.2f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FoodItemRow(item: FoodItem, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (index % 2 == 0) Color.White else Color(
                0xFFF9F9F9
            )
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${index + 1}",
                Modifier.weight(0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                item.name,
                Modifier.weight(2f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline
            )
            Text(item.id, Modifier.weight(2f), fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(
                String.format("%.2f", item.price),
                Modifier.weight(1.2f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
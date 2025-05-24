package com.apptora.restrotrack.presentation.foodmenu

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apptora.restrotrack.R
import com.apptora.restrotrack.data.model.FoodCategory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCategoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var foodCategories by remember { mutableStateOf(listOf<FoodCategory>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var sortByPaxAsc by remember { mutableStateOf(false) }
    var sortByPaxDesc by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("foodCategories").get().await()
            foodCategories = snapshot.documents.mapNotNull { it.toObject<FoodCategory>() }
        } catch (e: Exception) {
            // Handle error (show Snackbar, log, etc.)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                }
            }

            Button(
                onClick = {
                    navController.navigate("addCategory")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE7B3C)),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add new category")
            }

            Spacer(modifier = Modifier.height(12.dp))
            HeaderRowFoodCategory()

            LazyColumn(Modifier.fillMaxSize()) {
                val filtered = foodCategories.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                itemsIndexed(filtered) { index, item ->
                    FoodCategoryRow(item, index, navController)
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
                        Text(
                            "Sort by No. of Food Items",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
                            Text("Ascend")
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
                            Text("Descend")
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
fun HeaderRowFoodCategory() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(Color(0xFFCE7B3C), shape = RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "No",
            Modifier
                .weight(0.5f)
                .padding(6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Category",
            Modifier
                .weight(2f)
                .padding(6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
        Text(
            "ID",
            Modifier
                .weight(1.5f)
                .padding(6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
        Text(
            "No. Food",
            Modifier
                .weight(1f)
                .padding(6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FoodCategoryRow(item: FoodCategory, index: Int, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("foodItems/${item.id}")
            }
            .background(if (index % 2 == 0) Color.White else Color(0xFFF2F2F2))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${index + 1}", Modifier.weight(0.5f), textAlign = TextAlign.Center)
        Text(item.name, Modifier.weight(2f), textAlign = TextAlign.Start)
        Text(item.id, Modifier.weight(1.5f), textAlign = TextAlign.Start)
        Text(item.numberOfItems.toString(), Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}
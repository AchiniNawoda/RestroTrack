import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class User(
    val employeeId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "",
    val customRole: String = "",
    val registeredDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRolesScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    var users by remember { mutableStateOf(listOf<User>()) }
    var searchQuery by remember { mutableStateOf("") }

    val tabOptions = listOf("Staff", "Admin")

    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance().collection("users").get().await()
            users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Supplier", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabOptions.forEachIndexed { index, title ->
                    OutlinedButton(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTab == index) Color(0xFFCE7B3C) else Color.White,
                            contentColor = if (selectedTab == index) Color.White else Color.Black
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TableHeader()

            val filteredUsers = users.filter {
                it.role.equals(tabOptions[selectedTab], ignoreCase = true) &&
                        (it.firstName.contains(searchQuery, ignoreCase = true) || it.lastName.contains(searchQuery, ignoreCase = true))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                filteredUsers.forEachIndexed { index, user ->
                    UserRow(user, index)
                }
            }
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFCE7B3C), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Employee ID", "Name", "Registered Date", "Job Role").forEach {
            Text(
                text = it,
                color = Color.White,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UserRow(user: User, index: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 0) Color(0xFFF9F9F9) else Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.employeeId, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
        Text("${user.firstName} ${user.lastName}", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
        Text(user.registeredDate.ifEmpty { "-" }, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
        Text(user.customRole.ifEmpty { user.role }, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
    }
}



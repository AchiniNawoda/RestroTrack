package com.apptora.restrotrack

import UserRolesScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apptora.restrotrack.presentation.auth.LoginScreen
import com.apptora.restrotrack.presentation.notifications.NotificationScreen
import com.apptora.restrotrack.presentation.auth.SignUpScreen
import com.apptora.restrotrack.presentation.SplashScreen
import com.apptora.restrotrack.presentation.auth.ForgotPasswordScreen
import com.apptora.restrotrack.presentation.auth.OTPScreen
import com.apptora.restrotrack.presentation.auth.ResetPasswordScreen
import com.apptora.restrotrack.presentation.foodmenu.AddFoodItemScreen
import com.apptora.restrotrack.presentation.foodmenu.FoodCategoryAddScreen
import com.apptora.restrotrack.presentation.foodmenu.FoodCategoryScreen
import com.apptora.restrotrack.presentation.foodmenu.FoodItemListScreen
import com.apptora.restrotrack.presentation.inventory.AddNewInventoryBarcodeScreen
import com.apptora.restrotrack.presentation.inventory.AddNewInventoryScreen
import com.apptora.restrotrack.presentation.inventory.EditInventoryItemScreen
import com.apptora.restrotrack.presentation.orders.OrderDetailScreen
import com.apptora.restrotrack.presentation.orders.OrdersScreen
import com.apptora.restrotrack.presentation.orders.ReserveTableScreen
import com.apptora.restrotrack.presentation.reports.InventoryReportScreen
import com.apptora.restrotrack.presentation.reports.ReportsScreen
import com.apptora.restrotrack.presentation.reports.SalesReportScreen
import com.apptora.restrotrack.presentation.settings.ChangePasswordScreen
import com.apptora.restrotrack.presentation.settings.NotificationSettingsScreen
import com.apptora.restrotrack.presentation.settings.ProfileSettingsScreen
import com.apptora.restrotrack.presentation.settings.SettingsScreen
import com.apptora.restrotrack.presentation.supplier.AddNewSupplierScreen
import com.apptora.restrotrack.presentation.supplier.SupplierScreen
import com.apptora.restrotrack.presentation.table.TableScreen
import com.apptora.restrotrack.presentation.usermanagement.UserManagementScreen
import com.apptora.restrotrack.presentation.usermanagement.UserPermissionScreen
import com.example.restrotrack.ui.inventory.InventoryScreen

@Composable
fun NavHostController() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("dashboard") { MainScreenWithDrawer(navController) }

        //notification
        composable("notifications") {
            NotificationScreen(navController)
        }

        //auth
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }

        composable("otp_screen/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OTPScreen(navController, email)
        }

        composable("reset_password") {
            ResetPasswordScreen(navController)
        }

        //inventory
        composable("inventory_screen") {
            InventoryScreen(navController)
        }

        composable("addItem") {
            AddNewInventoryScreen(navController)
        }

        composable("editItem/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            EditInventoryItemScreen(navController, itemId)
        }

        //food
        composable("food_category_screen") {
            FoodCategoryScreen(navController)
        }

        composable("addCategory") {
            FoodCategoryAddScreen(navController)
        }

        composable("foodItems/{categoryId}") { backStack ->
            val categoryId = backStack.arguments?.getString("categoryId") ?: ""
            FoodItemListScreen(
                navController,
                categoryId
            ) // You can also query this from Firestore if needed
        }

        composable("addFoodItem/{categoryId}") { backStack ->
            val categoryId = backStack.arguments?.getString("categoryId") ?: ""
            AddFoodItemScreen(navController, categoryId)
        }


        //orders
        composable("orders") {
            OrdersScreen(navController)
        }

        composable("orderDetail/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(navController, orderId)
        }

        composable("reserveTable/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            ReserveTableScreen(navController, orderId)
        }

        //reports
        composable("reports") {
            ReportsScreen(navController)
        }

        composable("inventoryReport") {
            InventoryReportScreen(navController = navController)
        }

        composable("salesReport") {
            SalesReportScreen(navController = navController)
        }

        //suppliers

        composable("suppliers") {
            SupplierScreen(navController)
        }

        composable("add_supplier") {
            AddNewSupplierScreen(navController)
        }

        //userManagement
        composable("user_management") {
            UserManagementScreen(navController)
        }

        composable("user_roles") {
            UserRolesScreen(navController)
        }

        composable("user_permissions") {
            UserPermissionScreen(navController)
        }

        //table

        composable("table") {
            TableScreen(navController)
        }

        //settings
        composable("settings") {
            SettingsScreen(navController)
        }

        composable("profile_settings") {
            ProfileSettingsScreen(navController)
        }

        composable("change_password") {
            ChangePasswordScreen(navController)
        }

        composable("notify_settings") {
            NotificationSettingsScreen(navController)
        }


        //barcode
        composable("barcode") {
            AddNewInventoryBarcodeScreen(navController)
        }


    }
}

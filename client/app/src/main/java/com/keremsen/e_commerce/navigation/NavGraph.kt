package com.keremsen.e_commerce.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keremsen.e_commerce.view.AddressAddEditScreen
import com.keremsen.e_commerce.view.AddressListScreen
import com.keremsen.e_commerce.view.CheckOutScreen
import com.keremsen.e_commerce.view.HomeScreen
import com.keremsen.e_commerce.view.OrderListScreen
import com.keremsen.e_commerce.view.ProductDetailScreen
import com.keremsen.e_commerce.view.ProfileEditScreen
import com.keremsen.e_commerce.view.SplashScreen
import com.keremsen.e_commerce.view.auth.LoginScreen
import com.keremsen.e_commerce.view.auth.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.AddressList.route) {
            AddressListScreen(navController = navController)
        }
        composable(route = Screen.OrderList.route) {
            OrderListScreen(navController = navController)
        }
        composable(route = Screen.ProfileEdit.route) {
            ProfileEditScreen(navController = navController)
        }
        composable(
            route = Screen.AddressAddEdit.route,
            arguments = listOf(navArgument("addressId") { type = NavType.IntType })
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getInt("addressId") ?: 0
            AddressAddEditScreen(navController = navController, addressId = addressId)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0

            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }



        composable(
            route = Screen.Checkout.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("quantity") { type = NavType.IntType; defaultValue = 1 }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId").takeIf { it != -1 }
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
            CheckOutScreen(navController, productId, quantity)
        }
    }
}
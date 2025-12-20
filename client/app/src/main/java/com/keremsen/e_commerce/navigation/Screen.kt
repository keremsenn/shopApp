package com.keremsen.e_commerce.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object AddressList : Screen("address_list")
    data object OrderList : Screen("order_list")
    data object ProfileEdit : Screen("profile_edit")

    data object Checkout : Screen("checkout?productId={productId}&quantity={quantity}") {
        fun createRoute(productId: Int? = null, quantity: Int = 1): String {
            return if (productId != null) {
                "checkout?productId=$productId&quantity=$quantity"
            } else {
                "checkout"
            }
        }
    }
    data object AddressAddEdit : Screen("address_add_edit/{addressId}") {
        fun createRoute(addressId: Int) = "address_add_edit/$addressId"
    }
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
}
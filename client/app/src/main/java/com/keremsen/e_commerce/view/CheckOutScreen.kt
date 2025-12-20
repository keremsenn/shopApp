package com.keremsen.e_commerce.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.keremsen.e_commerce.models.requestModel.OrderItemRequest
import com.keremsen.e_commerce.utils.Constants
import com.keremsen.e_commerce.viewmodel.AddressViewModel
import com.keremsen.e_commerce.viewmodel.CartViewModel
import com.keremsen.e_commerce.viewmodel.OrderViewModel
import com.keremsen.e_commerce.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    navController: NavController,
    productId: Int? = null,
    quantity: Int = 1,
    orderViewModel: OrderViewModel = hiltViewModel(),
    addressViewModel: AddressViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val addresses by addressViewModel.addresses.collectAsState()
    val cart by cartViewModel.cart.collectAsState()
    val buyNowProduct by productViewModel.productDetail.collectAsState()

    val addressLoading by addressViewModel.isLoading.collectAsState()
    val orderLoading by orderViewModel.isLoading.collectAsState()
    val orderSuccess by orderViewModel.operationSuccess.collectAsState()
    val message by orderViewModel.message.collectAsState()

    var selectedAddressId by remember { mutableStateOf<Int?>(null) }

    // --- Verileri Yükle ---
    LaunchedEffect(Unit) {
        addressViewModel.getAddresses()
        if (productId != null) {
            productViewModel.fetchProductById(productId)
        } else {
            cartViewModel.getCart()
        }
    }

    // --- Hesaplamalar ---
    val totalPrice = if (productId != null) {
        (buyNowProduct?.price ?: 0.0) * quantity
    } else {
        cart?.total_price ?: 0.0
    }

    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            navController.navigate("home") { popUpTo("home") { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ödeme ve Onay", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                })
        },
        bottomBar = {
            Surface(shadowElevation = 12.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Toplam Tutar Gösterimi
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ödenecek Toplam:", fontSize = 16.sp, color = Color.Gray)
                        Text("$totalPrice ₺", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            selectedAddressId?.let { addressId ->
                                val items = if (productId != null) listOf(OrderItemRequest(productId, quantity)) else null
                                orderViewModel.createOrder(addressId, items)
                            }
                        },
                        enabled = selectedAddressId != null && !orderLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (orderLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Siparişi Onayla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. SİPARİŞ ÖZETİ (ÜRÜNLER) ---
            item {
                Text("Sipariş Özeti", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }

            if (productId != null) {
                // Hemen Al Ürünü
                buyNowProduct?.let { product ->
                    item {
                        CheckoutItemCard(name = product.name, price = product.price, qty = quantity, imageUrl = product.images?.firstOrNull()?.url)
                    }
                }
            } else {
                // Sepet Ürünleri
                items(cart?.items ?: emptyList()) { item ->
                    CheckoutItemCard(
                        name = item.product?.name ?: "",
                        price = item.product?.price ?: 0.0,
                        qty = item.quantity,
                        imageUrl = item.product?.images?.firstOrNull()?.url
                    )
                }
            }

            // --- 2. ÖDEME YÖNTEMİ ---
            item {
                Text("Ödeme Yöntemi", fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Kredi / Banka Kartı", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("**** **** **** 4242", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("KEREM SEN", color = Color.White)
                            Text("12/28", color = Color.White)
                        }
                    }
                }
            }

            // --- 3. ADRES SEÇİMİ ---
            item { Text("Teslimat Adresi", fontWeight = FontWeight.Bold) }

            if (addressLoading) {
                item { Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() } }
            } else {
                items(addresses) { address ->
                    val isSelected = selectedAddressId == address.id
                    OutlinedCard(
                        onClick = { selectedAddressId = address.id },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(address.title, fontWeight = FontWeight.Bold)
                                Text(address.detail, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(120.dp)) }
        }
    }
}

@Composable
fun CheckoutItemCard(name: String, price: Double, qty: Int, imageUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = Constants.getImageUrl(imageUrl),
                contentDescription = null,
                modifier = Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$qty Adet x $price ₺", fontSize = 13.sp, color = Color.Gray)
            }
            Text("${price * qty} ₺", fontWeight = FontWeight.Bold)
        }
    }
}
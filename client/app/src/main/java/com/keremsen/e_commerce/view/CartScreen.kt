package com.keremsen.e_commerce.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.keremsen.e_commerce.models.entityModel.CartItem
import com.keremsen.e_commerce.navigation.Screen
import com.keremsen.e_commerce.utils.Constants
import com.keremsen.e_commerce.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.cartMessage.collectAsState()
    val context = LocalContext.current

    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getCart()
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Sepeti Temizle") },
            text = { Text("Sepetinizdeki tüm ürünleri silmek istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Evet, Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("İptal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sepetim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    if (cart?.items?.isNotEmpty() == true) {
                        TextButton(onClick = { showClearDialog = true }) {
                            Text("Tüm Sepeti Sil", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cart?.items?.isNotEmpty() == true) {
                // ⭐ cart!!.total_price bir Double'dır. Aşağıdaki fonksiyonda doğru işlenecek.
                CartSummaryBottomBar(
                    totalPrice = cart!!.total_price,
                    onConfirmClick = {
                        navController.navigate("checkout")
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (cart == null || cart!!.items.isEmpty()) {
                EmptyCartView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cart!!.items) { item ->
                        CartItemCard(
                            item = item,
                            onIncrease = { viewModel.updateCartItem(item.id, item.quantity + 1) },
                            onDecrease = {
                                if (item.quantity > 1) viewModel.updateCartItem(item.id, item.quantity - 1)
                                else viewModel.removeFromCart(item.id)
                            },
                            onRemove = { viewModel.removeFromCart(item.id) },
                            onItemClick = {
                                item.product?.id?.let { id ->
                                    navController.navigate(Screen.ProductDetail.createRoute(id))
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = Constants.getImageUrl(item.product!!.images?.firstOrNull()?.url),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.product?.name ?: "Ürün",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                    }
                }

                // ⭐ DÜZELTME: Fiyat Double olduğu için String template ("") içine alındı
                Text(
                    text = "${item.product?.price} ₺",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)).padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                        Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    // ⭐ DÜZELTME: quantity Int olduğu için String template içine alındı
                    Text(text = "${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartSummaryBottomBar(totalPrice: Double, onConfirmClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Toplam Tutar:", fontSize = 16.sp, color = Color.Gray)
                // ⭐ DÜZELTME: totalPrice Double olduğu için String template içine alındı
                Text(
                    text = "$totalPrice ₺",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, // Parametre ismini belirttik
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sepeti Onayla", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text("Sepetiniz şu an boş.", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}
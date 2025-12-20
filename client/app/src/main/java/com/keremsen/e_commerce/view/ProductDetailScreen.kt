package com.keremsen.e_commerce.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.keremsen.e_commerce.utils.Constants
import com.keremsen.e_commerce.viewmodel.CartViewModel
import com.keremsen.e_commerce.viewmodel.FavoriteViewModel
import com.keremsen.e_commerce.viewmodel.ProductViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Int,
    productViewModel: ProductViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val product by productViewModel.productDetail.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val cart by cartViewModel.cart.collectAsState()
    val cartLoading by cartViewModel.isLoading.collectAsState()
    val cartMessage by cartViewModel.cartMessage.collectAsState()
    val favorites by favoriteViewModel.favorites.collectAsState()
    val favMessage by favoriteViewModel.message.collectAsState()

    val isFavorite = favorites.any { it.product_id == productId }
    val isInCart = cart?.items?.any { it.product?.id == productId } == true

    LaunchedEffect(productId) {
        productViewModel.fetchProductById(productId)
        favoriteViewModel.getFavorites()
        cartViewModel.getCart()
    }
    LaunchedEffect(cartMessage, favMessage) {
        cartMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            cartViewModel.clearMessage()
        }
        favMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            favoriteViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ürün Detayı", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { favoriteViewModel.toggleFavorite(productId) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoriye Ekle",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (product != null) {
                BottomActionButtons(
                    isLoading = cartLoading,
                    isInCart = isInCart,
                    stock = product!!.stock,
                    onAddToCart = { quantity ->
                        cartViewModel.addToCart(productId, quantity)
                    },
                    onBuyNowClick = { quantity ->
                        navController.navigate("checkout?productId=$productId&quantity=$quantity")
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            product?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    ProductImageGallery(
                        images = item.images ?: emptyList(),
                        productName = item.name
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(item.category_name ?: "Kategorisiz", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.price} ₺",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.rating > 0) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(18.dp))
                                Text(" ${item.rating} ", fontWeight = FontWeight.Medium)
                                Text(" | ", color = Color.Gray)
                            }
                            Text(
                                text = if (item.stock > 0) "Stokta Var (${item.stock})" else "Stokta Yok",
                                color = if (item.stock > 0) Color(0xFF2E7D32) else Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                        Text("Ürün Açıklaması", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description ?: "Bu ürün için açıklama bulunmuyor.",
                            color = Color.DarkGray,
                            lineHeight = 22.sp,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(140.dp)) // Alt barın içeriği örtmemesi için boşluk
                    }
                }
            } ?: run {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Text("Ürün bulunamadı.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProductImageGallery(
    images: List<com.keremsen.e_commerce.models.entityModel.ProductImage>,
    productName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(Color.White)
    ) {
        if (images.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Görsel Bulunmuyor", color = Color.Gray)
            }
        } else {
            val pagerState = rememberPagerState()

            Column {
                HorizontalPager(
                    count = images.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { page ->
                    AsyncImage(
                        model = Constants.getImageUrl(images[page].url),
                        contentDescription = productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                if (images.size > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(12.dp),
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = Color.LightGray
                    )
                }
            }

            if (images.size > 1) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${images.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomActionButtons(
    isLoading: Boolean,
    isInCart: Boolean,
    stock: Int,
    onAddToCart: (Int) -> Unit,
    onBuyNowClick: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val maxSelectable = min(10, stock)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 20.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            if (!isInCart && stock > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Adet Seçimi:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    QuantitySelector(
                        currentQty = quantity,
                        maxQty = maxSelectable,
                        onIncrease = { if (quantity < maxSelectable) quantity++ },
                        onDecrease = { if (quantity > 1) quantity-- }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Sepete Ekle
                OutlinedButton(
                    onClick = { onAddToCart(quantity) },
                    enabled = !isLoading && !isInCart && stock > 0,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isInCart) Color.Gray else MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (isInCart) "Sepette Var" else "Sepete Ekle", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onBuyNowClick(quantity) },
                    enabled = stock > 0,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text("Hemen Al", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(currentQty: Int, maxQty: Int, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp)).padding(4.dp)
    ) {
        IconButton(onClick = onDecrease, enabled = currentQty > 1, modifier = Modifier.size(36.dp)) {
            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "$currentQty", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        IconButton(onClick = onIncrease, enabled = currentQty < maxQty, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
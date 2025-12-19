package com.keremsen.e_commerce.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.keremsen.e_commerce.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Int,
    viewModel: ProductViewModel = hiltViewModel()
) {
    // LiveData'yı Compose State'ine çeviriyoruz
    val product by viewModel.productDetail.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    // Sayfa açıldığında ürünü çek (sadece productId değiştiğinde çalışır)
    LaunchedEffect(productId) {
        viewModel.fetchProductById(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ürün Detayı", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Favori Ekle */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoriye Ekle")
                    }
                }
            )
        },
        bottomBar = {
            if (product != null) {
                BottomActionButtons(onAddToCart = {
                    // İleride buraya CartViewModel'den bir fonksiyon gelecek
                })
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            product?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()) // Kaydırılabilir yapı
                ) {
                    // 1. Ürün Resmi (Tam genişlik)
                    AsyncImage(
                        // Backend URL'in burayla eşleşmeli
                        model = "http://192.168.0.7:5000/${item.images?.firstOrNull()?.url ?: ""}",
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(Color.White),
                        contentScale = ContentScale.Fit // Resmi kesmeden sığdır
                    )

                    // 2. Bilgi Alanı
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Kategori
                        Text(
                            text = item.category_name ?: "",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Ad ve Fiyat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.price} ₺",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stok ve Satıcı
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.rating > 0) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(18.dp))
                                Text(text = " ${item.rating} ", fontWeight = FontWeight.Medium)
                                Text(text = " | ", color = Color.Gray)
                            }
                            Text(text = "Stok: ${item.stock} | ", color = Color.Gray, fontSize = 14.sp)
                            Text(text = "Satıcı: ${item.seller_name ?: ""}", color = Color.Gray, fontSize = 14.sp)
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                        // Açıklama Başlığı
                        Text(text = "Ürün Açıklaması", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Açıklama Metni
                        Text(
                            text = item.description ?: "Bu ürün için açıklama girilmemiştir.",
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        // Alt butonların altında boşluk kalması için
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } ?: run {
                // Ürün bulunamadı durumu
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Text("Ürün bilgisi yüklenemedi.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BottomActionButtons(onAddToCart: () -> Unit) {
    // Alt kısımdaki sabit buton grubu
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 10.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(), // Cihazın alt barına denk gelmesin
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sepete Ekle
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Sepete Ekle")
            }

            // Hemen Al
            Button(
                onClick = { /* Satın Alma Akışı */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hemen Al")
            }
        }
    }
}
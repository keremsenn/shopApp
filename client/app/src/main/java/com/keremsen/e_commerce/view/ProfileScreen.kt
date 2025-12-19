package com.keremsen.e_commerce.view


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.keremsen.e_commerce.navigation.Screen
import com.keremsen.e_commerce.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // ViewModel'den anlık kullanıcı verisini dinliyoruz
    val currentUser by authViewModel.currentUser.collectAsState()

    // Sayfa açıldığında kullanıcı verisi boşsa tekrar iste (Garanti olsun)
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            authViewModel.getCurrentUser()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Hafif gri arka plan
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. ÜST PROFİL KARTI ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profil İkonu (Yuvarlak)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kullanıcı Adı ve Maili
                Text(
                    text = currentUser?.fullname ?: "Kullanıcı",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = currentUser?.email ?: "Email yükleniyor...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (!currentUser?.phone.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentUser?.phone ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. MENÜ LİSTESİ ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.ShoppingBag,
                title = "Siparişlerim",
                onClick = { /* Navigasyon eklenecek */ }
            )
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            ProfileMenuItem(
                icon = Icons.Default.LocationOn,
                title = "Adreslerim",
                onClick = { /* Navigasyon eklenecek */ }
            )
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = "Favorilerim",
                onClick = { /* Navigasyon eklenecek */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Kalan boşluğu it

        // --- 3. ÇIKIŞ YAP BUTONU ---
        Button(
            onClick = {
                authViewModel.logout() // Tokenları sil
                // Login ekranına git ve geri dönüşü engelle (popUpTo 0)
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)), // Açık kırmızı
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Çıkış Yap",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Menü Elemanı için Yardımcı Composable
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}
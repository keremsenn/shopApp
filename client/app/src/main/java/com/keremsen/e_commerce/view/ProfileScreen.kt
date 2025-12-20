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
import com.keremsen.e_commerce.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onGoToFavorites: () -> Unit
) {
    val userProfile by userViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadMyProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Üst Profil Kartı aynı kalıyor) ...
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
                Text(
                    text = userProfile?.fullname ?: "Kullanıcı",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = userProfile?.email ?: "Email yükleniyor...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (!userProfile?.phone.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = userProfile?.phone ?: "", fontSize = 14.sp, color = Color.Gray)
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
                icon = Icons.Default.Edit,
                title = "Profili Düzenle",
                onClick = { navController.navigate(Screen.ProfileEdit.route) }
            )
            ProfileMenuItem(
                icon = Icons.Default.ShoppingBag,
                title = "Siparişlerim",
                onClick = { navController.navigate(Screen.OrderList.route) }
            )
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            ProfileMenuItem(
                icon = Icons.Default.LocationOn,
                title = "Adreslerim",
                onClick = { navController.navigate(Screen.AddressList.route) }
            )
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            // DEĞİŞİKLİK BURADA: onGoToFavorites'i çağırıyoruz
            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = "Favorilerim",
                onClick = { onGoToFavorites() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ... (Çıkış Yap butonu aynı kalıyor) ...
        Button(
            onClick = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ... (ProfileMenuItem fonksiyonu aynı kalıyor) ...
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
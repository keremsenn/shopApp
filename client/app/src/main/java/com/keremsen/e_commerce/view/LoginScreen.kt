package com.keremsen.e_commerce.view.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.keremsen.e_commerce.navigation.Screen
import com.keremsen.e_commerce.ui.theme.LoginBackgroundColor
import com.keremsen.e_commerce.viewmodel.AuthViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var startAnimation by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(authState) {
        if (authState != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "Geçersiz e-posta veya şifre!", Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LoginBackgroundColor // Arka plan rengi burada uygulandı
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // İçerikleri AnimatedVisibility içine alıyoruz
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = scaleIn(
                        // Başlangıç ölçeği (0.5 = yarı boyutundan başla)
                        initialScale = 0.5f,
                        // Animasyon süresi ve yumuşatma eğrisi
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        // Büyümenin merkezi (Varsayılan olarak Center'dır ama netlik için ekledim)
                        transformOrigin = TransformOrigin.Center
                    ) + fadeIn(
                        // Büyürken aynı zamanda şeffaftan nete geçiş yapsın
                        animationSpec = tween(durationMillis = 800)
                    )
                ) {
                    // AnimatedVisibility içindeki sütun, animasyonun uygulanacağı öğeleri gruplar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Tekrar Hoş Geldiniz",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // --- EMAIL INPUT ---
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-posta", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedTextColor = Color.White, // Yazı rengi beyaz olsun ki okunsun
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- PASSWORD INPUT ---
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Şifre", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                val icon =
                                    if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- LOGIN BUTTON ---
                        Button(
                            onClick = {
                                when {
                                    email.isEmpty() || password.isEmpty() -> {
                                        Toast.makeText(
                                            context,
                                            "Lütfen tüm alanları doldurun",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    !isValidEmail(email) -> {
                                        Toast.makeText(
                                            context,
                                            "Geçersiz e-posta formatı!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                    password.length < 7 -> {
                                        Toast.makeText(
                                            context,
                                            "Geçersiz e-posta veya şifre!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        viewModel.login(email, password)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White, // Buton arka planı beyaz olsun
                                contentColor = LoginBackgroundColor // Buton yazısı teal olsun
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = LoginBackgroundColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    "Giriş Yap",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // --- KAYIT OL LİNKİ ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hesabınız yok mu?",
                                fontSize = 14.sp
                            )
                            Text(
                                text = " Kayıt Ol",
                                color = Color.White , // Link rengi de beyaz olsun
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Register.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
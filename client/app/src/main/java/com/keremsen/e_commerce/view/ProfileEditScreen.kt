package com.keremsen.e_commerce.view

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.keremsen.e_commerce.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationSuccess by viewModel.operationSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (userProfile == null) {
            viewModel.loadMyProfile()
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            // Sadece inputlar boşsa doldur (Kullanıcı yazarken silinmesin)
            if (fullname.isEmpty()) fullname = profile.fullname
            if (email.isEmpty()) email = profile.email
            if (phone.isEmpty()) phone = profile.phone ?: ""
        }
    }

    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            Toast.makeText(context, "Profil başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (!operationSuccess) {
                Toast.makeText(context, "Hata: $it", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    fun validateEmail(input: String): Boolean {
        return if (input.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            emailError = null
            true
        } else {
            emailError = "Geçersiz E-posta"
            false
        }
    }

    fun validatePhone(input: String): Boolean {
        if (!input.all { it.isDigit() }) {
            phoneError = "Sadece rakam giriniz"
            return false
        }
        return if (input.length == 10 || (input.length == 11 && input.startsWith("0"))) {
            phoneError = null
            true
        } else {
            phoneError = "Telefon 10 veya 11 hane olmalı"
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (userProfile == null && isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. İsim
                OutlinedTextField(
                    value = fullname,
                    onValueChange = { fullname = it },
                    label = { Text("İsim Soyisim") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 2. Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        validateEmail(it)
                    },
                    label = { Text("E-posta") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError!!, color = Color.Red) }
                )

                // 3. Telefon
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 11) {
                            phone = it
                            validatePhone(it)
                        }
                    },
                    label = { Text("Telefon") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = { if (phoneError != null) Text(phoneError!!, color = Color.Red) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Kaydet Butonu
                Button(
                    onClick = {
                        val isNameValid = fullname.isNotBlank()
                        val isEmailValid = validateEmail(email)
                        val isPhoneValid = validatePhone(phone)
                        val isProfileLoaded = userProfile != null

                        // --- LOGLAMA ---
                        Log.d("ProfileDebug", "Butona Basıldı!")
                        Log.d("ProfileDebug", "İsim Geçerli mi: $isNameValid")
                        Log.d("ProfileDebug", "Email Geçerli mi: $isEmailValid")
                        Log.d("ProfileDebug", "Telefon Geçerli mi: $isPhoneValid")
                        Log.d("ProfileDebug", "Profil Yüklü mü: $isProfileLoaded")

                        if (isNameValid && isEmailValid && isPhoneValid && isProfileLoaded) {
                            val finalPhone = if (phone.startsWith("0")) phone.substring(1) else phone

                            Log.d("ProfileDebug", "API İsteği Gönderiliyor... ID: ${userProfile!!.id}")

                            viewModel.updateProfile(
                                userId = userProfile!!.id,
                                fullName = fullname,
                                email = email,
                                phone = finalPhone
                            )
                        } else {
                            if (!isProfileLoaded) {
                                viewModel.loadMyProfile()
                                Toast.makeText(context, "Profil verisi bekleniyor, tekrar deneyin.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Lütfen hatalı alanları düzeltin.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Değişiklikleri Kaydet")
                    }
                }
            }
        }
    }
}
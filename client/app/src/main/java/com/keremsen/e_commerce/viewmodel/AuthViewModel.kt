package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.data.local.DataStoreManager
import com.keremsen.e_commerce.models.entityModel.User
import com.keremsen.e_commerce.models.requestModel.LoginRequest
import com.keremsen.e_commerce.models.requestModel.RegisterRequest
import com.keremsen.e_commerce.models.responseModel.AuthResponse
import com.keremsen.e_commerce.repository.AuthRepository
import com.keremsen.e_commerce.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResponse?>(null)
    val authState: StateFlow<AuthResponse?> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() {
        _error.value = null
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!

                    saveSession(
                        accessToken = authBody.access_token,
                        refreshToken = authBody.refresh_token,
                        userId = authBody.user?.id
                    )

                    _authState.value = authBody
                    _currentUser.value = authBody.user
                } else {
                    _error.value = "Giriş başarısız: Bilgilerinizi kontrol edin."
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun register(fullname: String, email: String, password: String, phone: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.register(RegisterRequest(fullname, email, password, phone))
                if (response.isSuccessful) {
                    _authState.value = response.body()
                } else {
                    _error.value = "Kayıt hatası: Bu e-posta zaten kullanımda olabilir."
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveSession(accessToken: String?, refreshToken: String?, userId: Int?) {
        accessToken?.let { dataStoreManager.saveToken(it) }
        refreshToken?.let { dataStoreManager.saveRefreshToken(it) }
        userId?.let { dataStoreManager.saveUserId(it.toString()) }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clear()
            _authState.value = null
            _currentUser.value = null
            _error.value = null
        }
    }
}
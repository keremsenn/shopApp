package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.User
import com.keremsen.e_commerce.models.requestModel.UpdateUserRequest
import com.keremsen.e_commerce.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    // 1. Giriş yapan kullanıcının kendi profili
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    // 2. Tüm kullanıcıların listesi (Admin paneli vs. için)
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    // 3. ID ile çekilen herhangi bir kullanıcının detayı
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    // Yükleniyor durumu
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Hata Mesajları
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // İşlem Başarılı mı? (Update/Delete sonrası tetiklenir)
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess

    // --- FONKSİYONLAR ---

    // 1. Mevcut (Login olmuş) kullanıcının profilini getir
    fun loadMyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getCurrentUser()
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                } else {
                    _errorMessage.value = "Profil bilgileri alınamadı: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. Tüm Kullanıcıları Getir (UserRepository'deki getAllUsers)
    fun fetchAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getAllUsers()
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Kullanıcı listesi alınamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 3. ID'ye göre Kullanıcı Getir (UserRepository'deki getUserById)
    fun fetchUserById(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getUserById(userId)
                if (response.isSuccessful) {
                    _selectedUser.value = response.body()
                } else {
                    _errorMessage.value = "Kullanıcı bulunamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 4. Kullanıcı Güncelle (UserRepository'deki updateUser)
    fun updateProfile(userId: Int, fullName: String?, email: String?, phone: String?) {
        val request = UpdateUserRequest(
            fullname = fullName,
            email = email,
            phone = phone
        )

        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val response = repository.updateUser(userId, request)
                if (response.isSuccessful) {
                    val updatedUser = response.body()?.user
                    if (updatedUser != null) {
                        _userProfile.value = updatedUser // Kendi profilimizse güncelleyelim
                        _operationSuccess.value = true
                        _errorMessage.value = "Profil güncellendi."
                    }
                } else {
                    _errorMessage.value = "Güncelleme başarısız."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 5. Kullanıcı Sil (UserRepository'deki deleteUser)
    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val response = repository.deleteUser(userId)
                if (response.isSuccessful) {
                    _errorMessage.value = "Kullanıcı silindi."
                    _operationSuccess.value = true
                    // Eğer bir listedeysek listeyi yenilemek isteyebiliriz:
                    fetchAllUsers()
                } else {
                    _errorMessage.value = "Silme işlemi başarısız."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
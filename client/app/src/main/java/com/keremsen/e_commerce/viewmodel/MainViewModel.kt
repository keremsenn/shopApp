package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.data.local.DataStoreManager
import com.keremsen.e_commerce.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkToken()
    }

    private fun checkToken() {
        viewModelScope.launch {
            val token = dataStoreManager.getAccessToken()
            if (!token.isNullOrEmpty()) {
                _startDestination.value = Screen.Home.route
            } else {
                _startDestination.value = Screen.Login.route
            }
        }
    }
}
package com.example.emy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiService

class AuthViewModelFactory(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val loginPath: String,
    private val usernameField: String,
    private val passwordField: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
        return AuthViewModel(apiService, tokenManager, loginPath, usernameField, passwordField) as T
    }
}

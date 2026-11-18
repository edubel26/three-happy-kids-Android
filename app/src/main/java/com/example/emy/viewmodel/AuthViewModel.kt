package com.example.emy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class AuthViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val loginPath: String,
    private val usernameField: String,
    private val passwordField: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun authenticate(usuario: String, clave: String) {
        if (_uiState.value == AuthUiState.Loading) return

        if (loginPath.isBlank() || usernameField.isBlank() || passwordField.isBlank()) {
            _uiState.value = AuthUiState.Error(
                "Configura apiLoginPath, apiLoginUserField y apiLoginPasswordField en Gradle"
            )
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val request = JsonObject().apply {
                    addProperty(usernameField, usuario)
                    addProperty(passwordField, clave)
                }
                val response = apiService.post(loginPath.trim('/'), request)

                if (!response.isSuccessful) {
                    val message = response.errorBody()?.string()?.let { raw ->
                        runCatching { JsonParser().parse(raw).asJsonObject.get("error")?.asString }
                            .getOrNull()
                    }
                    _uiState.value = AuthUiState.Error(
                        message ?: if (response.code() == 401) {
                            "Correo o contraseña incorrectos"
                        } else {
                            "Error HTTP ${response.code()} al iniciar sesión"
                        }
                    )
                    return@launch
                }

                val body = response.body()
                val token = body?.asJsonObject?.get("token")?.takeIf { it.isJsonPrimitive }
                    ?.asString
                val user = body?.asJsonObject?.get("user")
                if (token.isNullOrBlank() || user == null || !user.isJsonObject) {
                    _uiState.value = AuthUiState.Error("La API devolvió una respuesta de inicio de sesión incompleta")
                    return@launch
                }
                tokenManager.saveToken(token)
                tokenManager.saveUser(Gson().toJson(user))
                _uiState.value = AuthUiState.Success
            } catch (error: TimeoutCancellationException) {
                _uiState.value = AuthUiState.Error("La solicitud de inicio de sesión excedió el tiempo de espera")
            } catch (error: SocketTimeoutException) {
                _uiState.value = AuthUiState.Error("La solicitud de inicio de sesión excedió el tiempo de espera")
            } catch (error: IOException) {
                _uiState.value = AuthUiState.Error("No se pudo conectar con la API")
            } catch (error: JsonParseException) {
                _uiState.value = AuthUiState.Error("La API devolvió una respuesta JSON no válida")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = AuthUiState.Error(
                    error.message ?: "No se pudo completar el inicio de sesión"
                )
            }
        }
    }

    fun consumeSuccess() {
        if (_uiState.value == AuthUiState.Success) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

package com.example.emy.loginActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.emy.BuildConfig
import com.example.emy.MainActivity
import com.example.emy.MenuActivity
import com.example.emy.R
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiClient
import com.example.emy.viewmodel.AuthUiState
import com.example.emy.viewmodel.AuthViewModel
import com.example.emy.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginActivity2 : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var button: Button
    private var navigatedToMenu = false
    private var checkingSavedSession = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        setContentView(R.layout.activity_login2)

        val username = findViewById<EditText>(R.id.etUsuario)
        val password = findViewById<EditText>(R.id.etClave)
        button = findViewById(R.id.btnIngresar)
        findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
        findViewById<Button>(R.id.btnInicio).setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
        button.isEnabled = false

        val tokenManager = TokenManager(applicationContext)
        val factory = AuthViewModelFactory(
            ApiClient.service,
            tokenManager,
            BuildConfig.API_LOGIN_PATH,
            BuildConfig.API_LOGIN_USER_FIELD,
            BuildConfig.API_LOGIN_PASSWORD_FIELD
        )
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        button.setOnClickListener {
            val usuario = username.text.toString().trim().lowercase()
            val clave = password.text.toString()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
                username.error = "Ingresa un correo electrónico válido"
            } else if (clave.isBlank()) {
                password.error = "Ingresa tu contraseña"
            } else if (clave.length < 6) {
                password.error = "La contraseña debe tener al menos 6 caracteres"
            } else {
                viewModel.authenticate(usuario, clave)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        AuthUiState.Idle -> {
                            button.isEnabled = !checkingSavedSession
                            button.text = getString(R.string.login_button)
                        }
                        AuthUiState.Loading -> {
                            button.isEnabled = false
                            button.text = getString(R.string.login_loading)
                        }
                        AuthUiState.Success -> {
                            button.isEnabled = false
                            if (!navigatedToMenu) {
                                navigatedToMenu = true
                                Toast.makeText(this@LoginActivity2, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity2, MenuActivity::class.java))
                                viewModel.consumeSuccess()
                                finish()
                            }
                        }
                        is AuthUiState.Error -> {
                            button.isEnabled = true
                            button.text = getString(R.string.login_button)
                            Toast.makeText(this@LoginActivity2, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    checkingSavedSession = false
                    button.isEnabled = true
                    return@launch
                }
                val response = ApiClient.service.get("me")
                val user = response.body()?.asJsonObject?.get("user")
                if (response.isSuccessful && user != null && user.isJsonObject) {
                    tokenManager.saveUser(com.google.gson.Gson().toJson(user))
                    navigatedToMenu = true
                    startActivity(
                        Intent(this@LoginActivity2, MenuActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                } else if (response.code() == 401 || response.code() == 403) {
                    tokenManager.clearToken()
                    checkingSavedSession = false
                    button.isEnabled = true
                } else {
                    checkingSavedSession = false
                    button.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity2,
                        "No se pudo validar la sesión guardada (HTTP ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                checkingSavedSession = false
                button.isEnabled = true
                Toast.makeText(
                    this@LoginActivity2,
                    error.message ?: "No se pudo validar la sesión guardada",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

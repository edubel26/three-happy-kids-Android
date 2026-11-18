package com.example.emy.loginActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.emy.MenuActivity
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiClient
import com.example.emy.ui.theme.EmyTheme
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        setContent {
            EmyTheme {
                RegistrationScreen(
                    onBack = { finish() },
                    onRegistered = { token, user ->
                        lifecycleScope.launch {
                            val tokenManager = TokenManager(applicationContext)
                            tokenManager.saveToken(token)
                            tokenManager.saveUser(Gson().toJson(user))
                            startActivity(
                                Intent(this@RegistrationActivity, MenuActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun RegistrationScreen(
    onBack: () -> Unit,
    onRegistered: (String, JsonObject) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var documentTypes by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var selectedDocumentType by remember { mutableStateOf<JsonObject?>(null) }
    var documentMenuExpanded by remember { mutableStateOf(false) }
    var loadingTypes by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.service.get("tipos-documento")
            if (!response.isSuccessful) {
                error = "No se pudieron cargar los tipos de documento (HTTP ${response.code()})"
            } else {
                val types = response.body()?.asJsonObject?.getAsJsonArray("documentTypes")
                    ?: JsonArray()
                documentTypes = types.mapNotNull { it.takeIf { item -> item.isJsonObject }?.asJsonObject }
                if (documentTypes.isEmpty()) error = "No hay tipos de documento activos para el registro."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudieron cargar los tipos de documento."
        } finally {
            loadingTypes = false
        }
    }

    fun submit() {
        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim().lowercase()
        val normalizedFirstName = firstName.trim()
        val normalizedLastName = lastName.trim()
        val normalizedDocument = documentNumber.trim()
        val usernamePattern = Regex("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'_-]{2,49}$")
        val namePattern = Regex("^[\\p{L}\\p{M}][\\p{L}\\p{M} '-]{0,49}$")

        error = when {
            normalizedUsername.length !in 3..50 || !usernamePattern.matches(normalizedUsername) ->
                "El usuario debe tener entre 3 y 50 caracteres válidos."
            normalizedEmail.length > 100 ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() ->
                "Escribe un correo electrónico válido."
            !namePattern.matches(normalizedFirstName) || !namePattern.matches(normalizedLastName) ->
                "El nombre y apellido deben tener hasta 50 caracteres y no incluir números."
            selectedDocumentType == null -> "Selecciona un tipo de documento."
            !normalizedDocument.matches(Regex("^\\d{1,19}$")) || normalizedDocument.toLongOrNull() == null ->
                "El documento debe contener solo dígitos y ser válido."
            phone.trim().length > 20 || address.trim().length > 150 || city.trim().length > 60 ->
                "El teléfono, dirección o ciudad supera la longitud permitida."
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            password != confirmPassword -> "Las contraseñas no coinciden."
            else -> ""
        }
        if (error.isNotEmpty()) return

        saving = true
        scope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("nombre_usuario", normalizedUsername)
                    addProperty("email", normalizedEmail)
                    addProperty("primer_nombre", normalizedFirstName)
                    addProperty("primer_apellido", normalizedLastName)
                    addProperty("id_tipo_documento", selectedDocumentType?.get("id")?.asInt)
                    addProperty("numero_documento", normalizedDocument)
                    addProperty("telefono", phone.trim())
                    addProperty("ciudad", city.trim())
                    addProperty("direccion", address.trim())
                    addProperty("password", password)
                    addProperty("confirmar_password", confirmPassword)
                }
                val response = ApiClient.service.post("registro", body)
                val result = response.body()
                if (!response.isSuccessful) {
                    val responseError = response.errorBody()?.string()?.let { raw ->
                        runCatching { JsonParser().parse(raw).asJsonObject.get("error")?.asString }
                            .getOrNull()
                    }
                    error = responseError ?: "No se pudo completar el registro (HTTP ${response.code()})."
                    return@launch
                }
                val token = result?.asJsonObject?.get("token")?.asString
                val user = result?.asJsonObject?.get("user")?.takeIf { it.isJsonObject }?.asJsonObject
                if (token.isNullOrBlank() || user == null) {
                    error = "La API no devolvió los datos de sesión esperados."
                } else {
                    onRegistered(token, user)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                error = exception.message ?: "No se pudo completar el registro."
            } finally {
                saving = false
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("‹  Volver")
            }
            Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            RegistrationField("Nombre de usuario", username, { username = it.take(50) })
            RegistrationField(
                "Correo electrónico",
                email,
                { email = it.take(100) },
                keyboardType = KeyboardType.Email
            )
            RegistrationField("Nombre", firstName, { firstName = it.take(50) })
            RegistrationField("Apellido", lastName, { lastName = it.take(50) })

            Box {
                Button(
                    onClick = { documentMenuExpanded = true },
                    enabled = !loadingTypes && documentTypes.isNotEmpty() && !saving
                ) {
                    val typeName = selectedDocumentType?.get("nombre")?.asString
                    Text(typeName ?: if (loadingTypes) "Cargando documentos..." else "Tipo de documento")
                }
                DropdownMenu(
                    expanded = documentMenuExpanded,
                    onDismissRequest = { documentMenuExpanded = false }
                ) {
                    documentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text("${type.get("nombre").asString} (${type.get("sigla").asString})")
                            },
                            onClick = {
                                selectedDocumentType = type
                                documentMenuExpanded = false
                            }
                        )
                    }
                }
            }

            RegistrationField(
                "Número de documento",
                documentNumber,
                { value -> if (value.all(Char::isDigit)) documentNumber = value.take(19) },
                keyboardType = KeyboardType.Number
            )
            RegistrationField("Teléfono (opcional)", phone, { phone = it.take(20) }, KeyboardType.Phone)
            RegistrationField("Ciudad (opcional)", city, { city = it.take(60) })
            RegistrationField("Dirección (opcional)", address, { address = it.take(150) })
            RegistrationField(
                "Contraseña",
                password,
                { password = it },
                visualTransformation = PasswordVisualTransformation()
            )
            RegistrationField(
                "Confirmar contraseña",
                confirmPassword,
                { confirmPassword = it },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = ::submit,
                enabled = !saving && !loadingTypes && documentTypes.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saving) CircularProgressIndicator()
                else Text("Registrarme")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun RegistrationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

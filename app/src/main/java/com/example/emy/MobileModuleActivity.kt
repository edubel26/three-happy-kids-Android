package com.example.emy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiClient
import com.example.emy.loginActivity.LoginActivity2
import com.example.emy.ui.RemoteImage
import com.example.emy.ui.theme.EmyTheme
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MobileModuleActivity : ComponentActivity() {
    companion object {
        const val EXTRA_TITLE = "module_title"
        const val EXTRA_PATH = "module_path"
    }

    private val tokenManager by lazy { TokenManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val path = intent.getStringExtra(EXTRA_PATH).orEmpty()
        if (title.isBlank() || path.isBlank()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val user = tokenManager.getUser()?.let { raw ->
                runCatching { JsonParser().parse(raw).asJsonObject }.getOrNull()
            }
            val roles = user?.let(::readRoles).orEmpty()
            val userName = user?.get("nombre")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()
            val email = user?.get("correo")?.takeIf { it.isJsonPrimitive }?.asString
                ?: user?.get("email")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()

            setContent {
                EmyTheme {
                    HappyKidsNavigationDrawer(
                        userName = userName,
                        email = email,
                        roles = roles,
                        sections = sectionsFor(roles),
                        activeTitle = title,
                        onNavigate = ::openModule,
                        onLogout = ::openLogin
                    ) { openDrawer ->
                        ModuleContent(
                            title = title,
                            path = path,
                            onBack = ::finish,
                            onOpenDrawer = openDrawer,
                            onUnauthorized = {
                                lifecycleScope.launch {
                                    tokenManager.clearToken()
                                    openLogin()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun openModule(module: StoreModule) {
        if (module.path == PUBLIC_HOME_PATH || module.path == PUBLIC_CATALOG_PATH) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_OPEN_CATALOG, module.path == PUBLIC_CATALOG_PATH)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            return
        }
        startActivity(
            Intent(this, MobileModuleActivity::class.java)
                .putExtra(EXTRA_TITLE, module.title)
                .putExtra(EXTRA_PATH, module.path)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity2::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}

@Composable
private fun ModuleContent(
    title: String,
    path: String,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onUnauthorized: () -> Unit
) {
    var refreshKey by remember { mutableIntStateOf(0) }
    var content by remember { mutableStateOf<JsonElement?>(null) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(path, refreshKey) {
        loading = true
        error = ""
        try {
            val response = ApiClient.service.get(path)
            if (response.code() == 401) {
                onUnauthorized()
                return@LaunchedEffect
            }
            if (!response.isSuccessful) {
                val responseError = response.errorBody()?.string()?.let { raw ->
                    runCatching { JsonParser().parse(raw).asJsonObject }.getOrNull()
                }
                error = responseError?.get("error")?.asString
                    ?: responseError?.get("message")?.asString
                    ?: "No se pudo cargar la información (HTTP ${response.code()})."
            } else {
                content = response.body()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudo conectar con la tienda."
        } finally {
            if (kotlin.coroutines.coroutineContext.isActive) loading = false
        }
    }

    val records = remember(content) { content?.let(::findRecords).orEmpty() }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack, shape = RoundedCornerShape(14.dp)) {
                        Text("‹  Volver")
                    }
                    OutlinedButton(onClick = onOpenDrawer, shape = RoundedCornerShape(14.dp)) {
                        Text("☰  Menú")
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "HAPPY KIDS  /  TIENDA",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Información actualizada desde tu cuenta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Button(
                    onClick = { refreshKey++ },
                    enabled = !loading,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Actualizar")
                }
            }

            if (loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else if (error.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            error,
                            modifier = Modifier.padding(18.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (records.isNotEmpty()) {
                itemsIndexed(records) { index, record ->
                    RecordCard(
                        record = record,
                        index = index,
                        showImageFallback = path.displaysImages()
                    )
                }
            } else if (content == null || content?.isJsonNull == true) {
                item {
                    Text(
                        "No hay información disponible.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            Gson().toJson(content),
                            modifier = Modifier.padding(18.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(record: JsonElement, index: Int, showImageFallback: Boolean) {
    val fields = when {
        record.isJsonObject -> record.asJsonObject.entrySet()
            .filter { !it.value.isJsonNull && it.key !in setOf("success", "message") }
        else -> emptySet()
    }
    val titleField = fields.firstOrNull {
        it.key.lowercase() in setOf("nombre", "producto", "titulo", "nombre_usuario", "email")
    }
    val title = titleField?.value?.let(::displayValue) ?: "Elemento ${index + 1}"
    val imageFieldNames = fields
        .filter { it.key.isImageFieldName() }
        .map { it.key }
        .toSet()
    val imagePaths = findImageValues(record).distinct()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (imagePaths.isNotEmpty() || showImageFallback) {
                RemoteImage(
                    imagePath = imagePaths.firstOrNull(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            fields.filter { it != titleField && it.key !in imageFieldNames }.forEach { field ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        field.key.toReadableLabel(),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        displayValue(field.value),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun String.isImageFieldName(): Boolean {
    val normalized = lowercase().filter(Char::isLetterOrDigit)
    return normalized in setOf(
        "image", "images", "imagen", "imagenes", "foto", "fotos",
        "photo", "photos", "picture", "pictures", "thumbnail", "img",
        "urlimage", "urlimages", "urlimagen", "urlimagenes", "urlfoto", "urlfotos",
        "urlphoto", "urlphotos", "rutaimage", "rutaimagen", "rutafoto", "rutaphoto"
    ) || listOf("image", "imagen", "foto", "photo", "picture", "img")
        .any { normalized.startsWith(it) && normalized.removePrefix(it).let { suffix ->
            suffix in setOf("url", "path", "ruta", "src", "producto") ||
                suffix.firstOrNull()?.isDigit() == true
        } } || listOf("image", "imagen", "foto", "photo", "picture")
        .any(normalized::contains)
}

private fun imageValues(element: JsonElement): List<String> = when {
    element.isJsonPrimitive && element.asJsonPrimitive.isString ->
        listOf(element.asString)
    element.isJsonArray ->
        element.asJsonArray.flatMap(::imageValues)
    element.isJsonObject ->
        element.asJsonObject.entrySet()
            .filter {
                it.key.lowercase().filter(Char::isLetterOrDigit) in setOf(
                    "url", "src", "path", "ruta", "image", "imagen", "foto",
                    "urlimage", "urlimagen", "urlfoto", "urlphoto",
                    "imagepath", "imagenpath", "rutaimagen", "rutafoto", "rutaimage"
                )
            }
            .flatMap { imageValues(it.value) }
    else -> emptyList()
}

private fun findImageValues(element: JsonElement): List<String> {
    if (!element.isJsonObject) return emptyList()
    return element.asJsonObject.entrySet().flatMap { (key, value) ->
        if (key.isImageFieldName()) imageValues(value) else findImageValues(value)
    }
}

private fun String.displaysImages(): Boolean {
    val normalized = lowercase()
    return listOf("producto", "categoria", "carrito", "favorito", "pedido", "perfil")
        .any(normalized::contains) || normalized == "me"
}

private fun findRecords(element: JsonElement): List<JsonElement> {
    if (element.isJsonArray) return element.asJsonArray.toList()
    if (!element.isJsonObject) return emptyList()

    val objectValue = element.asJsonObject
    val preferredKeys = listOf("products", "productos", "orders", "pedidos", "users", "usuarios", "data", "items")
    for (key in preferredKeys) {
        val value = objectValue.get(key)
        if (value?.isJsonArray == true) return value.asJsonArray.toList()
        if (value?.isJsonObject == true) {
            val nested = findRecords(value)
            if (nested.isNotEmpty()) return nested
        }
    }
    return objectValue.entrySet()
        .firstOrNull { it.value.isJsonArray }
        ?.value?.asJsonArray?.toList()
        .orEmpty()
}

private fun displayValue(value: JsonElement): String = when {
    value.isJsonPrimitive -> value.asJsonPrimitive.asString
    value.isJsonArray -> value.asJsonArray.joinToString(", ") { displayValue(it) }
    else -> value.toString()
}

private fun String.toReadableLabel(): String = replace(Regex("([a-z])([A-Z])")) { match ->
    "${match.groupValues[1]} ${match.groupValues[2]}"
}
    .replace('_', ' ')
    .replaceFirstChar(Char::uppercase)

package com.example.emy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiClient
import com.example.emy.loginActivity.LoginActivity2
import com.example.emy.ui.theme.EmyTheme
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

class MenuActivity : ComponentActivity() {
    private val tokenManager by lazy { TokenManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)

        lifecycleScope.launch {
            val token = tokenManager.getToken()
            val userJson = tokenManager.getUser()
            val user = runCatching {
                userJson?.let { JsonParser().parse(it).asJsonObject }
            }.getOrNull()
            if (token.isNullOrBlank() || user == null) {
                tokenManager.clearToken()
                openLogin()
                return@launch
            }

            val roles = readRoles(user)
            setContent {
                EmyTheme {
                    StoreHome(
                        userName = user.get("nombre")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty(),
                        email = user.get("correo")?.takeIf { it.isJsonPrimitive }?.asString
                            ?: user.get("email")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty(),
                        roles = roles,
                        sections = sectionsFor(roles),
                        onOpenModule = ::openModule,
                        onLogout = {
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
                .putExtra(MobileModuleActivity.EXTRA_TITLE, module.title)
                .putExtra(MobileModuleActivity.EXTRA_PATH, module.path)
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
private fun StoreHome(
    userName: String,
    email: String,
    roles: Set<String>,
    sections: List<ModuleSection>,
    onOpenModule: (StoreModule) -> Unit,
    onLogout: () -> Unit
) {
    val roleLabel = roles.joinToString(" · ") { it.replaceFirstChar(Char::uppercase) }
    HappyKidsNavigationDrawer(
        userName = userName,
        email = email,
        roles = roles,
        sections = sections,
        activeTitle = null,
        onNavigate = onOpenModule,
        onLogout = onLogout
    ) { openDrawer ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    OutlinedButton(
                        onClick = openDrawer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("☰  Menú")
                    }
                    Text(
                        "Happy kids.",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                    Text("Hola${userName.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(14.dp)) {
                    Text("Salir")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "MODA INFANTIL PARA CADA DÍA",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Lo mejor para sus pequeños",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Explora la tienda y accede a tus herramientas de trabajo desde un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Button(
                    onClick = { sections.firstOrNull()?.modules?.firstOrNull()?.let(onOpenModule) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ver colección")
                }
            }

            if (roleLabel.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Text(
                        roleLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            sections.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        section.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    section.modules.forEach { module ->
                        ModuleCard(module = module, onClick = { onOpenModule(module) })
                    }
                }
            }
            Text(
                "Calidad y diseño en cada prenda.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
        }
    }
}

@Composable
private fun ModuleCard(module: StoreModule, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        module.mark,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(module.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", color = MaterialTheme.colorScheme.primary, fontSize = 28.sp)
        }
    }
}

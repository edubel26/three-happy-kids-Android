package com.example.emy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

data class StoreModule(
    val title: String,
    val description: String,
    val path: String,
    val mark: String
)

data class ModuleSection(
    val title: String,
    val subtitle: String,
    val modules: List<StoreModule>
)

const val PUBLIC_HOME_PATH = "mobile/home"
const val PUBLIC_CATALOG_PATH = "mobile/catalog"

fun readRoles(user: JsonObject): Set<String> {
    val roles = user.get("roles")?.takeIf { it.isJsonArray }?.asJsonArray
        ?.mapNotNull { item ->
            item.takeIf { it.isJsonPrimitive }?.asString?.trim()?.lowercase()
        }.orEmpty()
        .ifEmpty {
            user.get("rol")?.takeIf { it.isJsonPrimitive }?.asString
                ?.trim()?.lowercase()?.let(::listOf).orEmpty()
        }
    return roles.map { if (it == "admin") "administrador" else it }.toSet()
}

fun sectionsFor(roles: Set<String>): List<ModuleSection> = buildList {
    add(
        ModuleSection(
            "Tienda",
            "Compra y consulta",
            listOf(
                StoreModule("Inicio", "Productos disponibles", PUBLIC_HOME_PATH, "⌂"),
                StoreModule("Catálogo", "Explora todas las categorías", PUBLIC_CATALOG_PATH, "▦"),
                StoreModule("Carrito", "Revisa los productos elegidos", "carrito", "▤"),
                StoreModule("Favoritos", "Tus productos guardados", "favoritos", "♡"),
                StoreModule("Historial de compras", "Consulta tus pedidos", "mis-pedidos", "◷")
            )
        )
    )
    if ("administrador" in roles) {
        add(
            ModuleSection(
                "Administración",
                "Gestión de la tienda",
                listOf(
                    StoreModule("Dashboard", "Indicadores y resumen", "dashboard", "▦"),
                    StoreModule("Inventario", "Productos y existencias", "productos", "▣"),
                    StoreModule("Compras", "Compras y abastecimiento", "admin/compras/opciones", "↗"),
                    StoreModule("Actividad de empleados", "Actividad registrada", "inventario/actividad", "☷"),
                    StoreModule("Pedidos", "Consulta y gestiona pedidos", "pedidos", "▤"),
                    StoreModule("Novedades", "Incidencias reportadas", "admin/novedades/pedidos", "!"),
                    StoreModule("Usuarios", "Cuentas y roles", "usuarios", "♙")
                )
            )
        )
    }
    if ("bodeguero" in roles) {
        add(
            ModuleSection(
                "Bodega",
                "Operaciones de bodega",
                listOf(
                    StoreModule("Inicio", "Resumen de trabajo", "bodega/pedidos/disponibles", "⌂"),
                    StoreModule("Inventario", "Consulta productos y existencias", "productos", "▣"),
                    StoreModule("Pedidos", "Pedidos disponibles para preparar", "bodega/pedidos/disponibles", "▤"),
                    StoreModule("Mis pedidos", "Preparaciones asignadas", "bodega/pedidos/mios", "□"),
                    StoreModule("Compras y recepción", "Registra recepciones", "bodega/compras", "↗"),
                    StoreModule("Mi actividad", "Movimientos e historial", "inventario/actividad", "☷")
                )
            )
        )
    }
    if ("mensajero" in roles) {
        add(
            ModuleSection(
                "Mensajería",
                "Pedidos y entregas",
                listOf(
                    StoreModule("Inicio", "Pedidos listos para asignar", "mensajeria/pedidos/disponibles", "⌂"),
                    StoreModule("Mis entregas", "Pedidos asignados", "mensajeria/mis-entregas", "⇢"),
                    StoreModule("Ruta / Mapa", "Consulta tus destinos", "mensajeria/mis-entregas", "↗"),
                    StoreModule("Historial", "Entregas anteriores", "mensajeria/historial", "◷"),
                    StoreModule("Evidencias de entrega", "Fotos de las entregas", "mensajeria/mis-entregas", "▧"),
                    StoreModule("Novedades", "Reportes y observaciones", "mensajeria/mis-entregas", "!")
                )
            )
        )
    }
    if (roles.isNotEmpty()) {
        add(
            ModuleSection(
                "Cuenta",
                "Tu perfil y sesión",
                listOf(StoreModule("Actualizar perfil", "Datos personales", "me", "●"))
            )
        )
    }
}

@Composable
fun HappyKidsNavigationDrawer(
    userName: String,
    email: String,
    roles: Set<String>,
    sections: List<ModuleSection>,
    activeTitle: String?,
    onNavigate: (StoreModule) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(max = 320.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Happy kids.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                userName.ifBlank { "Cuenta HappyKids" },
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (email.isNotBlank()) {
                                Text(email, color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                roles.sorted().joinToString(" · ") { it.replaceFirstChar(Char::uppercase) }
                                    .ifBlank { "Invitado" },
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    sections.forEach { section ->
                        Text(
                            section.title,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        section.modules.forEach { module ->
                            val selected = module.title == activeTitle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        scope.launch {
                                            drawerState.close()
                                            onNavigate(module)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    androidx.compose.foundation.layout.Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            module.mark,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSecondaryContainer,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        module.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        module.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (selected) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    drawerState.close()
                                    onLogout()
                                }
                            },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            "Cerrar sesión",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    ) {
        content { scope.launch { drawerState.open() } }
    }
}

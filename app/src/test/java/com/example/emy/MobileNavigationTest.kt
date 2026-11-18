package com.example.emy

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileNavigationTest {
    @Test
    fun administratorMenuMatchesWebNavigation() {
        val sections = sectionsFor(setOf("administrador"))
        val adminItems = sections.single { it.title == "Administración" }.modules.map { it.title }

        assertEquals(
            listOf(
                "Dashboard",
                "Inventario",
                "Compras",
                "Actividad de empleados",
                "Pedidos",
                "Novedades",
                "Usuarios"
            ),
            adminItems
        )
        assertEquals(
            listOf(
                "dashboard",
                "productos",
                "admin/compras/opciones",
                "inventario/actividad",
                "pedidos",
                "admin/novedades/pedidos",
                "usuarios"
            ),
            sections.single { it.title == "Administración" }.modules.map { it.path }
        )
        assertFalse(sections.any { it.title == "Bodega" || it.title == "Mensajería" })
    }

    @Test
    fun warehouseMenuIncludesItsWebLinksAndExistingOrderViews() {
        val sections = sectionsFor(setOf("bodeguero"))
        val warehouseItems = sections.single { it.title == "Bodega" }.modules.map { it.title }

        assertEquals(
            listOf("Inicio", "Inventario", "Pedidos", "Mis pedidos", "Compras y recepción", "Mi actividad"),
            warehouseItems
        )
        assertEquals(
            listOf(
                "bodega/pedidos/disponibles",
                "productos",
                "bodega/pedidos/disponibles",
                "bodega/pedidos/mios",
                "bodega/compras",
                "inventario/actividad"
            ),
            sections.single { it.title == "Bodega" }.modules.map { it.path }
        )
        assertFalse(sections.any { it.title == "Administración" || it.title == "Mensajería" })
    }

    @Test
    fun courierMenuIncludesWebNavigationAndExistingDeliveryTools() {
        val sections = sectionsFor(setOf("mensajero"))
        val courierItems = sections.single { it.title == "Mensajería" }.modules.map { it.title }

        assertEquals(
            listOf(
                "Inicio",
                "Mis entregas",
                "Ruta / Mapa",
                "Historial",
                "Evidencias de entrega",
                "Novedades"
            ),
            courierItems
        )
        assertEquals(
            listOf(
                "mensajeria/pedidos/disponibles",
                "mensajeria/mis-entregas",
                "mensajeria/mis-entregas",
                "mensajeria/historial",
                "mensajeria/mis-entregas",
                "mensajeria/mis-entregas"
            ),
            sections.single { it.title == "Mensajería" }.modules.map { it.path }
        )
        assertFalse(sections.any { it.title == "Administración" || it.title == "Bodega" })
    }

    @Test
    fun customerMenuDoesNotExposeStaffModules() {
        val sections = sectionsFor(setOf("cliente"))
        val storeItems = sections.single { it.title == "Tienda" }.modules.map { it.title }

        assertEquals(
            listOf("Inicio", "Catálogo", "Carrito", "Favoritos", "Historial de compras"),
            storeItems
        )
        assertFalse(sections.any { it.title in setOf("Administración", "Bodega", "Mensajería") })
    }

    @Test
    fun administratorAliasIsNormalized() {
        assertTrue(readRoles(JsonObject().apply { addProperty("rol", "admin") }).contains("administrador"))
    }
}

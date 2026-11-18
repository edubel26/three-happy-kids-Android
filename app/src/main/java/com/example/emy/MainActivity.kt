package com.example.emy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.emy.data.local.TokenManager
import com.example.emy.data.remote.ApiClient
import com.example.emy.loginActivity.LoginActivity2
import com.example.emy.loginActivity.RegistrationActivity
import com.example.emy.ui.RemoteImage
import com.example.emy.ui.theme.Brand
import com.example.emy.ui.theme.BrandSoft
import com.example.emy.ui.theme.BrandStrong
import com.example.emy.ui.theme.Border
import com.example.emy.ui.theme.EmyTheme
import com.example.emy.ui.theme.Ink
import com.example.emy.ui.theme.Muted
import com.example.emy.ui.theme.Page
import com.example.emy.ui.theme.SurfaceSoft
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private enum class PublicTab {
    HOME,
    CATALOG
}

private data class ProductCategory(val id: Int, val label: String)

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_CATALOG = "open_catalog"
    }

    private val tokenManager by lazy { TokenManager(applicationContext) }
    private var authenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        setContent {
            EmyTheme(darkTheme = false) {
                PublicStoreScreen(
                    authenticated = authenticated,
                    initialTab = if (intent.getBooleanExtra(EXTRA_OPEN_CATALOG, false)) {
                        PublicTab.CATALOG
                    } else {
                        PublicTab.HOME
                    },
                    onLogin = { startActivity(Intent(this, LoginActivity2::class.java)) },
                    onRegister = { startActivity(Intent(this, RegistrationActivity::class.java)) },
                    onAccount = { startActivity(Intent(this, MenuActivity::class.java)) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            authenticated = !tokenManager.getToken().isNullOrBlank()
        }
    }
}

@Composable
private fun PublicStoreScreen(
    authenticated: Boolean,
    initialTab: PublicTab,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onAccount: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var selectedProduct by remember { mutableStateOf<JsonObject?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var allProducts by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var catalogProducts by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }
    var categoryError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryLabel by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(refreshKey, selectedTab, selectedCategoryId) {
        loading = true
        error = ""
        try {
            val categoryId = selectedCategoryId.takeIf { selectedTab == PublicTab.CATALOG }
            val response = ApiClient.service.getPublicProducts(categoryId)
            if (!response.isSuccessful) {
                error = "No se pudieron cargar los productos (HTTP ${response.code()})."
            } else {
                val loaded = response.body()?.let(::findProducts).orEmpty()
                catalogProducts = loaded
                if (categoryId == null) allProducts = loaded
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudo conectar con la tienda."
        } finally {
            if (kotlin.coroutines.coroutineContext.isActive) loading = false
        }
    }

    LaunchedEffect(refreshKey, selectedTab) {
        if (selectedTab == PublicTab.CATALOG) {
            categoryError = ""
            try {
                val response = ApiClient.service.getPublicCategories()
                if (!response.isSuccessful) {
                    categoryError = "No se pudieron cargar las categorías (HTTP ${response.code()})."
                } else {
                    categories = response.body()?.let(::findCategories).orEmpty()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                categoryError = exception.message ?: "No se pudieron cargar las categorías."
            }
        }
    }
    val homeProducts = remember(allProducts, searchText) {
        val query = searchText.trim()
        if (query.isBlank()) allProducts
        else allProducts.filter { productName(it).contains(query, ignoreCase = true) }
    }

    BackHandler(enabled = selectedProduct != null) {
        selectedProduct = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Page)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        StoreHeader(
            selectedTab = selectedTab,
            authenticated = authenticated,
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onSearch = {
                selectedTab = PublicTab.HOME
                selectedCategoryId = null
                selectedCategoryLabel = null
                selectedProduct = null
            },
            onHome = {
                selectedTab = PublicTab.HOME
                selectedCategoryId = null
                selectedCategoryLabel = null
                selectedProduct = null
            },
            onCatalog = {
                selectedTab = PublicTab.CATALOG
                selectedCategoryId = null
                selectedCategoryLabel = null
                searchText = ""
                selectedProduct = null
            },
            onLogin = onLogin,
            onRegister = onRegister,
            onAccount = onAccount
        )

        if (selectedProduct != null) {
            ProductDetail(
                product = selectedProduct!!,
                authenticated = authenticated,
                onBack = { selectedProduct = null },
                onLogin = onLogin,
                modifier = Modifier.weight(1f)
            )
        } else {
            val products = if (selectedTab == PublicTab.HOME) {
                homeProducts.take(6)
            } else {
                catalogProducts
            }
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val gridColumns = when {
                    maxWidth <= 360.dp -> 1
                    maxWidth <= 760.dp -> 2
                    maxWidth <= 1024.dp -> 3
                    else -> 4
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = if (selectedTab == PublicTab.CATALOG && maxWidth <= 760.dp) {
                            12.dp
                        } else {
                            16.dp
                        },
                        vertical = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTab == PublicTab.HOME) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeHero(onViewCollection = {
                                selectedTab = PublicTab.CATALOG
                                selectedCategoryId = null
                                selectedCategoryLabel = null
                                searchText = ""
                            })
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeading(
                                title = "Productos disponibles",
                                description = "Explora los productos disponibles para tus pequeños"
                            )
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CatalogHeading(
                                selectedCategoryLabel = selectedCategoryLabel,
                                onClearFilters = {
                                    selectedCategoryId = null
                                    selectedCategoryLabel = null
                                    searchText = ""
                                }
                            )
                        }
                        if (categories.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CategoryFilters(
                                    categories = categories,
                                    selectedCategoryId = selectedCategoryId,
                                    onSelect = { category ->
                                        selectedCategoryId = category?.id
                                        selectedCategoryLabel = category?.label
                                    }
                                )
                            }
                        }
                        if (categoryError.isNotBlank()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                StoreMessage(
                                    message = categoryError,
                                    actionLabel = "Reintentar",
                                    onAction = { refreshKey++ }
                                )
                            }
                        }
                    }

                    when {
                        loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                            StoreMessage(message = "Cargando productos…", loading = true)
                        }
                        error.isNotBlank() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            StoreMessage(
                                message = error,
                                actionLabel = "Reintentar",
                                onAction = { refreshKey++ }
                            )
                        }
                        selectedTab == PublicTab.HOME && products.isEmpty() -> item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            val message = if (searchText.isBlank()) {
                                "No hay productos disponibles."
                            } else {
                                "No se encontraron productos para “${searchText.trim()}”."
                            }
                            StoreMessage(message)
                        }
                        selectedTab == PublicTab.CATALOG && products.isEmpty() -> item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            StoreMessage("No hay productos disponibles.")
                        }
                        else -> {
                            items(products, key = ::productKey) { product ->
                                ProductCard(
                                    product = product,
                                    showVariants = selectedTab == PublicTab.CATALOG,
                                    onClick = { selectedProduct = product }
                                )
                            }
                            if (selectedTab == PublicTab.HOME) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    TextButton(
                                        onClick = {
                                            selectedTab = PublicTab.CATALOG
                                            selectedCategoryId = null
                                            selectedCategoryLabel = null
                                            searchText = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver más", color = BrandStrong)
                                    }
                                }
                            }
                            if (products.any { productImage(it) == null }) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    ImageCreditsDisclosure()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreHeader(
    selectedTab: PublicTab,
    authenticated: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onHome: () -> Unit,
    onCatalog: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onAccount: () -> Unit
) {
    var accountMenuExpanded by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Happy kids.",
                    color = BrandStrong,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box {
                    TextButton(
                        onClick = {
                            if (authenticated) onAccount() else accountMenuExpanded = true
                        }
                    ) {
                        Text(if (authenticated) "Mi cuenta" else "Cuenta", color = BrandStrong)
                    }
                    if (!authenticated) {
                        DropdownMenu(
                            expanded = accountMenuExpanded,
                            onDismissRequest = { accountMenuExpanded = false }
                        ) {
                            Text(
                                "Invitado",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = Ink,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "No has iniciado sesión",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                color = Muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            DropdownMenuItem(
                                text = { Text("Iniciar sesión") },
                                onClick = {
                                    accountMenuExpanded = false
                                    onLogin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registrarse") },
                                onClick = {
                                    accountMenuExpanded = false
                                    onRegister()
                                }
                            )
                        }
                    }
                }
            }
            SearchField(
                value = searchText,
                onValueChange = onSearchTextChange,
                onSearch = onSearch
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderLink("Inicio", selectedTab == PublicTab.HOME, onHome)
                HeaderLink("Catálogo", selectedTab == PublicTab.CATALOG, onCatalog)
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text("Buscar productos...", color = Muted, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        onSearch()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                        .semantics { contentDescription = "Buscar productos..." }
                )
            }
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                modifier = Modifier.semantics { contentDescription = "Buscar" }
            ) {
                Text("⌕", color = BrandStrong)
            }
        }
    }
}

@Composable
private fun HeaderLink(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            label,
            color = if (selected) BrandStrong else Ink,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun HomeHero(onViewCollection: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "MODA INFANTIL PARA CADA DÍA",
                color = BrandStrong,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Bienvenido a HappyKids",
                modifier = Modifier.padding(top = 12.dp),
                color = Ink,
                fontSize = 31.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Sudaderas, jeans, bermudas y mucho más para niñas y niños. Calidad y diseño en cada prenda, a los mejores precios.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onViewCollection,
                modifier = Modifier.padding(top = 18.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) {
                Text("Ver colección")
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            title,
            color = Ink,
            fontSize = 25.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            description,
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CatalogHeading(
    selectedCategoryLabel: String?,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "HappyKids · Colección",
            color = BrandStrong,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Catálogo",
            color = Ink,
            fontSize = 29.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Encuentra prendas para cada día. Explora los productos disponibles y sus detalles.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        if (selectedCategoryLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    selectedCategoryLabel?.let { CategoryChip(label = "Categoría seleccionada: $it", selected = true) {} }
                }
                TextButton(onClick = onClearFilters) {
                    Text("Limpiar filtros", color = BrandStrong)
                }
            }
        }
    }
}

@Composable
private fun CategoryFilters(
    categories: List<ProductCategory>,
    selectedCategoryId: Int?,
    onSelect: (ProductCategory?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            CategoryChip(
                label = "Todas",
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) }
            )
        }
        items(categories, key = ProductCategory::id) { category ->
            CategoryChip(
                label = category.label,
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) BrandSoft else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) Brand else Border)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 11.dp)
                .heightIn(min = 20.dp)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                label,
                color = if (selected) BrandStrong else Muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StoreMessage(
    message: String,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) CircularProgressIndicator(color = Brand)
        Text(message, color = Muted, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            OutlinedButton(
                onClick = onAction,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, Border)
            ) {
                Text(actionLabel, color = BrandStrong)
            }
        }
    }
}

private data class ReferentialImageCredit(
    val title: String,
    val author: String,
    val sourceUrl: String,
    val license: String,
    val licenseUrl: String
)

@Composable
private fun ImageCreditsDisclosure() {
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val credits = remember {
        listOf(
            ReferentialImageCredit(
                "Baby Clothes",
                "Sean MacEntee",
                "https://www.flickr.com/photos/18090920@N07/9560424310",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0/"
            ),
            ReferentialImageCredit(
                "Baby clothes",
                "Mark.Pilgrim",
                "https://www.flickr.com/photos/41169327@N00/93843796",
                "CC BY-SA 2.0",
                "https://creativecommons.org/licenses/by-sa/2.0/"
            ),
            ReferentialImageCredit(
                "Baby Clothes",
                "Joe Shlabotnik",
                "https://www.flickr.com/photos/40646519@N00/2284764037",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0/"
            ),
            ReferentialImageCredit(
                "Baby Clothes",
                "Joe Shlabotnik",
                "https://www.flickr.com/photos/40646519@N00/2295445164",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0/"
            ),
            ReferentialImageCredit(
                "Baby clothes",
                "StarsApart",
                "https://www.flickr.com/photos/39042870@N00/15111483361",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0/"
            ),
            ReferentialImageCredit(
                "Baby Clothes",
                "nateOne",
                "https://www.flickr.com/photos/49998984@N00/3276169544",
                "CC BY 2.0",
                "https://creativecommons.org/licenses/by/2.0/"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Créditos de imágenes ilustrativas", color = BrandStrong)
        }
        if (expanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Las imágenes que no tienen foto propia son referenciales y no representan un producto específico.",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    credits.forEach { credit ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "${credit.title} — ${credit.author}",
                                color = Ink,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "fuente",
                                    color = BrandStrong,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { uriHandler.openUri(credit.sourceUrl) }
                                )
                                Text("·", color = Muted)
                                Text(
                                    credit.license,
                                    color = BrandStrong,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { uriHandler.openUri(credit.licenseUrl) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: JsonObject,
    showVariants: Boolean,
    onClick: () -> Unit
) {
    val name = productName(product)
    val actualImage = productImage(product)
    val imagePath = actualImage ?: referentialImage(product)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                RemoteImage(
                    imagePath = imagePath,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .background(SurfaceSoft),
                    contentScale = ContentScale.Fit,
                    cacheKey = product.stringFrom("updated_at", "fecha_actualizacion", "updatedAt")
                )
                if (actualImage == null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Text(
                            "Imagen ilustrativa",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            color = Muted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    name,
                    color = Ink,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Text(
                    productPrice(product) ?: "Precio no disponible.",
                    color = BrandStrong,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (productStock(product)?.let { it <= 0.0 } == true) {
                    Text(
                        "Agotado",
                        color = BrandStrong,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (showVariants) {
                    productVariants(product)?.let {
                        Text(it, color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text("Ver producto", color = BrandStrong)
                }
            }
        }
    }
}

@Composable
private fun ProductDetail(
    product: JsonObject,
    authenticated: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = productName(product)
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedButton(onClick = onBack) { Text("‹  Volver al catálogo") }
        }
        item {
            RemoteImage(
                imagePath = productImage(product) ?: referentialImage(product),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f),
                contentScale = ContentScale.Fit
            )
        }
        item {
            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(productPrice(product) ?: "Precio no disponible.", color = BrandStrong)
            productCategory(product)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        product.entrySet().filterNot { it.key.isProductImageKey() || it.value.isJsonNull }.forEach { field ->
            val label = field.key.toReadableProductLabel()
            val value = field.value.displayText()
            if (value.isNotBlank() && label !in setOf("nombre", "producto", "titulo", "precio", "categoria")) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label.replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.labelMedium)
                        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (!authenticated) {
            item {
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Inicia sesión para comprar")
                }
            }
        }
    }
}

private fun findProducts(element: JsonElement): List<JsonObject> {
    if (element.isJsonArray) {
        return element.asJsonArray.mapNotNull { it.takeIf(JsonElement::isJsonObject)?.asJsonObject }
    }
    if (!element.isJsonObject) return emptyList()

    val obj = element.asJsonObject
    for (key in listOf("products", "productos", "data", "items", "results")) {
        val nested = obj.get(key) ?: continue
        val result = findProducts(nested)
        if (result.isNotEmpty()) return result
    }
    return obj.entrySet().firstNotNullOfOrNull { (_, value) ->
        value.takeIf { it.isJsonArray }?.let(::findProducts)?.takeIf(List<JsonObject>::isNotEmpty)
    }.orEmpty()
}

private fun findCategories(element: JsonElement): List<ProductCategory> {
    if (!element.isJsonObject) return emptyList()
    val categories = element.asJsonObject.get("categories")
        ?.takeIf { it.isJsonArray }
        ?.asJsonArray
        ?: return emptyList()
    return categories.mapNotNull { item ->
        if (!item.isJsonObject) return@mapNotNull null
        val category = item.asJsonObject
        val id = category.stringFrom("id_categoria", "id", "category_id")?.toIntOrNull()
            ?: return@mapNotNull null
        val label = category.stringFrom("nombre", "name")?.takeIf(String::isNotBlank)
            ?: return@mapNotNull null
        ProductCategory(id, label)
    }
}

private fun productKey(product: JsonObject): String =
    product.stringFrom("id", "id_producto", "product_id") ?: productName(product)

private fun productName(product: JsonObject): String =
    product.stringFrom("nombre", "nombre_producto", "name", "producto", "titulo")
        ?.takeIf(String::isNotBlank) ?: "Producto"

private fun productImage(product: JsonObject): String? =
    listOf(
        "imagen_producto", "imagen", "url_imagen", "imagen_url", "foto", "url_foto",
        "image", "image_url", "photo", "thumbnail", "img"
    ).firstNotNullOfOrNull { key ->
        product.get(key)
            ?.takeIf { it.isJsonPrimitive && !it.isJsonNull }
            ?.asString
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

private fun referentialImage(product: JsonObject): String {
    val id = product.stringFrom("id", "id_producto", "product_id")?.toIntOrNull()
    val images = (1..10).map { "producto$it.jpg" } +
        (1..6).map { "referencial-bebe-${it.toString().padStart(2, '0')}.jpg" }
    val index = id?.takeIf { it > 0 }?.let { (it - 1) % images.size } ?: 0
    return "/media/productos/${images[index]}"
}

private fun productCategory(product: JsonObject): String? {
    val value = listOf("categoria", "category", "nombre_categoria")
        .firstNotNullOfOrNull { product.get(it)?.takeIf { item -> !item.isJsonNull } }
        ?: return null
    return if (value.isJsonObject) {
        value.asJsonObject.stringFrom("nombre", "name", "categoria")
    } else {
        value.displayText().takeIf(String::isNotBlank)
    }
}

private fun productPrice(product: JsonObject): String? {
    val raw = product.stringFrom("precio", "precio_venta", "price", "valor", "precio_unitario")
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: return null
    val number = raw.replace(",", "").toDoubleOrNull()
        ?.takeIf { it.isFinite() && it >= 0.0 }
        ?: return null
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).apply {
        maximumFractionDigits = 0
    }.format(number)
}

private fun productStock(product: JsonObject): Double? =
    product.stringFrom("stock", "existencias", "cantidad_disponible")?.toDoubleOrNull()

private fun productVariants(product: JsonObject): String? {
    val size = product.stringFrom("talla", "size")?.takeIf(String::isNotBlank)
    val color = product.stringFrom("color", "colour")?.takeIf(String::isNotBlank)
    return listOfNotNull(size?.let { "Talla $it" }, color?.let { "Color $it" })
        .joinToString(" · ")
        .takeIf(String::isNotBlank)
}

private fun JsonObject.stringFrom(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString
    }

private fun JsonElement.displayText(): String = when {
    isJsonPrimitive -> asJsonPrimitive.asString
    isJsonArray -> asJsonArray.joinToString(", ") { it.displayText() }
    else -> toString()
}

private fun String.isProductImageKey(): Boolean {
    val key = lowercase().filter(Char::isLetterOrDigit)
    return listOf("image", "imagen", "foto", "photo", "picture", "thumbnail", "img")
        .any(key::contains)
}

private fun String.toReadableProductLabel(): String =
    replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}" }
        .replace('_', ' ')
        .trim()

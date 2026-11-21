# Módulos y componentes

El único módulo Gradle de aplicación es `:app`. Esta página describe sus componentes Android y las clases principales del paquete `com.example.emy`. Para rutas exactas de cada archivo, consulta [ESTRUCTURA_DEL_PROYECTO.md](ESTRUCTURA_DEL_PROYECTO.md).

## Componentes de pantalla

| Componente | Recibe / consulta | Proceso | Resultado y relaciones |
|---|---|---|---|
| `MainActivity` | API pública de categorías/productos; selecciones de búsqueda/categoría/producto; estado local de sesión. | Carga el catálogo, extrae campos de JSON con alias conocidos, filtra la UI y resuelve imágenes. | Presenta Inicio, Catálogo y detalle. Abre login; usa `ApiClient`, `ImageUrlResolver`, `RemoteImage` y `TokenManager`. |
| `LoginActivity2` | Correo/contraseña del formulario XML; estado de `AuthViewModel`; posible sesión guardada. | Valida entradas, inicia login y puede validar sesión mediante `GET me`. | Muestra estados/mensajes y navega al menú tras éxito. |
| `RegistrationActivity` | Campos de registro y tipos de documento remotos. | Carga opciones, valida campos y envía el alta. | Si recibe token y usuario, persiste sesión y abre el menú. |
| `MenuActivity` | Token y JSON de usuario almacenados. | Comprueba datos locales, obtiene nombre/correo/roles y llama `sectionsFor()`. | Muestra secciones, módulos y logout. Si faltan datos de sesión, vuelve al login. |
| `MobileModuleActivity` | Título y ruta relativa recibidos mediante Intent. | Ejecuta GET; procesa JSON como registros genéricos; trata específicamente 401. | Muestra tarjetas/texto o una redirección al login; depende de `ApiClient` y `TokenManager`. |

## Navegación y rol

### `MobileNavigation.kt`

- **`StoreModule`**: describe una entrada navegable, su nombre y su ruta de API.
- **`ModuleSection`**: agrupa módulos bajo una sección del menú.
- **`readRoles()`**: extrae y normaliza los roles del usuario; reconoce `roles`/`rol` y el alias `admin`.
- **`sectionsFor()`**: produce las secciones visibles en el cliente de acuerdo con los roles.
- **`HappyKidsNavigationDrawer`**: presenta las secciones en el drawer Compose y notifica la selección.

Las rutas configuradas incluyen catálogo/carrito/pedidos, dashboard, operaciones de bodega y entregas de mensajería. Varias opciones de mensajería conducen a una misma ruta. El acceso visual según rol no es control de acceso de servidor.

## Autenticación

### `AuthUiState.kt`

Modelo de estado de interfaz de login: `Idle`, `Loading`, `Success` y `Error`. Lo consume `LoginActivity2` mientras opera `AuthViewModel`.

### `AuthViewModel.kt`

Recibe las dependencias de servicio/persistencia que le entrega la fábrica, realiza la operación de autenticación, interpreta éxito/error y guarda los datos cuando la respuesta contiene la sesión esperada. No representa todo el estado de las demás pantallas.

### `AuthViewModelFactory.kt`

Construye el ViewModel con las dependencias concretas utilizadas por el login.

### `TokenManager.kt`

Encapsula DataStore Preferences `user_prefs` con las claves `auth_token` y `auth_user`. Permite obtener/guardar esos valores y `clearToken()` los elimina. No se observa cifrado explícito.

## Red e imágenes

### `ApiClient.kt`

Configura Gson, OkHttp, logging y Retrofit. Declara `ApiService` con operaciones tipadas de login, sesión, catálogo, categorías y registro, además de operaciones genéricas para rutas de módulos. Usa las constantes `BuildConfig` para su base de API y la ruta/campos de login.

**Inconsistencia importante:** aunque el interceptor accede al token local, establece `Authorization` como el texto literal `******`. Las solicitudes autenticadas no reciben el token esperado tal como está el código.

### `ImageUrlResolver.kt`

`resolveImageUrl` valida una URL o ruta recibida, completa rutas relativas con el backend base, conserva destinos externos y normaliza hosts loopback usando el host configurado. Rechaza valores vacíos y esquemas no HTTP(S). Sus casos principales tienen pruebas unitarias.

### `RemoteImage.kt`

Componente Compose que configura Coil para cargar el modelo/URL remoto y presentar el placeholder en estado vacío o error, con caché de imagen.

## Tema y recursos

- `ui/theme/Color.kt`: paleta de colores Compose.
- `ui/theme/Theme.kt`: esquema claro/oscuro y proveedor `EmyTheme`.
- `ui/theme/Type.kt`: tipografía Compose.
- `res/drawable/image_placeholder.xml`: gráfico de reserva para imágenes.
- `res/layout/activity_login2.xml`: controles tradicionales del login.
- `res/layout/activity_menu.xml`: layout XML existente que no usa el menú Compose actual.
- `res/values/strings.xml`, `colors.xml`, `themes.xml`: recursos y tema Android.
- `res/xml/network_security_config.xml` y `src/debug/res/xml/network_security_config.xml`: políticas de cleartext para release/base y debug.

## Relación con el sistema externo

Los componentes no acceden directamente a una base de datos de tienda. `ApiService` conversa por HTTP/JSON con un backend independiente. Coil puede descargar imágenes desde el propio host del backend o hosts externos. Sin backend, la app puede presentar su estructura, pero no tiene datos remotos de catálogo/autenticación.

## Pruebas relacionadas

- `MobileNavigationTest.kt`: roles y módulos visibles.
- `ImageUrlResolverTest.kt`: rutas relativas, externas, loopback e inválidas.
- `ExampleUnitTest.kt`: prueba de ejemplo.
- `ExampleInstrumentedTest.kt`: identidad Android del paquete `com.example.emy`.

No se ven pruebas automatizadas de las pantallas de registro/login contra un backend.

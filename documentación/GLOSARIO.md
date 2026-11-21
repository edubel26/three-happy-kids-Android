# Glosario

| Término | Explicación sencilla |
|---|---|
| **Activity** | Pantalla/entrada del sistema Android que aloja una interfaz y participa en el ciclo de vida de la app. Emy usa una Activity para cada pantalla principal. |
| **Android SDK** | Herramientas y bibliotecas de Android necesarias para compilar e instalar aplicaciones. Cada desarrollador configura su propia ruta local. |
| **API** | Interfaz por la que una aplicación solicita datos o acciones a otro programa. En emy es el servicio web del backend. |
| **API REST** | API que se comunica por HTTP mediante rutas y verbos como GET o POST. |
| **AVD** | *Android Virtual Device*: teléfono Android virtual creado para ejecutar la app en un emulador. |
| **Backend** | Programa/servidor que proporciona los datos, autentica usuarios y ejecuta la lógica de la tienda. No está incluido en este proyecto Android. |
| **BuildConfig** | Clase generada durante la compilación que contiene constantes de configuración para esa variante, por ejemplo las URL de API. |
| **Catálogo** | Productos y categorías públicos presentados por `MainActivity`. Sus datos vienen de la API, no de archivos locales del proyecto. |
| **Cleartext** | Tráfico HTTP sin cifrado TLS. La variante debug permite cleartext; la configuración base/release lo bloquea. |
| **Compose / Jetpack Compose** | Herramientas de Android para crear interfaces mediante funciones Kotlin en vez de describir toda la pantalla en XML. |
| **DataStore Preferences** | Almacenamiento local de preferencias clave-valor; emy lo utiliza para token y datos JSON de usuario. |
| **Endpoint / ruta** | Dirección relativa de una operación del backend, por ejemplo `productos-publicos`. Se combina con la URL base. |
| **Gradle** | Sistema que configura y ejecuta la compilación, pruebas y empaquetado. |
| **Gradle Wrapper** | Scripts `gradlew`/`gradlew.bat` que descargan y utilizan la versión de Gradle definida para el proyecto. |
| **HTTP** | Protocolo de solicitudes/respuestas usado entre la app y el servidor. HTTP simple no cifra el contenido. |
| **Intent** | Mensaje Android usado, entre otras cosas, para abrir otra Activity y enviarle datos. emy pasa el título y la ruta del módulo de esta forma. |
| **JSON** | Formato de texto para representar objetos, listas y valores. La API de emy intercambia JSON con la app. |
| **JDK / JVM** | El JDK contiene herramientas de Java; la JVM ejecuta Gradle. Gradle 9 necesita JVM 17 o posterior. |
| **Material 3** | Sistema de componentes y estilos de interfaz usado por varias pantallas Compose. |
| **minSdk** | Versión mínima de Android declarada para instalar la app: API 24. |
| **OkHttp** | Cliente HTTP subyacente que Retrofit usa para enviar solicitudes; también permite configurar interceptores. |
| **Retrofit** | Biblioteca que representa operaciones HTTP como funciones/interfaz Kotlin. |
| **Rol** | Etiqueta del usuario (por ejemplo, administrador o mensajero) que la app usa para elegir qué módulos mostrar. La autorización efectiva corresponde al backend. |
| **SDK Platform / compileSdk** | API Android usada por el compilador; emy apunta a Android 36. |
| **targetSdk** | Nivel de API Android para el que la app declara comportamiento: 36. |
| **Token** | Cadena que el backend puede emitir al iniciar sesión y que normalmente identifica la sesión. En emy se guarda localmente, pero el interceptor actual no envía su valor. |
| **URL base** | Dirección raíz del backend/API a la que se agregan rutas relativas. |
| **Variante debug/release** | Configuraciones de desarrollo y publicación. En este proyecto la red debug permite HTTP y la configuración base/release bloquea cleartext. |
| **ViewModel** | Componente que mantiene estado y lógica de presentación más allá de una recomposición de UI. `AuthViewModel` controla estados del login. |
| **`10.0.2.2`** | Dirección especial que usa el Android Emulator para acceder al host PC. Un teléfono físico no la usa para llegar a ese PC. |
| **`admin` / `administrador`** | Variantes del nombre de rol que `readRoles()` normaliza en la navegación local. |
| **`auth_token` / `auth_user`** | Claves DataStore donde `TokenManager` guarda el token y el JSON del usuario. |
| **`backendBaseUrl`** | Propiedad Gradle para el host backend y resolución de imágenes; predeterminado `http://10.0.2.2:3000/`. |
| **`apiBaseUrl`** | Propiedad Gradle que define la base Retrofit; por defecto se deriva del backend agregando `api/v1/`. |
| **`user_prefs`** | Nombre del DataStore Preferences empleado por `TokenManager`. |

# Estructura del proyecto

Las rutas de esta guía son relativas a la raíz que contiene `settings.gradle.kts`. El proyecto Gradle se llama `emy` y contiene el módulo de aplicación `:app`.

## Árbol de archivos

```text
raíz/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/emy/
│       │   │   ├── MainActivity.kt
│       │   │   ├── MenuActivity.kt
│       │   │   ├── MobileModuleActivity.kt
│       │   │   ├── MobileNavigation.kt
│       │   │   ├── data/local/TokenManager.kt
│       │   │   ├── data/remote/ApiClient.kt
│       │   │   ├── data/remote/ImageUrlResolver.kt
│       │   │   ├── loginActivity/LoginActivity2.kt
│       │   │   ├── loginActivity/RegistrationActivity.kt
│       │   │   ├── ui/RemoteImage.kt
│       │   │   ├── ui/theme/Color.kt
│       │   │   ├── ui/theme/Theme.kt
│       │   │   ├── ui/theme/Type.kt
│       │   │   └── viewmodel/
│       │   │       ├── AuthUiState.kt
│       │   │       ├── AuthViewModel.kt
│       │   │       └── AuthViewModelFactory.kt
│       │   ├── res/
│       │   │   ├── drawable/image_placeholder.xml
│       │   │   ├── layout/activity_login2.xml
│       │   │   ├── layout/activity_menu.xml
│       │   │   ├── values/colors.xml
│       │   │   ├── values/strings.xml
│       │   │   ├── values/themes.xml
│       │   │   └── xml/
│       │   │       ├── backup_rules.xml
│       │   │       ├── data_extraction_rules.xml
│       │   │       └── network_security_config.xml
│       │   ├── test/java/com/example/emy/
│       │   │   ├── ExampleUnitTest.kt
│       │   │   ├── MobileNavigationTest.kt
│       │   │   └── data/remote/ImageUrlResolverTest.kt
│       │   ├── androidTest/java/com/example/emy/ExampleInstrumentedTest.kt
│       │   └── debug/res/xml/network_security_config.xml
│       └── .gitignore
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── local.properties                 (local; no copiar entre equipos)
├── .gradle/                         (caché/configuración local)
├── .kotlin/                         (datos generados/locales)
└── .idea/                           (metadatos del IDE)
```

`documentación/` contiene estas guías y no participa en la compilación de Android.

## Configuración Gradle

- `settings.gradle.kts`: define repositorios de plugins y dependencias, el nombre raíz `emy` e incluye `:app`.
- `build.gradle.kts`: declara versiones globales de Android Gradle Plugin y plugin Compose de Kotlin.
- `gradle.properties`: opciones de Gradle y ajustes AndroidX/Kotlin.
- `gradle/wrapper/gradle-wrapper.properties`: versión de Gradle que descarga el wrapper (9.0.0).
- `gradlew` y `gradlew.bat`: lanzadores del wrapper para Unix y Windows.
- `app/build.gradle.kts`: identidad de la app, SDK, versiones, propiedades para API, características de Compose/BuildConfig y dependencias.
- `app/proguard-rules.pro`: reglas de ProGuard/R8; la configuración actual de release desactiva minificación.
- `app/.gitignore`: excluye la salida generada de `app/build/`.

## Código fuente

### Actividades y pantallas

- `app/src/main/java/com/example/emy/MainActivity.kt`: pantalla Compose pública de tienda, catálogo, búsqueda, categorías, producto, precio e imágenes.
- `app/src/main/java/com/example/emy/loginActivity/LoginActivity2.kt`: login tradicional basado en `activity_login2.xml`; valida datos, consulta `AuthViewModel` y gestiona la sesión.
- `app/src/main/java/com/example/emy/loginActivity/RegistrationActivity.kt`: formulario Compose de registro y carga de tipos de documento.
- `app/src/main/java/com/example/emy/MenuActivity.kt`: pantalla Compose de bienvenida y módulos para el usuario autenticado.
- `app/src/main/java/com/example/emy/MobileModuleActivity.kt`: consulta una ruta suministrada y muestra su JSON.
- `app/src/main/java/com/example/emy/MobileNavigation.kt`: catálogo de módulos, normalización de roles, secciones y drawer.

### Datos, red y presentación compartida

- `app/src/main/java/com/example/emy/data/remote/ApiClient.kt`: configuración compartida de Retrofit/OkHttp y endpoints de `ApiService`.
- `app/src/main/java/com/example/emy/data/remote/ImageUrlResolver.kt`: valida y resuelve URL de imágenes, incluidas rutas relativas y hosts loopback.
- `app/src/main/java/com/example/emy/data/local/TokenManager.kt`: lectura, escritura y borrado de token/usuario en DataStore Preferences.
- `app/src/main/java/com/example/emy/ui/RemoteImage.kt`: componente Compose que carga una imagen remota con Coil.
- `app/src/main/java/com/example/emy/viewmodel/AuthUiState.kt`: estados de la interfaz de autenticación.
- `app/src/main/java/com/example/emy/viewmodel/AuthViewModel.kt`: estado y operación de login.
- `app/src/main/java/com/example/emy/viewmodel/AuthViewModelFactory.kt`: fábrica del ViewModel.
- `app/src/main/java/com/example/emy/ui/theme/Color.kt`, `Theme.kt` y `Type.kt`: paletas, esquema Material 3 y tipografía Compose.

## Recursos Android

- `app/src/main/AndroidManifest.xml`: permiso de Internet/estado de red, Activities, launcher y configuración de seguridad de red.
- `app/src/main/res/layout/activity_login2.xml`: controles del formulario XML de login.
- `app/src/main/res/layout/activity_menu.xml`: layout XML de menú existente; la interfaz actual de `MenuActivity` se construye en Compose y no utiliza este layout.
- `app/src/main/res/drawable/image_placeholder.xml`: recurso mostrado por Coil como placeholder/error.
- `app/src/main/res/values/strings.xml`: nombre de app y textos XML.
- `app/src/main/res/values/colors.xml`: colores de recursos XML; la paleta Compose está además en Kotlin.
- `app/src/main/res/values/themes.xml`: tema Android `Theme.Emy`.
- `app/src/main/res/xml/network_security_config.xml`: configuración de seguridad predeterminada; no permite tráfico HTTP cleartext.
- `app/src/debug/res/xml/network_security_config.xml`: variante de depuración, que permite cleartext para desarrollo.
- `app/src/main/res/xml/backup_rules.xml` y `data_extraction_rules.xml`: reglas/plantillas para la extracción y copia de datos.

## Pruebas

- `app/src/test/java/com/example/emy/MobileNavigationTest.kt`: prueba la selección de módulos/secciones por rol.
- `app/src/test/java/com/example/emy/data/remote/ImageUrlResolverTest.kt`: prueba casos válidos e inválidos de resolución de URL.
- `app/src/test/java/com/example/emy/ExampleUnitTest.kt`: prueba de ejemplo de suma.
- `app/src/androidTest/java/com/example/emy/ExampleInstrumentedTest.kt`: prueba de instrumentación que comprueba el identificador de paquete.

Las pruebas unitarias corren con `.\gradlew.bat :app:testDebugUnitTest`; las de instrumentación requieren un dispositivo/emulador.

## Datos de entorno que no son portátiles

- `local.properties` contiene la ruta absoluta del SDK Android de esta máquina. En otra computadora, Android Studio o el usuario debe apuntar a su propio SDK.
- `.gradle/` contiene caché y datos de Gradle. `.kotlin/` y `app/build/` contienen datos/salidas generados.
- `.idea/` contiene metadatos de Android Studio, algunos específicos del equipo. No sustituye a los archivos Gradle compartibles.

No usar una ruta local de SDK o JDK como valor universal en una guía de replicación.

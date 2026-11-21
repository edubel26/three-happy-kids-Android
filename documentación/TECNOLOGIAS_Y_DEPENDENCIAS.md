# Tecnologías y dependencias

## Plataforma y herramientas de compilación

| Tecnología/configuración | Versión o valor | Uso |
|---|---:|---|
| Kotlin | Plugin Compose `2.0.21` | Lenguaje principal del cliente Android y compilación Compose. |
| Android Gradle Plugin | `9.0.0-alpha06` | Tareas y configuración de compilación Android. Es una versión alpha. |
| Gradle Wrapper | `9.0.0` | Reproduce la versión de Gradle utilizada por el proyecto. |
| Java para Gradle | 17 o posterior | Gradle 9 requiere una JVM 17 o posterior. |
| Java source/target | 11 | Compatibilidad del código Java declarado en `compileOptions`. |
| Android compileSdk | 36 | API Android contra la que se compila. |
| Android minSdk | 24 | Versión mínima de Android declarada por la app. |
| Android targetSdk | 36 | API objetivo declarada. |
| Android namespace/applicationId | `com.example.emy` | Paquete y espacio de nombres Android. |
| Compose BOM | `2024.09.00` | Alinea versiones de librerías Compose sin declarar cada una individualmente. |

Las versiones anteriores provienen de `build.gradle.kts`, `app/build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`.

## Dependencias de ejecución

| Dependencia | Versión | Integración y propósito |
|---|---:|---|
| `androidx.core:core-ktx` | `1.15.0` | Extensiones Kotlin para APIs Android. |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.8.7` | Integración de coroutines con el ciclo de vida Android. |
| `androidx.activity:activity-compose` | `1.9.3` | Permite alojar UI Compose dentro de una Activity. |
| `androidx.compose:compose-bom` | `2024.09.00` | Plataforma de versiones para Compose. |
| `androidx.compose.ui:ui` | BOM | Componentes y fundamento de la interfaz Compose. |
| `androidx.compose.ui:ui-graphics` | BOM | APIs gráficas de Compose. |
| `androidx.compose.ui:ui-tooling-preview` | BOM | Soporte para previews de Compose en el IDE. |
| `androidx.compose.material3:material3` | BOM | Componentes Material 3, temas y controles. |
| `io.coil-kt:coil-compose` | `2.7.0` | Carga de imágenes remotas desde composables, placeholder y caché. |
| `com.squareup.retrofit2:retrofit` | `2.9.0` | Cliente HTTP declarativo para los endpoints REST. |
| `com.squareup.retrofit2:converter-gson` | `2.9.0` | Conversión de respuestas/solicitudes JSON con Gson. |
| `com.squareup.okhttp3:logging-interceptor` | `4.12.0` | Configura el registro de solicitudes HTTP; `ApiClient` lo limita a BASIC en debug y lo desactiva en release. |
| `androidx.navigation:navigation-compose` | `2.7.7` | Dependencia disponible para navegación Compose; el flujo observado usa Activities e Intents, no un NavHost. |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.7` | Integración de ViewModel y Compose. |
| `androidx.lifecycle:lifecycle-runtime-compose` | `2.8.7` | Integración del ciclo de vida con UI Compose. |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | `2.8.7` | APIs ViewModel con soporte Kotlin/coroutines. |
| `androidx.datastore:datastore-preferences` | `1.1.1` | Preferencias asíncronas para guardar el token y el JSON del usuario. |

## Dependencias de pruebas y depuración

| Dependencia | Versión | Propósito |
|---|---:|---|
| `junit:junit` | `4.13.2` | Pruebas unitarias locales JVM. |
| `androidx.test.ext:junit` | `1.3.0` | Integración de JUnit con pruebas instrumentadas Android. |
| `androidx.test.espresso:espresso-core` | `3.7.0` | Interacción/validación de interfaz en pruebas de instrumentación. |
| BOM Compose en `androidTest` | `2024.09.00` | Alinea dependencias Compose de pruebas. |
| `androidx.compose.ui:ui-test-junit4` | BOM | Pruebas de UI Compose. |
| `androidx.compose.ui:ui-tooling` | BOM, debug | Herramientas de inspección de Compose durante desarrollo. |
| `androidx.compose.ui:ui-test-manifest` | BOM, debug | Componentes de prueba Compose en variantes debug. |

## Cómo se conectan las tecnologías

1. Las Activities componen pantallas con Compose y Material 3, excepto el formulario de login, que se define en XML.
2. Retrofit y OkHttp envían solicitudes al backend. Gson interpreta JSON.
3. DataStore Preferences conserva token y usuario en el dispositivo.
4. Coil descarga y cachea imágenes; `ImageUrlResolver` prepara URL compatibles con el host configurado.
5. JUnit prueba funciones locales, y las pruebas instrumentadas requieren Android.

## Configuración inyectada en `BuildConfig`

`app/build.gradle.kts` crea estas constantes para la variante de la app:

- `BACKEND_BASE_URL`: host backend usado también para resolver imágenes.
- `API_BASE_URL`: base de Retrofit.
- `API_LOGIN_PATH`: ruta de login.
- `API_LOGIN_USER_FIELD`: clave JSON del identificador/email.
- `API_LOGIN_PASSWORD_FIELD`: clave JSON de contraseña.

Se pueden sustituir en una compilación con propiedades Gradle `-P...`; sus valores predeterminados y el alcance de cada URL se explican en [FLUJOS_DE_DATOS.md](FLUJOS_DE_DATOS.md).

## Repositorios de artefactos

`settings.gradle.kts` configura Google, Maven Central y Gradle Plugin Portal para la resolución de plugins y dependencias. La primera compilación requiere conexión a Internet para descargar el wrapper y los artefactos que no estén en caché.

## Observaciones de compatibilidad

- Android Gradle Plugin `9.0.0-alpha06` es alpha; no se debe asumir estabilidad de una versión final.
- El wrapper Gradle requiere JVM 17 o posterior. El `sourceCompatibility`/`targetCompatibility` Java 11 no rebaja el requisito de JVM de Gradle.
- `targetSdk` y `compileSdk` 36 requieren instalar la plataforma Android 36 en el SDK.
- La dependencia `navigation-compose` está declarada, pero no corresponde a la navegación entre pantallas actualmente observada.
- La configuración debug permite HTTP sin TLS; la seguridad principal para release bloquea cleartext. La URL HTTP predeterminada puede no funcionar en release.

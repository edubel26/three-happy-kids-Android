# Configuración e instalación

## Requisitos

- Windows con PowerShell, o un sistema compatible con Gradle.
- JDK 17 o posterior para ejecutar Gradle 9.
- Android SDK con la plataforma Android 36 y Build Tools 36.0.0 (la configuración de esta instalación usa esa versión).
- Android Platform Tools para `adb`.
- Acceso a Internet durante la primera compilación para obtener Gradle y dependencias.
- Opcional: Android Studio para editar, sincronizar Gradle y crear un AVD.
- Para ejecutar la aplicación: teléfono Android con depuración USB autorizada o un emulador.
- Para probar catálogo, login y módulos: backend con endpoints compatibles y una URL accesible desde el dispositivo.

El repositorio no incluye el backend ni un servicio de base de datos.

## Preparar el SDK en Windows

Instala Android Studio o el Android SDK Command-line Tools oficial. En Android Studio, usa **SDK Manager** e instala:

- Android SDK Platform 36.
- Android SDK Build-Tools 36.0.0.
- Android SDK Platform-Tools.
- Para un emulador: Android Emulator y una imagen de sistema compatible con la arquitectura del equipo.

Acepta las licencias desde SDK Manager o ejecuta desde PowerShell, sustituyendo la ruta por la instalación local:

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-17"
$env:ANDROID_HOME = "C:\ruta\a\Android\Sdk"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
```

`JAVA_HOME` y la ruta del SDK no son valores del proyecto: deben apuntar a instalaciones reales en cada equipo. En Android Studio selecciona como **Gradle JDK** un JDK 17 o posterior. No copies el `local.properties` de otra computadora.

## Backend y propiedades

Los valores predeterminados se construyen en `app/build.gradle.kts`:

| Propiedad Gradle | Predeterminado |
|---|---|
| `backendBaseUrl` | `http://10.0.2.2:3000/` |
| `apiBaseUrl` | `${backendBaseUrl}api/v1/` |
| `apiLoginPath` | `login` |
| `apiLoginUserField` | `email` |
| `apiLoginPasswordField` | `password` |

El host `10.0.2.2` es una dirección especial del Android Emulator para llegar al host PC. No sirve como dirección del backend para un teléfono físico. Desde un teléfono, usa una dirección/IP accesible desde la red del dispositivo, o el host/despliegue donde realmente se ejecute el backend. El servicio debe permitir conexiones desde ese dispositivo.

Ejemplo de compilación para un teléfono conectado a la misma red que un backend de desarrollo (reemplaza la IP por la dirección real de tu PC):

```powershell
.\gradlew.bat :app:assembleDebug `
  -PbackendBaseUrl=http://192.168.1.20:3000/ `
  -PapiBaseUrl=http://192.168.1.20:3000/api/v1/
```

Si no se establece `apiBaseUrl`, el proyecto lo deriva de `backendBaseUrl` agregando `api/v1/`. Las propiedades de login también pueden sustituirse con `-PapiLoginPath=...`, `-PapiLoginUserField=...` y `-PapiLoginPasswordField=...`. Mantén las barras finales en las URL para evitar confusión, aunque el build normaliza la barra final.

No escribas credenciales reales del backend dentro de la documentación o del código. El cliente no incluye una URL de backend pública que se pueda asumir operativa.

## Obtener el proyecto y compilar

En PowerShell:

```powershell
git clone --branch intalacion-en-movil https://github.com/edubel26/three-happy-kids-Android.git emy
Set-Location emy
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

El SDK local se configura mediante Android Studio o `local.properties` generado para ese equipo. La aplicación se compila en `app/build/outputs/apk/debug/app-debug.apk`.

## Ejecutar en un teléfono

1. Activa Opciones para desarrolladores en Android.
2. Activa **Depuración USB** y conecta el teléfono con un cable de datos.
3. Acepta en el teléfono la autorización del equipo.
4. Comprueba en una terminal:

   ```powershell
   adb devices -l
   ```

   Debe aparecer un dispositivo con estado `device`, no `unauthorized`.
5. Instala y ejecuta:

   ```powershell
   .\gradlew.bat :app:installDebug
   adb shell monkey -p com.example.emy 1
   ```

En un emulador, crea y arranca un dispositivo desde **Device Manager** de Android Studio y luego usa los mismos comandos. La virtualización del hardware puede ser requisito para un emulador rápido; la disponibilidad depende del procesador, BIOS/UEFI y sistema operativo.

## Pruebas y comprobaciones

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

El primer comando ejecuta las pruebas unitarias locales. El segundo necesita un dispositivo/emulador conectado. Para verificar instalación e inicio:

```powershell
adb shell pm path com.example.emy
adb shell monkey -p com.example.emy 1
```

La app puede abrir aunque el backend no esté disponible; las funciones que cargan información remota requieren conectividad y respuestas compatibles.

## Seguridad de red por variante

El manifest referencia `@xml/network_security_config`. La configuración principal (`app/src/main/res/xml/network_security_config.xml`) bloquea cleartext; la variante debug (`app/src/debug/res/xml/network_security_config.xml`) lo permite. Por ello, el backend HTTP predeterminado está orientado al desarrollo/debug. No supongas que una versión release podrá acceder por HTTP; el servidor debe servir una conexión compatible con la política release.

## Archivos locales y problemas frecuentes

- **Gradle JDK inválido:** selecciona un JDK instalado (17 o posterior) en Android Studio → Settings → Build Tools → Gradle.
- **SDK no encontrado:** configura el Android SDK Manager o genera un `local.properties` con la ruta del SDK de ese equipo.
- **`adb` no muestra el teléfono:** activa depuración USB, acepta la autorización y usa un cable/puerto de datos.
- **No hay productos o falla login:** revisa host, puerto, endpoints, conectividad del backend y formato JSON esperado. Esos servicios no están incluidos en el repositorio.
- **El teléfono no alcanza el backend:** no uses `10.0.2.2`; configura la dirección de red del host o un backend accesible.
- **401 aunque haya sesión local:** el código actual de `ApiClient` envía `Authorization: ******` literal, no el token persistido.
- **Release no conecta con un backend HTTP:** la política principal bloquea cleartext.

`local.properties`, `.gradle/`, `.kotlin/`, `.idea/` y `app/build/` son locales, generados o del IDE. No los uses como instrucciones portátiles ni modifiques valores locales de otra persona.

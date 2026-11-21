# Guía de replicación desde cero

Esta guía reproduce el cliente Android **emy**. El backend de tienda no está incluido: para usar el catálogo y las funciones de autenticación se necesita un servicio externo compatible con las rutas y JSON descritos en [FLUJOS_DE_DATOS.md](FLUJOS_DE_DATOS.md).

## 1. Preparar herramientas

En Windows instala:

1. Git.
2. Un JDK 17 o posterior.
3. Android Studio, o Android SDK Command-line Tools.
4. Android SDK Platform 36, Build Tools 36.0.0 y Platform Tools.
5. Opcional: Android Emulator y una imagen de Android para crear un dispositivo virtual.

En Android Studio configura el JDK 17 o posterior en **Settings → Build Tools → Gradle → Gradle JDK**. Gradle 9 requiere Java 17 aunque el código Java de la app declare compatibilidad 11.

## 2. Obtener el código

La rama publicada al momento de esta documentación es `intalacion-en-movil`:

```powershell
git clone --branch intalacion-en-movil https://github.com/edubel26/three-happy-kids-Android.git emy
Set-Location emy
```

Abre la carpeta `emy` en Android Studio. El proyecto Gradle se identifica como `emy` y contiene el módulo `:app`.

## 3. Configurar el SDK local

Instala la plataforma SDK 36 y las herramientas requeridas mediante **SDK Manager**. Para uso por terminal, define `JAVA_HOME` y `ANDROID_HOME` para esa sesión con las rutas reales de tu máquina:

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-17"
$env:ANDROID_HOME = "C:\ruta\a\Android\Sdk"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
```

`local.properties` apunta a un SDK específico de cada computadora; deja que Android Studio lo genere o configura una ruta local válida. No copies rutas de `C:\Users\...` de otro usuario ni de otra PC.

## 4. Configurar el backend

Determina un backend de desarrollo que implemente catálogo, categorías, login, consulta de sesión, tipos de documento, registro y las rutas de módulos que se necesiten. Configura una URL accesible desde el destino de ejecución.

- Android Emulator al backend que corre en el mismo PC: el valor predeterminado `10.0.2.2` puede usarse.
- Teléfono físico: usa una URL/IP accesible desde el teléfono, por ejemplo una dirección LAN real del PC. Ambos deben poder comunicarse y el firewall debe permitir el puerto del servicio.

Para compilar con URL propia:

```powershell
.\gradlew.bat :app:assembleDebug `
  -PbackendBaseUrl=http://192.168.1.20:3000/ `
  -PapiBaseUrl=http://192.168.1.20:3000/api/v1/
```

Sustituye la IP de ejemplo por la del backend real. Si la API tiene un contrato de login distinto, puedes ajustar `apiLoginPath`, `apiLoginUserField` y `apiLoginPasswordField` mediante `-P...`. La configuración de depuración permite HTTP; release bloquea cleartext en la política principal.

## 5. Sincronizar, compilar y probar

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Verifica que se genere `app/build/outputs/apk/debug/app-debug.apk`. Las pruebas cubren rutas/roles, resolución de URL, una prueba de ejemplo y el identificador de paquete; no prueban una integración real con backend.

## 6. Ejecutar en Android

### Dispositivo físico

1. Activa opciones para desarrolladores y depuración USB.
2. Conecta el dispositivo mediante un cable de datos y acepta la autorización de depuración.
3. Confirma su estado:

   ```powershell
   adb devices -l
   ```

4. Instala y abre:

   ```powershell
   .\gradlew.bat :app:installDebug
   adb shell monkey -p com.example.emy 1
   ```

### Emulador

Crea un dispositivo virtual desde Android Studio Device Manager, arráncalo y ejecuta los mismos comandos. La aceleración del emulador depende de la virtualización configurada en el equipo; sin aceleración puede no iniciar o ser extremadamente lento.

## 7. Validar funcionalidad

- La pantalla inicial debe abrir como `MainActivity`.
- Con backend accesible, deben cargarse categorías/productos y mostrarse las tarjetas.
- Confirma que las respuestas usen las claves previstas para el catálogo, `documentTypes` para el registro y `token`/`user` para login.
- Prueba el filtro de categoría, el detalle y navegación al login.
- Prueba registro/login con una cuenta de desarrollo, no con credenciales personales.
- Comprueba el menú con distintos roles y una ruta de módulo que el backend implemente.

La autenticación puede fallar aunque el backend devuelva un token: `ApiClient` envía `Authorization: ******` literal. Documenta este comportamiento al investigar el entorno; esta guía no cambia código.

## 8. Qué no copiar al replicar

No reuses `local.properties`, la caché `.gradle/`, los datos `.kotlin/`, el contenido de `.idea/` específico del IDE ni `app/build/`. Genera estos elementos localmente. La app no trae backend, catálogo local, credenciales ni archivos de imágenes de servidor.

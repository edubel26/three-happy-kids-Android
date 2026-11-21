# Funcionamiento de la aplicación

## Inicio de la app

`app/src/main/AndroidManifest.xml` declara `MainActivity` como Activity de entrada (`MAIN`/`LAUNCHER`) y exportada. Al iniciar, `MainActivity` configura el acceso a la API, presenta el catálogo público y vuelve a comprobar al reanudarse si hay un token local para actualizar el estado de sesión visible.

La interfaz principal es Compose. Ofrece navegación entre Inicio y Catálogo, búsqueda, filtro por categoría, tarjetas de producto y detalle. La API puede devolver productos con distintos nombres o envolverlos en contenedores JSON; los helpers de `MainActivity.kt` buscan claves y alias conocidos para presentar los campos disponibles.

### Catálogo e interacción con productos

1. La Activity pide categorías y productos públicos a `ApiService`.
2. Convierte la respuesta JSON a la lista que consume la UI.
3. En Inicio muestra una selección limitada (hasta seis productos); Catálogo presenta los productos disponibles.
4. El campo de texto filtra los productos de Inicio. La categoría se usa para consultar/filtrar el catálogo.
5. La tarjeta abre el detalle; se muestran precio, stock, categoría, variantes e imagen cuando están presentes.
6. El botón para comprar conduce al login si no hay sesión, pero no existe en el código una operación que agregue el artículo al carrito.

Los precios se presentan con formato regional `es-CO`. La disponibilidad y los valores concretos dependen del JSON de la API.

## Inicio de sesión

La entrada a login se abre desde la tienda. `LoginActivity2` utiliza `activity_login2.xml`, comprueba que correo y contraseña no estén vacíos y valida el formato básico del correo. La configuración de BuildConfig permite personalizar el nombre del campo de usuario y de contraseña enviados al backend.

`AuthViewModel.authenticate()` cambia el estado de UI a `Loading`, llama al endpoint configurado y espera una respuesta con un token y un objeto `user`. Un resultado correcto guarda token y usuario mediante `TokenManager` y publica `Success`; un error publica `Error` para mostrar el mensaje. La Activity observa ese estado y, si el login tuvo éxito, abre `MenuActivity`.

Al entrar o reanudar login, el código puede comprobar una sesión guardada con `GET me`; espera una respuesta con `user`, actualiza el usuario local y continúa al menú. Si no se puede validar, la sesión local puede limpiarse. Esta comprobación depende de que el backend responda y del estado de autenticación.

## Registro

`RegistrationActivity` muestra un formulario Compose. Al iniciar, solicita los tipos de documento al endpoint `tipos-documento` y espera que la respuesta exponga `documentTypes`. El formulario valida campos requeridos, correo y coincidencia de contraseñas; después manda un `POST registro` con la información de cuenta y opcionales, como teléfono o dirección.

El flujo de éxito espera que el servidor responda con `token` y `user`. Si están presentes, guarda la sesión y abre `MenuActivity`. Si la forma JSON o el contenido no coinciden con lo esperado, la UI informa un error o no puede completar el flujo.

## Menú y roles

`MenuActivity` lee de DataStore el token y el JSON de usuario. Si alguno falta o el usuario no puede interpretarse, limpia la sesión y regresa al login. Con una sesión local disponible, presenta nombre, correo, roles y las secciones permitidas.

`readRoles()` normaliza `roles` o `rol` y considera `admin` como alias de `administrador`. `sectionsFor()` calcula la navegación en el cliente. Entre las secciones observadas están Tienda, Administración, Bodega, Mensajería y Cuenta. Las entradas incluyen nombres como productos, carrito, pedidos, inventario, dashboard y entregas.

Cerrar sesión elimina la información local; no se observa una solicitud HTTP de logout.

## Pantalla de módulos

Al elegir un módulo, `MenuActivity` pasa el título y la ruta relativa mediante un Intent a `MobileModuleActivity`. Esta Activity consulta la ruta como GET y transforma objetos o listas JSON en tarjetas/texto genéricos. Algunas propiedades de la respuesta se tratan como posibles URLs de imagen. Un `401` limpia la sesión y conduce al login.

Esto permite consultar diferentes endpoints con una pantalla compartida, pero no equivale a pantallas específicas para editar, crear o eliminar registros. Varias opciones relacionadas con mapa/evidencias/novedades usan la misma ruta de entregas.

## Carga de imágenes

`MainActivity` extrae del producto uno de varios nombres conocidos para el campo de imagen. Si no encuentra una imagen, genera una ruta referencial según el identificador del producto. `resolveImageUrl()` acepta URL HTTP(S), mantiene hosts externos y completa rutas relativas con el host base del backend. Los hosts loopback en la URL recibida se sustituyen para utilizar el host configurado. No acepta valores vacíos, esquemas no HTTP(S) o rutas protocol-relative `//...`.

`RemoteImage` usa Coil y caché de memoria/disco. Ante ausencia o error muestra `image_placeholder.xml`. Para algunas imágenes de referencia se presenta un aviso/créditos desplegables con fuentes Flickr y licencias CC BY/CC BY-SA.

## Estados de UI y errores

`AuthUiState` define `Idle`, `Loading`, `Success` y `Error`. El login muestra estados de carga y mensajes; otras pantallas también presentan estados de carga/vacío/error según la solicitud. Los errores HTTP son relevantes: por ejemplo, el módulo trata de forma especial el código 401. La presentación genérica de JSON depende de las claves y formatos que el código reconoce.

## Límites funcionales que conviene conocer

- No hay servidor ni base de datos incluidos en este repositorio.
- La selección de roles es una conveniencia de presentación; no debe considerarse autorización.
- El logout borra datos locales, no revoca la sesión remotamente.
- El token se lee en `ApiClient.kt`, pero el interceptor envía el texto fijo `******` en `Authorization`, no el token. Login, validación `me` y operaciones que exijan autenticación pueden fallar aunque el backend haya entregado un token.
- No se implementa en el detalle una acción de compra o de añadir al carrito.
- Los módulos distintos del catálogo/login/registro son principalmente consultas GET con renderizado genérico.
- Hay que comprobar que el backend entregue las claves esperadas, particularmente `user`, `token` y `documentTypes`.

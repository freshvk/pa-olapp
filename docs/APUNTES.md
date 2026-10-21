# Apuntes de estudio – PañolApp

## 1. Idea general
App Android (Kotlin + Jetpack Compose) para un pañol: registrar equipos con stock, prestarlos, devolverlos y ver alertas. Todo se guarda en el teléfono.
Patrón **MVVM**: *Pantalla (View)* muestra datos y avisa acciones → *ViewModel* guarda el estado y valida → *Repository* habla con la base de datos → *SQLite* guarda.

## 2. Dependencias (`gradle/libs.versions.toml` y `app/build.gradle.kts`)
**Plugins**
- `com.android.application` (AGP 9.3.3): convierte el proyecto en una app Android (compila, empaqueta el APK). Desde AGP 9 incluye el soporte de Kotlin.
- `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.2.10): compilador de Compose; permite usar `@Composable`.

**Librerías (de tu profesora)**
- `compose-bom`: lista de versiones compatibles entre sí de todas las librerías Compose; por eso `ui`, `material3`, etc. no llevan versión.
- `ui` / `ui-graphics`: base de Compose (Modifier, Column, Row, Box, dibujo, imágenes).
- `material3`: componentes Material 3 (Scaffold, Button, Card, TextField, chips, AlertDialog, TopAppBar).
- `ui-tooling-preview`: permite `@Preview` en Android Studio. `ui-tooling` (debug): herramientas de inspección.
- `activity-compose`: `ComponentActivity.setContent {}` y `enableEdgeToEdge`.
- `core-ktx`: extensiones Kotlin para APIs de Android y `ContextCompat` (usado para registrar el receptor de batería).
- `lifecycle-runtime-ktx`: coroutines atadas al ciclo de vida de Android.
- Pruebas: `junit` (pruebas unitarias), `androidx-junit`, `espresso-core`, `ui-test-junit4`, `ui-test-manifest` (pruebas en dispositivo; vienen en la plantilla, no las usamos).

**Las 2 que agregamos**
- `lifecycle-viewmodel-compose`: función `viewModel()` para obtener un ViewModel desde un Composable. Es la pieza que hace posible MVVM en Compose.
- `navigation-compose`: `NavHost`, `composable(ruta)`, `navigate()` y argumentos entre pantallas.

**Lo que NO usamos a propósito** (para no salirnos de las dependencias de la profesora): Room (usamos SQLite nativo), Coil, librerías de permisos o de cámara (la app no usa fotos ni cámara).

**Configuración (`app/build.gradle.kts`)**: `namespace`/`applicationId` = identidad de la app; `minSdk 26` = Android mínimo (8.0); `compileSdk/targetSdk 37`; Java 11; `buildFeatures.compose = true`.

## 3. Estructura de archivos y qué hace cada pedazo
### Raíz del paquete
- `MainActivity.kt`: única Activity. Activa pantalla completa (`enableEdgeToEdge`), aplica el tema `PanolAppTheme` y muestra `AppNavegacion()`.
- `PanolApp.kt`: clase `Application` (se crea primero al abrir la app). Crea el `Contenedor`, que construye una sola vez la base de datos, la sesión y los 3 repositorios (inyección de dependencias manual). Contiene `cambios`, un contador: cada vez que algo se guarda sube y obliga a las listas a recargarse.

### `data/model/Modelos.kt` – datos puros
- `Usuario` (con `rol` y `esAdmin`), `Roles` (Administrador / Usuario), `Equipo` (con `prestadas` y `stockBajo` calculados), `Prestamo` (`activo`, `vencido()`), `EstadoPrestamo` (Activo/Devuelto), `Categorias.todas`, y resultados `ResultadoAuth` / `ResultadoOperacion` (éxito o error con mensaje).

### `data/local/` – almacenamiento
- `PanolDatabase.kt` (versión 3): `SQLiteOpenHelper`. Siembra la cuenta de administrador (admin@panol.cl / Admin123). Crea las tablas `usuarios`, `equipos`, `prestamos` (con clave foránea) la primera vez y siembra 5 equipos de ejemplo. Activa las claves foráneas.
- `SessionManager.kt`: guarda en `SharedPreferences` el id del usuario logueado; así la sesión sobrevive al cerrar la app.

### `data/repository/` – acceso a datos (corre en `Dispatchers.IO`, fuera del hilo de pantalla)
- `UsuarioRepository.kt`: registra siempre con rol Usuario (correo único, clave con **SHA-256 + sal aleatoria**), lista usuarios y cambia roles (no permite cambiar el propio), inicia sesión comparando hashes, obtiene/actualiza el usuario actual, cierra sesión.
- `EquipoRepository.kt`: lista/obtiene equipos con una consulta SQL que calcula `disponible = total − unidades en préstamos activos`; guarda (insert o update); elimina solo si no hay préstamos activos.
- `PrestamoRepository.kt`: lista préstamos con JOIN al nombre del equipo (el administrador ve todos; el usuario solo los suyos); `prestar` usa una **transacción** (revisa disponibilidad y recién inserta, evitando prestar de más); `devolver` marca Devuelto con fecha.

### `domain/`
- `Seguridad.kt`: genera la sal aleatoria y calcula el hash SHA-256 de la contraseña (lo usan el registro, el login y la creación del administrador).
- `Validaciones.kt`: reglas de cada campo (nombre, correo, teléfono, clave, confirmación, equipo, categoría, cantidades, solicitante, días). Devuelve `null` si está bien o el texto del error. Es lo que prueba `ValidacionesTest`.
- `Formatos.kt`: fecha `dd/MM/yyyy` y milisegundos por día.

### `viewmodel/` – estado y lógica de pantalla (`StateFlow`)
- `AuthViewModel.kt`: estados `LoginUiState`, `RegistroUiState`, `PerfilUiState`. Valida mientras se escribe, llama al repositorio, marca `exito`, y `consumirX()` reinicia el estado tras navegar. También guarda el `usuario` actual y cierra sesión.
- `EquipoViewModel.kt`: `inventario` combina lista + texto de búsqueda + categoría y calcula `visibles`, disponibles totales y stock bajo. Maneja el `detalle`, eliminar, y el formulario (crear/editar; `iniciarFormulario` solo inicializa una vez para no perder lo escrito al rotar la pantalla).
- `PrestamoViewModel.kt`: lista con filtro Todos/Activos/Devueltos, contadores de activos y vencidos, formulario de préstamo con validación y la devolución.

### `recursos/` – funciones del teléfono
- `Bateria.kt`: `EstadoBateria` (nivel, si carga, `baja` ≤ 20 % sin cargar), `interpretarBateria(intent)` y el composable `rememberBateria()`, que registra un `BroadcastReceiver` para `ACTION_BATTERY_CHANGED` y lo elimina al salir (`DisposableEffect`). No requiere permisos. El panel muestra el nivel con una barra y un aviso si está baja.
- `Vibracion.kt`: vibra 150 ms; usa `VibratorManager` en Android 12+ y `Vibrator` en versiones anteriores. Requiere el permiso `VIBRATE` del manifiesto.
- `Compartir.kt`: `Intent.ACTION_SEND` con selector de apps.

### `ui/theme/`
- `Color.kt` (paleta verde/azul y colores de estado), `Type.kt` (tipografías), `Theme.kt` (`PanolAppTheme`, tema Material 3 claro/oscuro).

### `ui/components/` – piezas reutilizables
- `Campos.kt`: `CampoTexto` (campo con ícono, error, teclado y ojo de contraseña), `BotonCarga` (botón con indicador de carga), `AvisoError`.
- `Tarjetas.kt`: `IconoEquipo`, `EtiquetaEstado`, `TarjetaEquipo`, `TarjetaResumen`.
- `Estructura.kt`: `PantallaConBarra` = Scaffold con barra superior, botón volver, acciones y botón flotante.

### `ui/navigation/`
- `Rutas.kt`: nombres de las rutas y sus funciones `detalle(id)` / `editar(id)`.
- `AppNavegacion.kt`: crea el `NavController`, los 3 ViewModels (compartidos), elige pantalla inicial según haya sesión, define cada `composable`, las animaciones de transición, y al cerrar sesión limpia el historial (`popUpTo(0)`).

### `ui/screen/` – 9 pantallas
`Login`, `Registro`, `Menu` (contadores, alertas, accesos), `Inventario` (búsqueda, chips, lista animada, compartir, FAB), `FormularioEquipo` (nombre, categoría, cantidades, vibración al guardar), `DetalleEquipo` (datos, préstamo, editar, eliminar con diálogo), `Prestamos` (filtros, devolución con confirmación), `Usuarios` (solo admin: lista y cambio de rol), `Perfil` (editar, ver rol y cerrar sesión).
Patrón de todas: `collectAsState()` observa el estado; los eventos llaman a funciones del ViewModel; `LaunchedEffect` reacciona a "éxito" para navegar.

### `res/` y manifiesto
- `AndroidManifest.xml`: declara la app, la Activity principal (MAIN/LAUNCHER), permiso `VIBRATE` y `android:name=".PanolApp"`.
- `strings.xml`, `themes.xml`, íconos y reglas de respaldo.

## 4. Roles (vista admin / vista usuario)
- El rol se guarda en la tabla `usuarios` y viaja en `Usuario.rol`.
- `AppNavegacion` calcula `esAdmin` y lo pasa a Menú, Inventario, Detalle y Préstamos, que muestran u ocultan botones y textos.
- Usuario: solicita préstamos con su propio nombre (`prestar(equipo, solicitanteFijo)`), no edita equipos ni devuelve.
- Administrador: todo lo anterior más equipos, devoluciones y la pantalla Usuarios.
- `cambios` se incrementa al iniciar/cerrar sesión para que las listas se recarguen con los permisos del usuario nuevo.

## 5. Conceptos clave para la defensa
- **State hoisting / flujo unidireccional**: el estado vive en el ViewModel; la UI solo lo muestra y notifica eventos.
- **StateFlow + collectAsState**: cuando el estado cambia, Compose redibuja solo lo necesario.
- **Corrutinas**: `viewModelScope.launch` y `withContext(Dispatchers.IO)` evitan bloquear la pantalla con la base de datos.
- **Persistencia**: SQLite (datos) + SharedPreferences (sesión).
- **Seguridad básica**: la contraseña nunca se guarda en texto plano.
- **Recursos nativos**: vibración, batería y compartir (sin cámara).

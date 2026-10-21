# Guion de defensa individual – PañolApp (≈5-7 min)
Debes poder explicar TODO el proyecto. Practica con el celular/emulador antes.

1. **Portada (15 s):** "Soy Alejandro Lazón, de la asignatura DSY1105. Presento PañolApp."
2. **Contexto y problema (40 s):** El pañol controla préstamos en papel; hay pérdidas y no se sabe el stock. PañolApp lo resuelve en el celular.
3. **Roles (45 s):** Administrador (encargado del pañol): gestiona equipos, registra préstamos, recibe devoluciones, administra usuarios. Usuario: ve el catálogo, solicita préstamos a su nombre y ve solo los suyos. Mencionar que el desarrollo fue individual.
4. **Mockups (40 s):** Se diseñaron 8 pantallas antes de programar; la app final sigue ese diseño.
5. **Arquitectura (60 s):** MVVM. La pantalla (Compose) observa el estado del ViewModel (StateFlow); el ViewModel valida y llama al Repository; el Repository usa SQLite. Los estados son clases `*UiState`.
6. **Paquetes (30 s):** data, domain, viewmodel, recursos, ui (theme, components, navigation, screen). Dependencias: las de la profesora + viewmodel-compose y navigation-compose.
7. **Demo (2 min), en este orden:** registro de usuario con error de validación → login como administrador (admin@panol.cl / Admin123): panel, nuevo equipo con vibración, batería en el panel (conectar/desconectar el cargador), pantalla Usuarios → cerrar sesión → login como usuario: catálogo sin edición, solicitar préstamo → volver como administrador: devolver en Préstamos y compartir inventario → cerrar la app y reabrir (persistencia).
8. **Conclusión (30 s):** Cumple los requisitos; aprendí MVVM, StateFlow y navegación; mejoras: roles, reportes, nube.

## Preguntas probables
- *¿Dónde se guardan los datos?* SQLite (`PanolDatabase`); la sesión en SharedPreferences.
- *¿Cómo se valida?* `Validaciones.kt` + estado de error por campo en el ViewModel; hay pruebas unitarias.
- *¿Qué recursos nativos usa?* Vibración (Vibrator), estado de la batería (BroadcastReceiver con ACTION_BATTERY_CHANGED) y compartir (Intent). No se usa la cámara.
- *¿Cómo se protege la contraseña?* SHA-256 con sal aleatoria por usuario.
- *¿Por qué no se puede eliminar un equipo?* Si tiene préstamos activos, para no perder el registro.

- *¿Cómo se separan las vistas?* El usuario actual tiene `rol`; las pantallas muestran u ocultan acciones con `esAdmin`, y el repositorio de préstamos filtra por usuario si no es admin.
- *¿Cómo se crea el primer administrador?* La base de datos lo siembra al crearse (admin@panol.cl); desde ahí un admin puede promover a otros.

## Antes de la defensa
Probar en el equipo que usarán, tener datos de ejemplo, repositorio público con tus commits y **no tocarlo tras entregar**.

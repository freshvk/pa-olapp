# PañolApp

Aplicación móvil Android para gestionar el inventario y los préstamos de equipos de un pañol (herramientas, instrumentos de medición, equipos de seguridad y computación). Conecta con microservicios Spring Boot propios, desplegados en AWS.

> Experiencia 3 – DSY1105 Desarrollo de Aplicaciones Móviles – Duoc UC – Equipo 17

## Integrantes

| Nombre | Aporte |
|---|---|
| Alejandro Lazón | App Android, microservicios, despliegue y documentación |

## Repositorios

- App Android: este repositorio.
- Backend (microservicios Spring Boot): https://github.com/freshvk/panolapp-backend
- Diagramas de arquitectura: rama `documents` de este repositorio (carpeta `documents/`).

## Funcionalidades

- **Dos perfiles:** Administrador y Usuario, con menús y permisos distintos.
  - Administrador: inventario completo, alta/edición/eliminación de equipos, registro de devoluciones, gestión de roles de usuarios y reportes del pañol.
  - Usuario: catálogo, solicitud de préstamos propios, "Mis préstamos" y "Mi resumen".
- **Autenticación con SQLite:** registro, inicio y cierre de sesión se resuelven localmente con `SQLiteOpenHelper` y sesión en `SharedPreferences`.
- **Conexión con microservicios** mediante Retrofit (usuarios, equipos, préstamos y reportes).
- **Formularios validados** con mensaje de error por campo (login, registro, equipo, préstamo, perfil).
- **Navegación jerárquica** con Navigation Compose y rutas protegidas por rol.
- **Animaciones:** transiciones entre pantallas, errores animados, botón con indicador de carga, lista con `animateItem`.
- **Recursos nativos:** compartir (inventario, comprobante de préstamo y reportes), vibración al registrar operaciones y estado de la batería (panel de administrador).
- **Manejo de errores de red:** mensajes claros del servidor y botón "Reintentar" cuando no hay conexión.

## Arquitectura (MVVM)

```
cl.duoc.panolapp
├── data
│   ├── api          Retrofit: ApiConfig, ClienteApi, interfaces por microservicio, Errores
│   ├── local        PanolDatabase (SQLite: usuarios) y SessionManager
│   ├── model        Modelos de dominio
│   └── repository   Usuario, Equipo, Prestamo, Reporte
├── domain           Validaciones, Formatos y Seguridad (lógica pura)
├── recursos         Compartir, Vibración, Batería
├── viewmodel        Auth, Equipo, Prestamo, Reporte (StateFlow)
└── ui
    ├── components   Campos, tarjetas, estructura
    ├── navigation   Rutas y AppNavegacion
    ├── screen       Login, Registro, Menú, Inventario, Detalle, Formulario, Préstamos, Reportes, Usuarios, Perfil
    └── theme        Tema Material 3
```

Flujo: **UI (Composable) → ViewModel (StateFlow) → Repository → SQLite / Retrofit**. La interfaz no contiene lógica de validación ni acceso a datos. Los diagramas están en la rama `documents`.

## Microservicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| usuarios-service | 8081 | Sincronización de usuarios y gestión de roles |
| equipos-service | 8082 | CRUD de equipos y control de stock |
| prestamos-service | 8083 | Préstamos y devoluciones con control de stock |
| reportes-service | 8084 | Resumen general, equipos más pedidos y resumen personal |

Spring Boot (Java 21), Firebase Firestore como base de datos, Docker y Docker Compose, desplegados en una instancia EC2 de AWS con IP elástica. El detalle está en el README del repositorio del backend.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, Navigation Compose, SQLite, Retrofit 2 con Gson y OkHttp, Spring Boot, Firebase Firestore, Docker, AWS EC2.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en Android Studio; esperar la sincronización de Gradle.
2. Crear `local.properties` con la ruta del SDK si Android Studio no lo hace.
3. En `data/api/ApiConfig.kt`, `HOST` apunta al backend desplegado en AWS. Para el emulador con un backend local usar `http://10.0.2.2`.
4. Ejecutar la configuración **app** en un emulador o dispositivo con Android 8.0 o superior.

Cuenta de administrador de prueba: `admin@panol.cl` / `Admin123`. Cualquier otro usuario se registra desde la app con rol Usuario.

## Pruebas

- App: `./gradlew testDebugUnitTest` (validaciones, roles y seguridad, batería y manejo de errores).
- Backend: pruebas JUnit en cada microservicio (`mvn test`).

## Limitaciones conocidas

- El backend confía en las cabeceras `X-Usuario` y `X-Rol` enviadas por la app (más una clave `X-API-KEY`); no usa JWT.
- El tráfico va por HTTP sin cifrar (`usesCleartextTraffic`) porque el servidor no tiene dominio ni certificado.
- Firestore no es una base relacional; la autenticación local sí usa SQLite.

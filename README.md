# PañolApp – Control de equipos y préstamos del pañol
DSY1105 Desarrollo de Aplicaciones Móviles · Duoc UC · Evaluación Parcial 2

**Integrante:** Alejandro Lazón

## Tecnología
Kotlin + Jetpack Compose + Material 3. Mismas versiones/dependencias del proyecto de la profesora
(`gradle/libs.versions.toml`, AGP 9.3.3, Kotlin 2.2.10, Compose BOM 2026.02.01), más solo dos librerías oficiales:
`lifecycle-viewmodel-compose` (MVVM) y `navigation-compose` (navegación).

## Roles
| | Administrador | Usuario |
|---|---|---|
| Panel | Contadores, alertas de stock bajo y vencidos | Contadores y sus préstamos |
| Equipos | Ver, crear, editar, eliminar | Solo ver (catálogo) |
| Préstamos | Registra a nombre de cualquier persona, ve todos, recibe devoluciones | Solicita a su nombre y ve solo los suyos |
| Usuarios | Lista y promueve/degrada roles | – |

Cuenta de administrador de prueba: **admin@panol.cl / Admin123**. Quien se registra queda como *Usuario*.

## Pantallas (9)
Login · Registro · Panel (admin/usuario) · Inventario/Catálogo · Nuevo/Editar equipo · Detalle + préstamo · Préstamos · Usuarios (admin) · Perfil

## Requisitos de la pauta
| Requisito | Dónde |
|---|---|
| Formularios validados | `domain/Validaciones.kt` + `viewmodel/*` (errores por campo en tiempo real) |
| MVVM + estado | `viewmodel/` (AndroidViewModel + StateFlow, estados `*UiState`) |
| Navegación | `ui/navigation/AppNavegacion.kt` (NavHost, transiciones, sesión persistente) |
| Persistencia local | `data/local/PanolDatabase.kt` (SQLite) + `SessionManager` (SharedPreferences) |
| Recurso nativo 1 | Vibración al guardar/prestar/devolver – `recursos/Vibracion.kt` |
| Recurso nativo 2 | Estado de la batería en tiempo real (BroadcastReceiver) en el panel – `recursos/Bateria.kt` |
| Recurso nativo extra | Compartir inventario (Intent) – `recursos/Compartir.kt` |
| Animaciones | Transiciones entre pantallas, `animateItem()` en listas |
| Roles | `Usuario.rol`, `Roles`, menús y acciones condicionadas por `esAdmin` |
| Pruebas unitarias | `app/src/test/.../ValidacionesTest.kt`, `RolesYSeguridadTest.kt` |

## Estructura de paquetes (`cl.duoc.panolapp`)
```
PanolApp.kt            Application + contenedor de dependencias
MainActivity.kt        Punto de entrada (tema + navegación)
data/model             Modelos (Usuario, Equipo, Prestamo)
data/local             SQLite y sesión
data/repository       Acceso a datos
domain                 Validaciones y formatos
viewmodel              ViewModels y estados de UI
recursos               Vibración, batería, compartir
ui/theme               Color, Type, Theme
ui/components          Componentes reutilizables
ui/navigation          Rutas y NavHost
ui/screen              Pantallas
```

## Ejecutar
Abrir en Android Studio (versión compatible con AGP 9.3.3), sincronizar Gradle y ejecutar en emulador/dispositivo (minSdk 26).
Datos de ejemplo: al primer inicio se crean 5 equipos y la cuenta de administrador. Registre un usuario normal desde la pantalla de registro.

## Plan de trabajo (Trello) – listas: Backlog · En progreso · Revisión · Hecho
Tarjetas del plan de trabajo:
1. Mockups (Figma/papel) · 2. Tema y componentes · 3. BD + repositorios · 4. Login/Registro + validaciones
5. Inventario + búsqueda/filtros · 6. Formulario de equipo · 7. Detalle + préstamo · 8. Préstamos + devolución
9. Perfil/sesión · 10. Navegación y animaciones · 11. Pruebas · 12. Presentación y defensa

## GitHub Flow
`main` siempre estable → una rama por tarjeta (`feature/login`, `feature/inventario`…) → commits pequeños →
Pull Request → merge a `main`. Todos los commits son del autor del proyecto.
**No modificar el repositorio después de la entrega.**

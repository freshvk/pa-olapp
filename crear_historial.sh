#!/usr/bin/env bash
set -e

if [ ! -f settings.gradle.kts ]; then
  echo "Ejecuta este script dentro de la carpeta PanolApp (donde está settings.gradle.kts)."
  exit 1
fi
if [ -d .git ]; then
  echo "Ya existe un repositorio Git aquí. No se hizo nada."
  exit 1
fi
if [ -z "$(git config user.name)" ] || [ -z "$(git config user.email)" ]; then
  echo "Configura tu identidad primero:"
  echo '  git config user.name "Tu Nombre"'
  echo '  git config user.email "tu_correo@ejemplo.com"'
  exit 1
fi

git init -b main >/dev/null

TRAILER_1="Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>"
TRAILER_2="Claude-Session: https://claude.ai/code/session_0196TWaFaL2MTcBBoBcNzuiL"

hacer() {
  local mensaje="$1"; shift
  git add -- "$@"
  git commit -q -m "$mensaje" -m "$TRAILER_1" -m "$TRAILER_2"
  echo "commit: $mensaje"
}

hacer "chore: agrega .gitignore del proyecto" .gitignore app/.gitignore
hacer "chore: agrega Gradle wrapper y propiedades del proyecto" gradlew gradlew.bat gradle/wrapper gradle/gradle-daemon-jvm.properties gradle.properties
hacer "build: define catalogo de versiones con las dependencias del curso" gradle/libs.versions.toml
hacer "build: configura build y settings raiz del proyecto" build.gradle.kts settings.gradle.kts
hacer "build: configura el modulo app con Compose, ViewModel y Navigation" app/build.gradle.kts app/src/main/keepRules
hacer "feat(res): agrega iconos, colores y reglas de respaldo" app/src/main/res/drawable app/src/main/res/drawable-v24 app/src/main/res/mipmap-anydpi-v26 app/src/main/res/xml app/src/main/res/values/colors.xml
hacer "feat(res): agrega textos y tema base de la aplicacion" app/src/main/res/values/strings.xml app/src/main/res/values/themes.xml
hacer "feat(data): define modelos de usuario, equipo, prestamo y roles" app/src/main/java/cl/duoc/panolapp/data/model
hacer "feat(domain): agrega validaciones de formularios" app/src/main/java/cl/duoc/panolapp/domain/Validaciones.kt
hacer "feat(domain): agrega formato de fechas" app/src/main/java/cl/duoc/panolapp/domain/Formatos.kt
hacer "feat(domain): agrega cifrado de claves con sal y SHA-256" app/src/main/java/cl/duoc/panolapp/domain/Seguridad.kt
hacer "feat(data): crea base de datos SQLite con datos iniciales" app/src/main/java/cl/duoc/panolapp/data/local/PanolDatabase.kt
hacer "feat(data): guarda la sesion del usuario en SharedPreferences" app/src/main/java/cl/duoc/panolapp/data/local/SessionManager.kt
hacer "feat(data): agrega repositorio de usuarios con registro, login y roles" app/src/main/java/cl/duoc/panolapp/data/repository/UsuarioRepository.kt
hacer "feat(data): agrega repositorio de equipos con stock disponible" app/src/main/java/cl/duoc/panolapp/data/repository/EquipoRepository.kt
hacer "feat(data): agrega repositorio de prestamos con transaccion" app/src/main/java/cl/duoc/panolapp/data/repository/PrestamoRepository.kt
hacer "feat(app): agrega Application y contenedor de dependencias" app/src/main/java/cl/duoc/panolapp/PanolApp.kt
hacer "feat(recursos): agrega vibracion como recurso nativo" app/src/main/java/cl/duoc/panolapp/recursos/Vibracion.kt
hacer "feat(recursos): agrega lectura de bateria como recurso nativo" app/src/main/java/cl/duoc/panolapp/recursos/Bateria.kt
hacer "feat(recursos): agrega compartir inventario con Intent" app/src/main/java/cl/duoc/panolapp/recursos/Compartir.kt
hacer "feat(ui): define paleta y tema Material 3" app/src/main/java/cl/duoc/panolapp/ui/theme
hacer "feat(ui): agrega componentes de formulario reutilizables" app/src/main/java/cl/duoc/panolapp/ui/components/Campos.kt
hacer "feat(ui): agrega tarjetas y etiquetas reutilizables" app/src/main/java/cl/duoc/panolapp/ui/components/Tarjetas.kt
hacer "feat(ui): agrega estructura de pantalla con barra superior" app/src/main/java/cl/duoc/panolapp/ui/components/Estructura.kt
hacer "feat(viewmodel): agrega AuthViewModel para login, registro, perfil y usuarios" app/src/main/java/cl/duoc/panolapp/viewmodel/AuthViewModel.kt
hacer "feat(viewmodel): agrega EquipoViewModel con filtros y formulario" app/src/main/java/cl/duoc/panolapp/viewmodel/EquipoViewModel.kt
hacer "feat(viewmodel): agrega PrestamoViewModel con filtros y formulario" app/src/main/java/cl/duoc/panolapp/viewmodel/PrestamoViewModel.kt
hacer "feat(ui): agrega pantalla de login" app/src/main/java/cl/duoc/panolapp/ui/screen/Login.kt
hacer "feat(ui): agrega pantalla de registro" app/src/main/java/cl/duoc/panolapp/ui/screen/Registro.kt
hacer "feat(ui): agrega panel segun rol con bateria" app/src/main/java/cl/duoc/panolapp/ui/screen/Menu.kt
hacer "feat(ui): agrega inventario con busqueda y filtros" app/src/main/java/cl/duoc/panolapp/ui/screen/Inventario.kt
hacer "feat(ui): agrega formulario para crear y editar equipos" app/src/main/java/cl/duoc/panolapp/ui/screen/FormularioEquipo.kt
hacer "feat(ui): agrega detalle de equipo y solicitud de prestamo" app/src/main/java/cl/duoc/panolapp/ui/screen/DetalleEquipo.kt
hacer "feat(ui): agrega pantalla de prestamos y devoluciones" app/src/main/java/cl/duoc/panolapp/ui/screen/Prestamos.kt
hacer "feat(ui): agrega pantalla de administracion de usuarios" app/src/main/java/cl/duoc/panolapp/ui/screen/Usuarios.kt
hacer "feat(ui): agrega pantalla de perfil" app/src/main/java/cl/duoc/panolapp/ui/screen/Perfil.kt
hacer "feat(nav): define rutas de navegacion" app/src/main/java/cl/duoc/panolapp/ui/navigation/Rutas.kt
hacer "feat(nav): agrega NavHost con transiciones y sesion persistente" app/src/main/java/cl/duoc/panolapp/ui/navigation/AppNavegacion.kt
hacer "feat(app): agrega MainActivity y manifiesto" app/src/main/java/cl/duoc/panolapp/MainActivity.kt app/src/main/AndroidManifest.xml
hacer "test: agrega pruebas unitarias de validaciones" app/src/test/java/cl/duoc/panolapp/domain/ValidacionesTest.kt
hacer "test: agrega pruebas de roles y cifrado" app/src/test/java/cl/duoc/panolapp/domain/RolesYSeguridadTest.kt
hacer "test: agrega pruebas del estado de bateria" app/src/test/java/cl/duoc/panolapp/domain/BateriaTest.kt
hacer "docs: agrega mockups de las pantallas" docs/mockups
hacer "docs: agrega README con roles, estructura y plan de trabajo" README.md
hacer "docs: agrega apuntes y guia de verificacion" docs/APUNTES.md docs/VERIFICACION.md
hacer "docs: agrega guion y presentacion de la defensa" docs/GUION_DEFENSA.md docs/PanolApp_Presentacion.pptx

git add -A
if ! git diff --cached --quiet; then
  git commit -q -m "chore: agrega archivos restantes" -m "$TRAILER_1" -m "$TRAILER_2"
fi

echo
echo "Listo. Total de commits: $(git rev-list --count HEAD)"

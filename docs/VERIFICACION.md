# Paso a paso para comprobar que todo funciona

## A. Compilación
1. Abre la carpeta `PanolApp` en Android Studio y espera que termine "Gradle sync" (sin errores rojos).
2. Menú *Build → Rebuild Project*. Debe terminar con "BUILD SUCCESSFUL".
3. Pruebas unitarias: clic derecho en la carpeta `app/src/test` → *Run Tests*. Deben pasar las 25 en verde (16 de validaciones, 6 de roles y cifrado, 3 de batería).
4. Si algo falla, copia el error de la pestaña *Build* (lo primero en rojo) y no sigas hasta resolverlo.

## B. Prueba funcional en el dispositivo (marca cada punto)
**IMPORTANTE:** si ya habías instalado una versión anterior, desinstala la app o borra sus datos (la base de datos cambió y necesita crear la cuenta de administrador).

**Primera vez (instalación limpia)**
- [ ] La app abre en **Login** (no hay sesión).
- [ ] Ingresar con campos vacíos muestra errores bajo cada campo.
- [ ] Ingresar con un correo que no existe muestra "Correo o contraseña incorrectos".

**Registro**
- [ ] "¿No tienes cuenta? Regístrate" abre Registro con transición animada.
- [ ] Correo sin `@`, teléfono inválido, clave corta y confirmación distinta muestran su error.
- [ ] Registro válido vuelve al Login. Registrar el mismo correo otra vez muestra "Este correo ya está registrado".
- [ ] Login con el usuario creado entra a "Mi panel", con "Rol: Usuario".

**Vista de USUARIO (cuenta recién registrada)**
- [ ] Panel con solo 3 accesos: Catálogo, Mis préstamos, Mi perfil. No aparece "Usuarios" ni "Nuevo equipo".
- [ ] Catálogo: lista y busca, pero no hay botón +.
- [ ] Detalle de un equipo: no hay lápiz ni papelera; el formulario dice "Solicitante: <tu nombre>" y el botón "Solicitar equipo".
- [ ] Solicitar un equipo (con un error de cantidad primero) funciona, vibra y baja el stock.
- [ ] "Mis préstamos" muestra solo el tuyo y NO tiene botón de devolución.
- [ ] Cerrar sesión.

**Vista de ADMINISTRADOR (admin@panol.cl / Admin123)**
- [ ] Título "Panel de administración" y "Rol: Administrador"; 5 accesos (Inventario, Nuevo equipo, Préstamos, Usuarios, Mi perfil).
- [ ] Muestra el préstamo del usuario en Préstamos y permite "Registrar devolución" (el stock sube).
- [ ] Usuarios: lista a todos; "Hacer administrador" cambia la etiqueta; en tu propia tarjeta no hay botón.
- [ ] Al promover al usuario de prueba y entrar con él, ve la vista de administrador (y al revés).

**Batería (ambos roles)**
- [ ] El panel muestra "Batería del dispositivo: N%" con barra de progreso, igual al nivel real del celular.
- [ ] Al conectar/desconectar el cargador, el texto se actualiza solo ("(cargando)").
- [ ] Con batería ≤ 20% y sin cargar aparece el aviso rojo (en emulador: *Extended controls → Battery* para simularlo).

**Panel (admin)**
- [ ] Muestra 5 equipos, 31 disponibles (datos de ejemplo).

**Inventario (administrador)**
- [ ] Lista los 5 equipos. Buscar "multi" deja solo el multímetro.
- [ ] Tocar un chip de categoría filtra; tocarlo de nuevo quita el filtro.
- [ ] Botón compartir abre el selector de apps con el texto del inventario.
- [ ] Botón + abre el formulario.

**Nuevo equipo**
- [ ] Guardar vacío muestra errores (nombre, categoría, cantidad).
- [ ] Stock mínimo mayor que la cantidad muestra error.
- [ ] Guardar vibra (celular real), muestra "Equipo guardado" y vuelve; el equipo aparece en la lista.

**Detalle y préstamo (administrador)**
- [ ] Tocar un equipo abre su detalle con total/disponibles/prestadas.
- [ ] Prestar con cantidad mayor a la disponible muestra error; solicitante vacío o días 0/31 también.
- [ ] Préstamo válido: vibra, muestra "Préstamo registrado" y las disponibles bajan.
- [ ] Editar (lápiz) precarga los datos; cambiar el nombre y guardar lo actualiza.
- [ ] Eliminar un equipo con préstamo activo dice que no se puede; sin préstamos, lo elimina tras confirmar.

**Préstamos**
- [ ] El préstamo aparece en "Activos" con su fecha de vencimiento.
- [ ] "Registrar devolución" → confirmar: vibra, pasa a "Devueltos" y el stock sube.
- [ ] Los filtros Todos / Activos / Devueltos cambian la lista.
- [ ] Como administrador, "Registrar préstamo" pide el nombre del solicitante.

**Perfil y sesión**
- [ ] Cambiar nombre/teléfono y guardar muestra "Perfil actualizado"; el Panel muestra el nuevo nombre.
- [ ] Cerrar el app por completo y abrirla: entra directo al Panel (sesión persistente) y los datos siguen ahí.
- [ ] "Cerrar sesión" lleva al Login y el botón atrás no vuelve al Panel.
- [ ] Rotar la pantalla en el formulario no pierde lo escrito.

## C. Antes de la defensa
- [ ] Repite la sección B completa en el equipo que usarás.
- [ ] Si quedó basura de pruebas: Ajustes del celular → Apps → PañolApp → Almacenamiento → Borrar datos (vuelve a los 5 equipos de ejemplo).
- [ ] Repositorio en GitHub público, con tus commits. No lo modifiques después de entregar.

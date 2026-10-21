package cl.duoc.panolapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.panolapp.ui.screen.PantallaDetalleEquipo
import cl.duoc.panolapp.ui.screen.PantallaFormularioEquipo
import cl.duoc.panolapp.ui.screen.PantallaInventario
import cl.duoc.panolapp.ui.screen.PantallaLogin
import cl.duoc.panolapp.ui.screen.PantallaMenu
import cl.duoc.panolapp.ui.screen.PantallaPerfil
import cl.duoc.panolapp.ui.screen.PantallaPrestamos
import cl.duoc.panolapp.ui.screen.PantallaRegistro
import cl.duoc.panolapp.ui.screen.PantallaUsuarios
import cl.duoc.panolapp.viewmodel.AuthViewModel
import cl.duoc.panolapp.viewmodel.EquipoViewModel
import cl.duoc.panolapp.viewmodel.PrestamoViewModel

@Composable
fun AppNavegacion() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val equipoVm: EquipoViewModel = viewModel()
    val prestamoVm: PrestamoViewModel = viewModel()

    val usuario by authVm.usuario.collectAsState()
    val esAdmin = usuario?.esAdmin == true

    val inicio = if (authVm.haySesion()) Rutas.MENU else Rutas.LOGIN

    NavHost(
        navController = nav,
        startDestination = inicio,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                vm = authVm,
                onIngreso = {
                    nav.navigate(Rutas.MENU) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                },
                onIrRegistro = { nav.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            PantallaRegistro(
                vm = authVm,
                onRegistrado = { nav.popBackStack() },
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.MENU) {
            PantallaMenu(
                authVm = authVm,
                equipoVm = equipoVm,
                prestamoVm = prestamoVm,
                onInventario = { nav.navigate(Rutas.INVENTARIO) },
                onNuevoEquipo = { nav.navigate(Rutas.EQUIPO_NUEVO) },
                onPrestamos = { nav.navigate(Rutas.PRESTAMOS) },
                onUsuarios = { nav.navigate(Rutas.USUARIOS) },
                onPerfil = { nav.navigate(Rutas.PERFIL) }
            )
        }
        composable(Rutas.INVENTARIO) {
            PantallaInventario(
                vm = equipoVm,
                esAdmin = esAdmin,
                onVolver = { nav.popBackStack() },
                onAbrirEquipo = { nav.navigate(Rutas.detalle(it)) },
                onNuevoEquipo = { nav.navigate(Rutas.EQUIPO_NUEVO) }
            )
        }
        composable(Rutas.EQUIPO_NUEVO) {
            PantallaFormularioEquipo(vm = equipoVm, idEquipo = null, onVolver = { nav.popBackStack() })
        }
        composable(
            route = Rutas.EQUIPO_EDITAR,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.LongType })
        ) { entrada ->
            PantallaFormularioEquipo(
                vm = equipoVm,
                idEquipo = entrada.arguments?.getLong(Rutas.ARG_ID),
                onVolver = { nav.popBackStack() }
            )
        }
        composable(
            route = Rutas.EQUIPO_DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong(Rutas.ARG_ID) ?: 0L
            PantallaDetalleEquipo(
                equipoVm = equipoVm,
                prestamoVm = prestamoVm,
                esAdmin = esAdmin,
                nombreUsuario = usuario?.nombre ?: "",
                idEquipo = id,
                onVolver = { nav.popBackStack() },
                onEditar = { nav.navigate(Rutas.editar(id)) }
            )
        }
        composable(Rutas.PRESTAMOS) {
            PantallaPrestamos(vm = prestamoVm, esAdmin = esAdmin, onVolver = { nav.popBackStack() })
        }
        composable(Rutas.USUARIOS) {
            PantallaUsuarios(vm = authVm, onVolver = { nav.popBackStack() })
        }
        composable(Rutas.PERFIL) {
            PantallaPerfil(
                vm = authVm,
                onVolver = { nav.popBackStack() },
                onCerrarSesion = {
                    nav.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}

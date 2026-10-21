package cl.duoc.panolapp.ui.navigation

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val MENU = "menu"
    const val INVENTARIO = "inventario"
    const val EQUIPO_NUEVO = "equipo_nuevo"
    const val EQUIPO_DETALLE = "equipo_detalle/{id}"
    const val EQUIPO_EDITAR = "equipo_editar/{id}"
    const val PRESTAMOS = "prestamos"
    const val PERFIL = "perfil"
    const val USUARIOS = "usuarios"

    const val ARG_ID = "id"

    fun detalle(id: Long) = "equipo_detalle/$id"
    fun editar(id: Long) = "equipo_editar/$id"
}

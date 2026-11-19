package cl.duoc.panolapp.data.api

data class UsuarioSolicitud(
    val nombre: String,
    val correo: String,
    val telefono: String
)

data class RolSolicitud(val rol: String)

data class EquipoSolicitud(
    val nombre: String,
    val categoria: String,
    val cantidadTotal: Int,
    val stockMinimo: Int
)

data class PrestamoSolicitud(
    val equipoId: Long,
    val solicitante: String,
    val cantidad: Int,
    val dias: Int
)

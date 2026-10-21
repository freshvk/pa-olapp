package cl.duoc.panolapp.data.model

data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rol: String = Roles.USUARIO
) {
    val esAdmin: Boolean get() = rol == Roles.ADMIN
}

object Roles {
    const val ADMIN = "Administrador"
    const val USUARIO = "Usuario"
}

data class Equipo(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val cantidadTotal: Int,
    val disponible: Int,
    val stockMinimo: Int
) {
    val prestadas: Int get() = cantidadTotal - disponible
    val stockBajo: Boolean get() = disponible <= stockMinimo
}

object EstadoPrestamo {
    const val ACTIVO = "Activo"
    const val DEVUELTO = "Devuelto"
}

data class Prestamo(
    val id: Long,
    val equipoId: Long,
    val equipoNombre: String,
    val solicitante: String,
    val cantidad: Int,
    val fechaPrestamo: Long,
    val fechaLimite: Long,
    val fechaDevolucion: Long?,
    val estado: String
) {
    val activo: Boolean get() = estado == EstadoPrestamo.ACTIVO
    fun vencido(ahora: Long = System.currentTimeMillis()): Boolean = activo && ahora > fechaLimite
}

object Categorias {
    val todas = listOf(
        "Herramientas eléctricas",
        "Herramientas manuales",
        "Medición",
        "Seguridad (EPP)",
        "Computación",
        "Otros"
    )
}

sealed interface ResultadoAuth {
    data class Exito(val usuarioId: Long) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

sealed interface ResultadoOperacion {
    data object Ok : ResultadoOperacion
    data class Error(val mensaje: String) : ResultadoOperacion
}

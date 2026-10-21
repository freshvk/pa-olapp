package cl.duoc.panolapp.data.repository

import android.content.ContentValues
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.local.SessionManager
import cl.duoc.panolapp.data.model.EstadoPrestamo
import cl.duoc.panolapp.data.model.Prestamo
import cl.duoc.panolapp.data.model.ResultadoOperacion
import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.domain.Formatos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PrestamoRepository(
    private val helper: PanolDatabase,
    private val sesion: SessionManager,
    private val cambios: MutableStateFlow<Int>
) {

    suspend fun listar(): List<Prestamo> = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val esAdmin = db.rawQuery(
            "SELECT rol FROM usuarios WHERE id = ?",
            arrayOf(sesion.usuarioId().toString())
        ).use { c -> c.moveToFirst() && c.getString(0) == Roles.ADMIN }
        val filtroUsuario = if (esAdmin) "" else "WHERE p.usuario_id = ${sesion.usuarioId()}"
        db.rawQuery(
            """
            SELECT p.id, p.equipo_id, e.nombre, p.solicitante, p.cantidad,
                   p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion, p.estado
            FROM prestamos p
            JOIN equipos e ON e.id = p.equipo_id
            $filtroUsuario
            ORDER BY (p.estado = ?) DESC, p.fecha_limite ASC
            """.trimIndent(),
            arrayOf(EstadoPrestamo.ACTIVO)
        ).use { c ->
            val lista = mutableListOf<Prestamo>()
            while (c.moveToNext()) {
                lista.add(
                    Prestamo(
                        id = c.getLong(0),
                        equipoId = c.getLong(1),
                        equipoNombre = c.getString(2),
                        solicitante = c.getString(3),
                        cantidad = c.getInt(4),
                        fechaPrestamo = c.getLong(5),
                        fechaLimite = c.getLong(6),
                        fechaDevolucion = if (c.isNull(7)) null else c.getLong(7),
                        estado = c.getString(8)
                    )
                )
            }
            lista
        }
    }

    suspend fun prestar(equipoId: Long, solicitante: String, cantidad: Int, dias: Int): ResultadoOperacion =
        withContext(Dispatchers.IO) {
            val db = helper.writableDatabase
            db.beginTransaction()
            val resultado: ResultadoOperacion = try {
                val disponible = db.rawQuery(
                    """
                    SELECT e.cantidad_total - COALESCE(
                        (SELECT SUM(p.cantidad) FROM prestamos p WHERE p.equipo_id = e.id AND p.estado = ?), 0)
                    FROM equipos e WHERE e.id = ?
                    """.trimIndent(),
                    arrayOf(EstadoPrestamo.ACTIVO, equipoId.toString())
                ).use { c -> if (c.moveToFirst()) c.getInt(0) else -1 }

                if (disponible < 0) {
                    ResultadoOperacion.Error("El equipo ya no existe")
                } else if (cantidad > disponible) {
                    ResultadoOperacion.Error("Solo quedan $disponible unidades disponibles")
                } else {
                    val ahora = System.currentTimeMillis()
                    val valores = ContentValues().apply {
                        put("equipo_id", equipoId)
                        put("usuario_id", sesion.usuarioId())
                        put("solicitante", solicitante.trim())
                        put("cantidad", cantidad)
                        put("fecha_prestamo", ahora)
                        put("fecha_limite", ahora + dias * Formatos.MILISEGUNDOS_POR_DIA)
                        put("estado", EstadoPrestamo.ACTIVO)
                    }
                    db.insertOrThrow("prestamos", null, valores)
                    db.setTransactionSuccessful()
                    ResultadoOperacion.Ok
                }
            } finally {
                db.endTransaction()
            }
            if (resultado is ResultadoOperacion.Ok) cambios.update { it + 1 }
            resultado
        }

    suspend fun devolver(prestamoId: Long): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("estado", EstadoPrestamo.DEVUELTO)
            put("fecha_devolucion", System.currentTimeMillis())
        }
        val filas = helper.writableDatabase.update(
            "prestamos", valores, "id = ? AND estado = ?",
            arrayOf(prestamoId.toString(), EstadoPrestamo.ACTIVO)
        )
        if (filas > 0) cambios.update { it + 1 }
        filas > 0
    }
}

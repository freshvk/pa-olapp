package cl.duoc.panolapp.data.repository

import android.content.ContentValues
import android.database.Cursor
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.model.Equipo
import cl.duoc.panolapp.data.model.EstadoPrestamo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

private const val CONSULTA_EQUIPOS = """
    SELECT e.id, e.nombre, e.categoria, e.cantidad_total, e.stock_minimo,
           e.cantidad_total - COALESCE(
               (SELECT SUM(p.cantidad) FROM prestamos p WHERE p.equipo_id = e.id AND p.estado = ?), 0
           ) AS disponible
    FROM equipos e
"""

class EquipoRepository(
    private val helper: PanolDatabase,
    private val cambios: MutableStateFlow<Int>
) {

    suspend fun listar(): List<Equipo> = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "$CONSULTA_EQUIPOS ORDER BY e.nombre COLLATE NOCASE",
            arrayOf(EstadoPrestamo.ACTIVO)
        ).use { c ->
            val lista = mutableListOf<Equipo>()
            while (c.moveToNext()) lista.add(c.aEquipo())
            lista
        }
    }

    suspend fun obtener(id: Long): Equipo? = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "$CONSULTA_EQUIPOS WHERE e.id = ?",
            arrayOf(EstadoPrestamo.ACTIVO, id.toString())
        ).use { c -> if (c.moveToFirst()) c.aEquipo() else null }
    }

    suspend fun guardar(
        id: Long?,
        nombre: String,
        categoria: String,
        cantidadTotal: Int,
        stockMinimo: Int
    ): Long = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("nombre", nombre.trim())
            put("categoria", categoria)
            put("cantidad_total", cantidadTotal)
            put("stock_minimo", stockMinimo)
        }
        val db = helper.writableDatabase
        val resultado = if (id == null) {
            db.insert("equipos", null, valores)
        } else {
            db.update("equipos", valores, "id = ?", arrayOf(id.toString()))
            id
        }
        cambios.update { it + 1 }
        resultado
    }

    suspend fun eliminar(id: Long): Boolean = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        val prestadas = db.rawQuery(
            "SELECT COUNT(*) FROM prestamos WHERE equipo_id = ? AND estado = ?",
            arrayOf(id.toString(), EstadoPrestamo.ACTIVO)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        if (prestadas > 0) return@withContext false
        db.delete("equipos", "id = ?", arrayOf(id.toString()))
        cambios.update { it + 1 }
        true
    }

    private fun Cursor.aEquipo(): Equipo = Equipo(
        id = getLong(0),
        nombre = getString(1),
        categoria = getString(2),
        cantidadTotal = getInt(3),
        stockMinimo = getInt(4),
        disponible = getInt(5)
    )
}

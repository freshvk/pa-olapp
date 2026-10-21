package cl.duoc.panolapp.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.domain.Seguridad

class PanolDatabase(context: Context) : SQLiteOpenHelper(context, NOMBRE, null, VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                telefono TEXT NOT NULL,
                rol TEXT NOT NULL,
                salt TEXT NOT NULL,
                clave TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE equipos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                categoria TEXT NOT NULL,
                cantidad_total INTEGER NOT NULL,
                stock_minimo INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE prestamos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                equipo_id INTEGER NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
                usuario_id INTEGER NOT NULL,
                solicitante TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                fecha_prestamo INTEGER NOT NULL,
                fecha_limite INTEGER NOT NULL,
                fecha_devolucion INTEGER,
                estado TEXT NOT NULL
            )
            """.trimIndent()
        )
        sembrarAdministrador(db)
        sembrarEquipos(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS prestamos")
        db.execSQL("DROP TABLE IF EXISTS equipos")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    private fun sembrarAdministrador(db: SQLiteDatabase) {
        val sal = Seguridad.nuevaSal()
        val valores = ContentValues().apply {
            put("nombre", "Administrador del Pañol")
            put("correo", CORREO_ADMIN)
            put("telefono", "+56 9 0000 0000")
            put("rol", Roles.ADMIN)
            put("salt", sal)
            put("clave", Seguridad.hash(CLAVE_ADMIN, sal))
        }
        db.insert("usuarios", null, valores)
    }

    private fun sembrarEquipos(db: SQLiteDatabase) {
        val ejemplos = listOf(
            Triple("Taladro percutor 650W", "Herramientas eléctricas", 6) to 2,
            Triple("Set de llaves combinadas", "Herramientas manuales", 10) to 3,
            Triple("Multímetro digital", "Medición", 8) to 2,
            Triple("Casco de seguridad", "Seguridad (EPP)", 15) to 4,
            Triple("Notebook de préstamo", "Computación", 5) to 1
        )
        ejemplos.forEach { (datos, minimo) ->
            val valores = ContentValues().apply {
                put("nombre", datos.first)
                put("categoria", datos.second)
                put("cantidad_total", datos.third)
                put("stock_minimo", minimo)
            }
            db.insert("equipos", null, valores)
        }
    }

    private companion object {
        const val NOMBRE = "panol.db"
        const val VERSION = 3
        const val CORREO_ADMIN = "admin@panol.cl"
        const val CLAVE_ADMIN = "Admin123"
    }
}

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
        sembrarAdministrador(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS prestamos")
            db.execSQL("DROP TABLE IF EXISTS equipos")
            db.execSQL("DROP TABLE IF EXISTS usuarios")
            onCreate(db)
        } else {
            db.execSQL("DROP TABLE IF EXISTS prestamos")
            db.execSQL("DROP TABLE IF EXISTS equipos")
        }
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

    private companion object {
        const val NOMBRE = "panol.db"
        const val VERSION = 4
        const val CORREO_ADMIN = "admin@panol.cl"
        const val CLAVE_ADMIN = "Admin123"
    }
}

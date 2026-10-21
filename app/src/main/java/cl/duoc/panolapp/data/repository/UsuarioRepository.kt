package cl.duoc.panolapp.data.repository

import android.content.ContentValues
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.local.SessionManager
import cl.duoc.panolapp.data.model.ResultadoAuth
import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.domain.Seguridad
import cl.duoc.panolapp.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val helper: PanolDatabase,
    private val sesion: SessionManager,
    private val cambios: MutableStateFlow<Int>
) {

    fun haySesion(): Boolean = sesion.haySesion()

    fun cerrarSesion() {
        sesion.cerrar()
        cambios.update { it + 1 }
    }

    suspend fun registrar(nombre: String, correo: String, telefono: String, clave: String): ResultadoAuth =
        withContext(Dispatchers.IO) {
            val db = helper.writableDatabase
            val correoNormalizado = correo.trim().lowercase()
            val existe = db.rawQuery("SELECT 1 FROM usuarios WHERE correo = ?", arrayOf(correoNormalizado))
                .use { it.moveToFirst() }
            if (existe) return@withContext ResultadoAuth.Error("Este correo ya está registrado")

            val sal = Seguridad.nuevaSal()
            val valores = ContentValues().apply {
                put("nombre", nombre.trim())
                put("correo", correoNormalizado)
                put("telefono", telefono.trim())
                put("salt", sal)
                put("rol", Roles.USUARIO)
                put("clave", Seguridad.hash(clave, sal))
            }
            val id = db.insert("usuarios", null, valores)
            if (id == -1L) {
                ResultadoAuth.Error("No se pudo crear la cuenta")
            } else {
                sesion.guardar(id)
                cambios.update { it + 1 }
                ResultadoAuth.Exito(id)
            }
        }

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth =
        withContext(Dispatchers.IO) {
            val db = helper.readableDatabase
            db.rawQuery(
                "SELECT id, salt, clave FROM usuarios WHERE correo = ?",
                arrayOf(correo.trim().lowercase())
            ).use { c ->
                if (c.moveToFirst() && c.getString(2) == Seguridad.hash(clave, c.getString(1))) {
                    val id = c.getLong(0)
                    sesion.guardar(id)
                    cambios.update { it + 1 }
                    ResultadoAuth.Exito(id)
                } else {
                    ResultadoAuth.Error("Correo o contraseña incorrectos")
                }
            }
        }

    suspend fun obtenerActual(): Usuario? = withContext(Dispatchers.IO) {
        val id = sesion.usuarioId()
        if (id <= 0) return@withContext null
        helper.readableDatabase.rawQuery(
            "SELECT id, nombre, correo, telefono, rol FROM usuarios WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) Usuario(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)) else null
        }
    }

    suspend fun actualizarPerfil(nombre: String, telefono: String) = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("nombre", nombre.trim())
            put("telefono", telefono.trim())
        }
        helper.writableDatabase.update("usuarios", valores, "id = ?", arrayOf(sesion.usuarioId().toString()))
    }

    suspend fun listar(): List<Usuario> = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "SELECT id, nombre, correo, telefono, rol FROM usuarios ORDER BY rol, nombre COLLATE NOCASE",
            null
        ).use { c ->
            val lista = mutableListOf<Usuario>()
            while (c.moveToNext()) {
                lista.add(Usuario(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)))
            }
            lista
        }
    }

    suspend fun cambiarRol(usuarioId: Long, rol: String): Boolean = withContext(Dispatchers.IO) {
        if (usuarioId == sesion.usuarioId()) return@withContext false
        val valores = ContentValues().apply { put("rol", rol) }
        val filas = helper.writableDatabase.update("usuarios", valores, "id = ?", arrayOf(usuarioId.toString()))
        if (filas > 0) cambios.update { it + 1 }
        filas > 0
    }
}

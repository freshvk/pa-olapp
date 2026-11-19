package cl.duoc.panolapp.data.repository

import android.content.ContentValues
import cl.duoc.panolapp.data.api.Carga
import cl.duoc.panolapp.data.api.RolSolicitud
import cl.duoc.panolapp.data.api.UsuarioSolicitud
import cl.duoc.panolapp.data.api.UsuariosApi
import cl.duoc.panolapp.data.api.cargarSeguro
import cl.duoc.panolapp.data.api.mensajeDeError
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.local.SessionManager
import cl.duoc.panolapp.data.model.ResultadoAuth
import cl.duoc.panolapp.data.model.ResultadoOperacion
import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.data.model.Usuario
import cl.duoc.panolapp.domain.Seguridad
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val helper: PanolDatabase,
    private val sesion: SessionManager,
    private val cambios: MutableStateFlow<Int>,
    private val api: UsuariosApi
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
                sesion.guardarIdentidad(correoNormalizado, Roles.USUARIO)
                sincronizarRemoto(id, nombre.trim(), correoNormalizado, telefono.trim(), Roles.USUARIO)
                cambios.update { it + 1 }
                ResultadoAuth.Exito(id)
            }
        }

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth =
        withContext(Dispatchers.IO) {
            val db = helper.readableDatabase
            val correoNormalizado = correo.trim().lowercase()
            val datos = db.rawQuery(
                "SELECT id, salt, clave, nombre, telefono, rol FROM usuarios WHERE correo = ?",
                arrayOf(correoNormalizado)
            ).use { c ->
                if (c.moveToFirst() && c.getString(2) == Seguridad.hash(clave, c.getString(1))) {
                    DatosLocales(c.getLong(0), c.getString(3), c.getString(4), c.getString(5))
                } else {
                    null
                }
            }
            if (datos == null) {
                ResultadoAuth.Error("Correo o contraseña incorrectos")
            } else {
                sesion.guardar(datos.id)
                sesion.guardarIdentidad(correoNormalizado, datos.rol)
                sincronizarRemoto(datos.id, datos.nombre, correoNormalizado, datos.telefono, datos.rol)
                cambios.update { it + 1 }
                ResultadoAuth.Exito(datos.id)
            }
        }

    suspend fun obtenerActual(): Usuario? = withContext(Dispatchers.IO) {
        val id = sesion.usuarioId()
        if (id <= 0) return@withContext null
        val usuario = helper.readableDatabase.rawQuery(
            "SELECT id, nombre, correo, telefono, rol FROM usuarios WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) Usuario(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)) else null
        }
        if (usuario != null) {
            val cambio = sesion.correo() != usuario.correo || sesion.rol() != usuario.rol
            sesion.guardarIdentidad(usuario.correo, usuario.rol)
            if (cambio) cambios.update { it + 1 }
        }
        usuario
    }

    suspend fun actualizarPerfil(nombre: String, telefono: String) = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("nombre", nombre.trim())
            put("telefono", telefono.trim())
        }
        helper.writableDatabase.update("usuarios", valores, "id = ?", arrayOf(sesion.usuarioId().toString()))
        sincronizarRemoto(sesion.usuarioId(), nombre.trim(), sesion.correo(), telefono.trim(), sesion.rol())
    }

    suspend fun listar(): Carga<List<Usuario>> = cargarSeguro { api.listar() }

    suspend fun cambiarRol(usuarioId: Long, rol: String): ResultadoOperacion =
        try {
            api.cambiarRol(usuarioId, RolSolicitud(rol))
            ResultadoOperacion.Ok
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(mensajeDeError(e))
        }

    private suspend fun sincronizarRemoto(
        usuarioId: Long,
        nombre: String,
        correo: String,
        telefono: String,
        rolLocal: String
    ) {
        val remoto = try {
            api.sincronizar(UsuarioSolicitud(nombre, correo, telefono))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: return
        if (remoto.rol != rolLocal) {
            val valores = ContentValues().apply { put("rol", remoto.rol) }
            helper.writableDatabase.update("usuarios", valores, "id = ?", arrayOf(usuarioId.toString()))
            sesion.guardarIdentidad(correo, remoto.rol)
        }
    }

    private data class DatosLocales(val id: Long, val nombre: String, val telefono: String, val rol: String)
}

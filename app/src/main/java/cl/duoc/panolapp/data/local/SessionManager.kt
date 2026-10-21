package cl.duoc.panolapp.data.local

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun usuarioId(): Long = prefs.getLong(CLAVE_USUARIO, -1L)

    fun haySesion(): Boolean = usuarioId() > 0

    fun guardar(usuarioId: Long) {
        prefs.edit().putLong(CLAVE_USUARIO, usuarioId).apply()
    }

    fun cerrar() {
        prefs.edit().remove(CLAVE_USUARIO).apply()
    }

    private companion object {
        const val CLAVE_USUARIO = "usuario_id"
    }
}

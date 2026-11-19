package cl.duoc.panolapp.data.local

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun usuarioId(): Long = prefs.getLong(CLAVE_USUARIO, -1L)

    fun haySesion(): Boolean = usuarioId() > 0

    fun correo(): String = prefs.getString(CLAVE_CORREO, "") ?: ""

    fun rol(): String = prefs.getString(CLAVE_ROL, "") ?: ""

    fun guardar(usuarioId: Long) {
        prefs.edit().putLong(CLAVE_USUARIO, usuarioId).apply()
    }

    fun guardarIdentidad(correo: String, rol: String) {
        prefs.edit().putString(CLAVE_CORREO, correo).putString(CLAVE_ROL, rol).apply()
    }

    fun cerrar() {
        prefs.edit()
            .remove(CLAVE_USUARIO)
            .remove(CLAVE_CORREO)
            .remove(CLAVE_ROL)
            .apply()
    }

    private companion object {
        const val CLAVE_USUARIO = "usuario_id"
        const val CLAVE_CORREO = "usuario_correo"
        const val CLAVE_ROL = "usuario_rol"
    }
}

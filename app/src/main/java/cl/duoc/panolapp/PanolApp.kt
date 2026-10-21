package cl.duoc.panolapp

import android.app.Application
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.local.SessionManager
import cl.duoc.panolapp.data.repository.EquipoRepository
import cl.duoc.panolapp.data.repository.PrestamoRepository
import cl.duoc.panolapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow

class Contenedor(application: Application) {
    private val base = PanolDatabase(application)
    private val sesion = SessionManager(application)

    val cambios = MutableStateFlow(0)

    val usuarios = UsuarioRepository(base, sesion, cambios)
    val equipos = EquipoRepository(base, cambios)
    val prestamos = PrestamoRepository(base, sesion, cambios)
}

class PanolApp : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }
}

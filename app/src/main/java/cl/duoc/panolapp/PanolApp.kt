package cl.duoc.panolapp

import android.app.Application
import cl.duoc.panolapp.data.api.ClienteApi
import cl.duoc.panolapp.data.local.PanolDatabase
import cl.duoc.panolapp.data.local.SessionManager
import cl.duoc.panolapp.data.repository.EquipoRepository
import cl.duoc.panolapp.data.repository.PrestamoRepository
import cl.duoc.panolapp.data.repository.ReporteRepository
import cl.duoc.panolapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow

class Contenedor(application: Application) {
    private val base = PanolDatabase(application)
    private val sesion = SessionManager(application)

    val cambios = MutableStateFlow(0)

    private val clientes = ClienteApi(sesion)

    val usuarios = UsuarioRepository(base, sesion, cambios, clientes.usuarios)
    val equipos = EquipoRepository(clientes.equipos, cambios)
    val prestamos = PrestamoRepository(clientes.prestamos, cambios)
    val reportes = ReporteRepository(clientes.reportes)
}

class PanolApp : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }
}

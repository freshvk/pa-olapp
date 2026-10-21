package cl.duoc.panolapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.panolapp.PanolApp
import cl.duoc.panolapp.data.model.Equipo
import cl.duoc.panolapp.data.model.Prestamo
import cl.duoc.panolapp.data.model.ResultadoOperacion
import cl.duoc.panolapp.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroPrestamo(val etiqueta: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    DEVUELTOS("Devueltos")
}

data class PrestamosUiState(
    val prestamos: List<Prestamo> = emptyList(),
    val filtro: FiltroPrestamo = FiltroPrestamo.ACTIVOS
) {
    val visibles: List<Prestamo>
        get() = when (filtro) {
            FiltroPrestamo.TODOS -> prestamos
            FiltroPrestamo.ACTIVOS -> prestamos.filter { it.activo }
            FiltroPrestamo.DEVUELTOS -> prestamos.filter { !it.activo }
        }

    val activos: Int get() = prestamos.count { it.activo }
    val vencidos: Int get() = prestamos.count { it.vencido() }
}

data class FormularioPrestamoUiState(
    val solicitante: String = "",
    val cantidad: String = "1",
    val dias: String = "3",
    val errorSolicitante: String? = null,
    val errorCantidad: String? = null,
    val errorDias: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val registrado: Boolean = false
)

class PrestamoViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = getApplication<PanolApp>().contenedor
    private val repositorio = contenedor.prestamos

    private val filtro = MutableStateFlow(FiltroPrestamo.ACTIVOS)
    private val todos = contenedor.cambios.map { repositorio.listar() }

    val estado: StateFlow<PrestamosUiState> = combine(todos, filtro) { lista, f ->
        PrestamosUiState(prestamos = lista, filtro = f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrestamosUiState())

    fun onFiltro(valor: FiltroPrestamo) { filtro.value = valor }

    fun devolver(prestamo: Prestamo, alTerminar: (Boolean) -> Unit) {
        viewModelScope.launch { alTerminar(repositorio.devolver(prestamo.id)) }
    }

    private val _formulario = MutableStateFlow(FormularioPrestamoUiState())
    val formulario: StateFlow<FormularioPrestamoUiState> = _formulario.asStateFlow()

    fun reiniciarFormulario() { _formulario.value = FormularioPrestamoUiState() }

    fun onSolicitante(valor: String) = _formulario.update {
        it.copy(solicitante = valor, errorSolicitante = Validaciones.solicitante(valor), errorGeneral = null)
    }

    fun onCantidad(valor: String, equipo: Equipo) = _formulario.update {
        it.copy(cantidad = valor, errorCantidad = Validaciones.cantidadPrestamo(valor, equipo.disponible), errorGeneral = null)
    }

    fun onDias(valor: String) = _formulario.update {
        it.copy(dias = valor, errorDias = Validaciones.diasPrestamo(valor), errorGeneral = null)
    }

    fun prestar(equipo: Equipo, solicitanteFijo: String? = null) {
        val actual = _formulario.value.let { if (solicitanteFijo != null) it.copy(solicitante = solicitanteFijo) else it }
        val errorSolicitante = Validaciones.solicitante(actual.solicitante)
        val errorCantidad = Validaciones.cantidadPrestamo(actual.cantidad, equipo.disponible)
        val errorDias = Validaciones.diasPrestamo(actual.dias)
        if (errorSolicitante != null || errorCantidad != null || errorDias != null) {
            _formulario.update {
                it.copy(errorSolicitante = errorSolicitante, errorCantidad = errorCantidad, errorDias = errorDias)
            }
            return
        }
        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val resultado = repositorio.prestar(
                equipoId = equipo.id,
                solicitante = actual.solicitante,
                cantidad = actual.cantidad.trim().toInt(),
                dias = actual.dias.trim().toInt()
            )
            when (resultado) {
                ResultadoOperacion.Ok -> _formulario.update { it.copy(guardando = false, registrado = true) }
                is ResultadoOperacion.Error -> _formulario.update {
                    it.copy(guardando = false, errorGeneral = resultado.mensaje)
                }
            }
        }
    }

    fun consumirRegistro() { _formulario.value = FormularioPrestamoUiState() }
}

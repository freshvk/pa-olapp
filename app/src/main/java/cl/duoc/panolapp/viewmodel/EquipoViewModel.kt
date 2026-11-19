package cl.duoc.panolapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.panolapp.PanolApp
import cl.duoc.panolapp.data.api.cargarSeguro
import cl.duoc.panolapp.data.api.Carga
import cl.duoc.panolapp.data.model.Equipo
import cl.duoc.panolapp.data.model.ResultadoOperacion
import cl.duoc.panolapp.domain.Validaciones
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventarioUiState(
    val equipos: List<Equipo> = emptyList(),
    val consulta: String = "",
    val categoria: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    val visibles: List<Equipo>
        get() = equipos.filter { e ->
            (categoria == null || e.categoria == categoria) &&
                (consulta.isBlank() || e.nombre.contains(consulta.trim(), ignoreCase = true))
        }

    val totalDisponibles: Int get() = equipos.sumOf { it.disponible }
    val conStockBajo: Int get() = equipos.count { it.stockBajo }
}

data class FormularioEquipoUiState(
    val id: Long? = null,
    val nombre: String = "",
    val categoria: String? = null,
    val cantidad: String = "",
    val stockMinimo: String = "1",
    val unidadesPrestadas: Int = 0,
    val errorNombre: String? = null,
    val errorCategoria: String? = null,
    val errorCantidad: String? = null,
    val errorStockMinimo: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = id != null
}

class EquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = getApplication<PanolApp>().contenedor
    private val repositorio = contenedor.equipos

    private val consulta = MutableStateFlow("")
    private val categoriaFiltro = MutableStateFlow<String?>(null)

    private val todos: Flow<Carga<List<Equipo>>> = contenedor.cambios.map { cargarSeguro { repositorio.listar() } }

    val inventario: StateFlow<InventarioUiState> = combine(todos, consulta, categoriaFiltro) { carga, q, c ->
        InventarioUiState(
            equipos = carga.datos.orEmpty(),
            consulta = q,
            categoria = c,
            cargando = false,
            error = carga.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventarioUiState())

    fun recargar() { contenedor.cambios.update { it + 1 } }

    fun onConsulta(valor: String) { consulta.value = valor }

    fun onCategoriaFiltro(categoria: String) {
        categoriaFiltro.value = if (categoriaFiltro.value == categoria) null else categoria
    }

    private val _detalle = MutableStateFlow<Equipo?>(null)
    val detalle: StateFlow<Equipo?> = _detalle.asStateFlow()
    private val _errorDetalle = MutableStateFlow<String?>(null)
    val errorDetalle: StateFlow<String?> = _errorDetalle.asStateFlow()
    private var trabajoDetalle: Job? = null

    fun cargarDetalle(id: Long) {
        trabajoDetalle?.cancel()
        _detalle.value = null
        _errorDetalle.value = null
        trabajoDetalle = viewModelScope.launch {
            contenedor.cambios.collect {
                val carga = cargarSeguro { repositorio.obtener(id) }
                _detalle.value = carga.datos
                _errorDetalle.value = carga.error
            }
        }
    }

    fun eliminar(equipo: Equipo, alTerminar: (ResultadoOperacion) -> Unit) {
        viewModelScope.launch { alTerminar(repositorio.eliminar(equipo.id)) }
    }

    private val _formulario = MutableStateFlow(FormularioEquipoUiState())
    val formulario: StateFlow<FormularioEquipoUiState> = _formulario.asStateFlow()
    private var formularioPara: Long? = null
    private var formularioListo = false

    fun iniciarFormulario(id: Long?) {
        if (formularioListo && formularioPara == id) return
        formularioListo = true
        formularioPara = id
        if (id == null) {
            _formulario.value = FormularioEquipoUiState()
            return
        }
        viewModelScope.launch {
            val carga = cargarSeguro { repositorio.obtener(id) }
            if (carga.error != null) {
                _formulario.update { it.copy(errorGeneral = carga.error) }
                return@launch
            }
            val equipo = carga.datos ?: return@launch
            _formulario.value = FormularioEquipoUiState(
                id = equipo.id,
                nombre = equipo.nombre,
                categoria = equipo.categoria,
                cantidad = equipo.cantidadTotal.toString(),
                stockMinimo = equipo.stockMinimo.toString(),
                unidadesPrestadas = equipo.prestadas
            )
        }
    }

    fun onNombre(valor: String) = _formulario.update {
        it.copy(nombre = valor, errorNombre = Validaciones.nombreEquipo(valor))
    }

    fun onCategoria(valor: String) = _formulario.update { it.copy(categoria = valor, errorCategoria = null) }

    fun onCantidad(valor: String) = _formulario.update {
        it.copy(
            cantidad = valor,
            errorCantidad = Validaciones.cantidadTotal(valor, maxOf(1, it.unidadesPrestadas)),
            errorStockMinimo = if (it.stockMinimo.isNotEmpty())
                Validaciones.stockMinimo(it.stockMinimo, valor.trim().toIntOrNull()) else null
        )
    }

    fun onStockMinimo(valor: String) = _formulario.update {
        it.copy(stockMinimo = valor, errorStockMinimo = Validaciones.stockMinimo(valor, it.cantidad.trim().toIntOrNull()))
    }

    fun guardar() {
        val actual = _formulario.value
        val errorNombre = Validaciones.nombreEquipo(actual.nombre)
        val errorCategoria = Validaciones.categoria(actual.categoria)
        val errorCantidad = Validaciones.cantidadTotal(actual.cantidad, maxOf(1, actual.unidadesPrestadas))
        val errorStockMinimo = Validaciones.stockMinimo(actual.stockMinimo, actual.cantidad.trim().toIntOrNull())
        if (listOf(errorNombre, errorCategoria, errorCantidad, errorStockMinimo).any { it != null }) {
            _formulario.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorCategoria = errorCategoria,
                    errorCantidad = errorCantidad,
                    errorStockMinimo = errorStockMinimo
                )
            }
            return
        }
        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val resultado = repositorio.guardar(
                id = actual.id,
                nombre = actual.nombre,
                categoria = actual.categoria!!,
                cantidadTotal = actual.cantidad.trim().toInt(),
                stockMinimo = actual.stockMinimo.trim().toInt()
            )
            when (resultado) {
                ResultadoOperacion.Ok -> _formulario.update { it.copy(guardando = false, guardado = true) }
                is ResultadoOperacion.Error -> _formulario.update {
                    it.copy(guardando = false, errorGeneral = resultado.mensaje)
                }
            }
        }
    }

    fun consumirGuardado() {
        formularioListo = false
        formularioPara = null
        _formulario.value = FormularioEquipoUiState()
    }
}

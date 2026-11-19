package cl.duoc.panolapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.panolapp.PanolApp
import cl.duoc.panolapp.data.api.cargarSeguro
import cl.duoc.panolapp.data.model.MiResumen
import cl.duoc.panolapp.data.model.ResumenGeneral
import cl.duoc.panolapp.data.model.TopEquipo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportesUiState(
    val resumen: ResumenGeneral? = null,
    val top: List<TopEquipo> = emptyList(),
    val mio: MiResumen? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = getApplication<PanolApp>().contenedor.reportes

    private val _estado = MutableStateFlow(ReportesUiState())
    val estado: StateFlow<ReportesUiState> = _estado.asStateFlow()

    fun cargarReportes(esAdmin: Boolean) {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            if (esAdmin) {
                val resumen = cargarSeguro { repositorio.resumen() }
                val top = cargarSeguro { repositorio.topEquipos(5) }
                _estado.value = ReportesUiState(
                    resumen = resumen.datos,
                    top = top.datos.orEmpty(),
                    error = resumen.error ?: top.error
                )
            } else {
                val mio = cargarSeguro { repositorio.miResumen() }
                _estado.value = ReportesUiState(mio = mio.datos, error = mio.error)
            }
        }
    }
}

package cl.duoc.panolapp.data.repository

import cl.duoc.panolapp.data.api.PrestamoSolicitud
import cl.duoc.panolapp.data.api.PrestamosApi
import cl.duoc.panolapp.data.api.mensajeDeError
import cl.duoc.panolapp.data.model.Prestamo
import cl.duoc.panolapp.data.model.ResultadoOperacion
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PrestamoRepository(
    private val api: PrestamosApi,
    private val cambios: MutableStateFlow<Int>
) {

    suspend fun listar(): List<Prestamo> = api.listar()

    suspend fun prestar(equipoId: Long, solicitante: String, cantidad: Int, dias: Int): ResultadoOperacion =
        try {
            api.prestar(PrestamoSolicitud(equipoId, solicitante.trim(), cantidad, dias))
            cambios.update { it + 1 }
            ResultadoOperacion.Ok
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(mensajeDeError(e))
        }

    suspend fun devolver(prestamoId: Long): ResultadoOperacion =
        try {
            api.devolver(prestamoId)
            cambios.update { it + 1 }
            ResultadoOperacion.Ok
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(mensajeDeError(e))
        }
}

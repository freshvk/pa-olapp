package cl.duoc.panolapp.data.repository

import cl.duoc.panolapp.data.api.EquipoSolicitud
import cl.duoc.panolapp.data.api.EquiposApi
import cl.duoc.panolapp.data.api.mensajeDeError
import cl.duoc.panolapp.data.model.Equipo
import cl.duoc.panolapp.data.model.ResultadoOperacion
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException

class EquipoRepository(
    private val api: EquiposApi,
    private val cambios: MutableStateFlow<Int>
) {

    suspend fun listar(): List<Equipo> = api.listar()

    suspend fun obtener(id: Long): Equipo? =
        try {
            api.obtener(id)
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }

    suspend fun guardar(
        id: Long?,
        nombre: String,
        categoria: String,
        cantidadTotal: Int,
        stockMinimo: Int
    ): ResultadoOperacion =
        try {
            val datos = EquipoSolicitud(nombre.trim(), categoria, cantidadTotal, stockMinimo)
            if (id == null) api.crear(datos) else api.actualizar(id, datos)
            cambios.update { it + 1 }
            ResultadoOperacion.Ok
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(mensajeDeError(e))
        }

    suspend fun eliminar(id: Long): ResultadoOperacion =
        try {
            api.eliminar(id)
            cambios.update { it + 1 }
            ResultadoOperacion.Ok
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(mensajeDeError(e))
        }
}

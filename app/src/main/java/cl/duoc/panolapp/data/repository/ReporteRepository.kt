package cl.duoc.panolapp.data.repository

import cl.duoc.panolapp.data.api.ReportesApi
import cl.duoc.panolapp.data.model.MiResumen
import cl.duoc.panolapp.data.model.ResumenGeneral
import cl.duoc.panolapp.data.model.TopEquipo

class ReporteRepository(private val api: ReportesApi) {

    suspend fun resumen(): ResumenGeneral = api.resumen()

    suspend fun topEquipos(limite: Int): List<TopEquipo> = api.topEquipos(limite)

    suspend fun miResumen(): MiResumen = api.miResumen()
}

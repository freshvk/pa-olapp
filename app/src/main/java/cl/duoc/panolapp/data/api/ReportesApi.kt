package cl.duoc.panolapp.data.api

import cl.duoc.panolapp.data.model.MiResumen
import cl.duoc.panolapp.data.model.ResumenGeneral
import cl.duoc.panolapp.data.model.TopEquipo
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportesApi {

    @GET("api/reportes/resumen")
    suspend fun resumen(): ResumenGeneral

    @GET("api/reportes/top-equipos")
    suspend fun topEquipos(@Query("limite") limite: Int): List<TopEquipo>

    @GET("api/reportes/mi-resumen")
    suspend fun miResumen(): MiResumen
}

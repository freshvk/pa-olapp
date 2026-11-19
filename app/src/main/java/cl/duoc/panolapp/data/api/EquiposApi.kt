package cl.duoc.panolapp.data.api

import cl.duoc.panolapp.data.model.Equipo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EquiposApi {

    @GET("api/equipos")
    suspend fun listar(): List<Equipo>

    @GET("api/equipos/{id}")
    suspend fun obtener(@Path("id") id: Long): Equipo

    @POST("api/equipos")
    suspend fun crear(@Body datos: EquipoSolicitud): Equipo

    @PUT("api/equipos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: EquipoSolicitud): Equipo

    @DELETE("api/equipos/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}

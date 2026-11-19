package cl.duoc.panolapp.data.api

import cl.duoc.panolapp.data.model.Prestamo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PrestamosApi {

    @POST("api/prestamos")
    suspend fun prestar(@Body datos: PrestamoSolicitud): Prestamo

    @GET("api/prestamos")
    suspend fun listar(): List<Prestamo>

    @PUT("api/prestamos/{id}/devolucion")
    suspend fun devolver(@Path("id") id: Long): Prestamo
}

package cl.duoc.panolapp.data.api

import cl.duoc.panolapp.data.model.Usuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuariosApi {

    @POST("api/usuarios/sincronizar")
    suspend fun sincronizar(@Body datos: UsuarioSolicitud): Usuario

    @GET("api/usuarios")
    suspend fun listar(): List<Usuario>

    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRol(@Path("id") id: Long, @Body rol: RolSolicitud): Usuario
}

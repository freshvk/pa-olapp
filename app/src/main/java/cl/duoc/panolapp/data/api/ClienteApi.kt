package cl.duoc.panolapp.data.api

import cl.duoc.panolapp.data.local.SessionManager
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClienteApi(private val sesion: SessionManager) {

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_CONEXION_SEGUNDOS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_LECTURA_SEGUNDOS, TimeUnit.SECONDS)
        .addInterceptor { cadena ->
            val solicitud = cadena.request().newBuilder()
                .header("X-API-KEY", ApiConfig.API_KEY)
                .header("X-Usuario", sesion.correo())
                .header("X-Rol", sesion.rol())
                .build()
            cadena.proceed(solicitud)
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private fun <T> crear(puerto: Int, servicio: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(ApiConfig.url(puerto))
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(servicio)

    val usuarios: UsuariosApi by lazy { crear(ApiConfig.PUERTO_USUARIOS, UsuariosApi::class.java) }
    val equipos: EquiposApi by lazy { crear(ApiConfig.PUERTO_EQUIPOS, EquiposApi::class.java) }
    val prestamos: PrestamosApi by lazy { crear(ApiConfig.PUERTO_PRESTAMOS, PrestamosApi::class.java) }
    val reportes: ReportesApi by lazy { crear(ApiConfig.PUERTO_REPORTES, ReportesApi::class.java) }
}

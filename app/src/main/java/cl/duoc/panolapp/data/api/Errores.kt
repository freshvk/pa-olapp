package cl.duoc.panolapp.data.api

import com.google.gson.JsonParser
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

data class Carga<T>(val datos: T? = null, val error: String? = null)

suspend fun <T> cargarSeguro(bloque: suspend () -> T): Carga<T> =
    try {
        Carga(datos = bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Carga(error = mensajeDeError(e))
    }

fun mensajeDeError(e: Throwable): String = when (e) {
    is HttpException -> mensajeHttp(e)
    is SocketTimeoutException -> "El servidor tardó demasiado en responder"
    is UnknownHostException -> "No se pudo conectar con el servidor"
    is ConnectException -> "No se pudo conectar con el servidor"
    is IOException -> "Sin conexión con el servidor"
    else -> e.message ?: "Ocurrió un error inesperado"
}

private fun mensajeHttp(e: HttpException): String {
    val cuerpo = try {
        e.response()?.errorBody()?.string()
    } catch (ex: Exception) {
        null
    }
    val mensaje = cuerpo?.let {
        try {
            JsonParser.parseString(it).asJsonObject.get("mensaje")?.asString
        } catch (ex: Exception) {
            null
        }
    }
    return mensaje ?: when (e.code()) {
        401 -> "Debes iniciar sesión nuevamente"
        403 -> "No tienes permiso para realizar esta acción"
        404 -> "No se encontró lo solicitado"
        else -> "Error del servidor (${e.code()})"
    }
}

package cl.duoc.panolapp

import cl.duoc.panolapp.data.api.cargarSeguro
import cl.duoc.panolapp.data.api.mensajeDeError
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ErroresTest {

    private fun http(codigo: Int, cuerpo: String) =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun usaElMensajeDelServidor() {
        val e = http(409, """{"mensaje":"Solo quedan 2 unidades disponibles"}""")
        assertEquals("Solo quedan 2 unidades disponibles", mensajeDeError(e))
    }

    @Test
    fun prohibidoSinCuerpoUsaMensajePorDefecto() {
        assertEquals("No tienes permiso para realizar esta acción", mensajeDeError(http(403, "")))
    }

    @Test
    fun noAutenticadoPideIniciarSesion() {
        assertEquals("Debes iniciar sesión nuevamente", mensajeDeError(http(401, "")))
    }

    @Test
    fun errorDeRedTieneMensajeAmigable() {
        assertEquals("No se pudo conectar con el servidor", mensajeDeError(UnknownHostException()))
        assertEquals("El servidor tardó demasiado en responder", mensajeDeError(SocketTimeoutException()))
    }

    @Test
    fun cargarSeguroCapturaExcepciones() = runBlocking {
        val fallo = cargarSeguro<Int> { throw UnknownHostException() }
        assertNull(fallo.datos)
        assertNotNull(fallo.error)
        val ok = cargarSeguro { 5 }
        assertEquals(5, ok.datos)
        assertNull(ok.error)
    }
}

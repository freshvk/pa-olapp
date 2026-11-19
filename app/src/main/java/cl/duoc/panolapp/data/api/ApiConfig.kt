package cl.duoc.panolapp.data.api

object ApiConfig {
    const val HOST = "http://localhost"
    const val API_KEY = "panol-equipo17-2026"

    const val PUERTO_USUARIOS = 8081
    const val PUERTO_EQUIPOS = 8082
    const val PUERTO_PRESTAMOS = 8083
    const val PUERTO_REPORTES = 8084

    const val TIMEOUT_CONEXION_SEGUNDOS = 6L
    const val TIMEOUT_LECTURA_SEGUNDOS = 15L

    fun url(puerto: Int): String = "$HOST:$puerto/"
}

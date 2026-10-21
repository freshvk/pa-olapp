package cl.duoc.panolapp.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatos {
    fun fecha(milisegundos: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")).format(Date(milisegundos))

    const val MILISEGUNDOS_POR_DIA = 86_400_000L
}

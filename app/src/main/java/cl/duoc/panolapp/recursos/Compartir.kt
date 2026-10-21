package cl.duoc.panolapp.recursos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

fun compartirTexto(context: Context, asunto: String, texto: String) {
    val intento = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    try {
        context.startActivity(Intent.createChooser(intento, asunto))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una app para compartir", Toast.LENGTH_SHORT).show()
    }
}

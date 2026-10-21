package cl.duoc.panolapp.recursos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class EstadoBateria(val nivel: Int, val cargando: Boolean) {
    val baja: Boolean get() = nivel <= UMBRAL_BAJO && !cargando

    companion object {
        const val UMBRAL_BAJO = 20
    }
}

fun interpretarBateria(intento: Intent?): EstadoBateria? {
    if (intento == null) return null
    val nivel = intento.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val escala = intento.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (nivel < 0 || escala <= 0) return null
    val estado = intento.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val cargando = estado == BatteryManager.BATTERY_STATUS_CHARGING || estado == BatteryManager.BATTERY_STATUS_FULL
    return EstadoBateria(nivel * 100 / escala, cargando)
}

@Composable
fun rememberBateria(): EstadoBateria? {
    val context = LocalContext.current
    var estado by remember { mutableStateOf<EstadoBateria?>(null) }

    DisposableEffect(context) {
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intento: Intent?) {
                estado = interpretarBateria(intento)
            }
        }
        val inicial = ContextCompat.registerReceiver(
            context,
            receptor,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        estado = interpretarBateria(inicial)
        onDispose { context.unregisterReceiver(receptor) }
    }
    return estado
}

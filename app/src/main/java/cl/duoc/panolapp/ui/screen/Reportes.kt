package cl.duoc.panolapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.data.model.MiResumen
import cl.duoc.panolapp.data.model.ResumenGeneral
import cl.duoc.panolapp.data.model.TopEquipo
import cl.duoc.panolapp.recursos.compartirTexto
import cl.duoc.panolapp.ui.components.AvisoReintento
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.viewmodel.ReporteViewModel

@Composable
fun PantallaReportes(vm: ReporteViewModel, esAdmin: Boolean, onVolver: () -> Unit) {
    val estado by vm.estado.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(esAdmin) { vm.cargarReportes(esAdmin) }

    PantallaConBarra(
        titulo = if (esAdmin) "Reportes" else "Mi resumen",
        onVolver = onVolver,
        acciones = {
            val resumen = estado.resumen
            val mio = estado.mio
            if (resumen != null || mio != null) {
                IconButton(onClick = {
                    val texto = if (resumen != null) textoReporte(resumen, estado.top) else textoMiResumen(mio!!)
                    compartirTexto(context, "Reporte del pañol", texto)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir reporte")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvisoReintento(estado.error, { vm.cargarReportes(esAdmin) })

            estado.resumen?.let { r ->
                Fila("Equipos registrados", r.totalEquipos)
                Fila("Unidades en total", r.totalUnidades)
                Fila("Unidades disponibles", r.disponibles)
                Fila("Unidades prestadas", r.prestadas)
                Fila("Préstamos activos", r.prestamosActivos)
                Fila("Préstamos vencidos", r.prestamosVencidos)
                Fila("Equipos con stock bajo", r.equiposStockBajo)

                Text("Equipos más pedidos", style = MaterialTheme.typography.titleMedium)
                if (estado.top.isEmpty()) {
                    Text(
                        "Aún no hay préstamos registrados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maximo = estado.top.maxOf { it.prestamos }.coerceAtLeast(1)
                    estado.top.forEach { equipo -> FilaTop(equipo, maximo) }
                }
            }

            estado.mio?.let { m ->
                Fila("Préstamos activos", m.activos)
                Fila("Préstamos vencidos", m.vencidos)
                Fila("Préstamos devueltos", m.devueltos)
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
            Text("$valor", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FilaTop(equipo: TopEquipo, maximo: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(equipo.equipoNombre)
            Text("${equipo.prestamos} préstamo(s) · ${equipo.unidades} u.")
        }
        LinearProgressIndicator(
            progress = { equipo.prestamos / maximo.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun textoReporte(r: ResumenGeneral, top: List<TopEquipo>): String =
    "Reporte del pañol\n" +
        "Equipos: ${r.totalEquipos}\n" +
        "Unidades: ${r.totalUnidades} (disponibles ${r.disponibles}, prestadas ${r.prestadas})\n" +
        "Préstamos activos: ${r.prestamosActivos}, vencidos: ${r.prestamosVencidos}\n" +
        "Equipos con stock bajo: ${r.equiposStockBajo}" +
        top.joinToString(separator = "") { "\n• ${it.equipoNombre}: ${it.prestamos} préstamo(s)" }

private fun textoMiResumen(m: MiResumen): String =
    "Mi resumen del pañol\nActivos: ${m.activos}\nVencidos: ${m.vencidos}\nDevueltos: ${m.devueltos}"

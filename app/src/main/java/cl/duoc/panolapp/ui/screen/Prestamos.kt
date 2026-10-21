package cl.duoc.panolapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.data.model.Prestamo
import cl.duoc.panolapp.domain.Formatos
import cl.duoc.panolapp.recursos.vibrar
import cl.duoc.panolapp.ui.components.EtiquetaEstado
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.ui.theme.EstadoAviso
import cl.duoc.panolapp.ui.theme.EstadoNeutro
import cl.duoc.panolapp.ui.theme.EstadoOk
import cl.duoc.panolapp.viewmodel.FiltroPrestamo
import cl.duoc.panolapp.viewmodel.PrestamoViewModel

@Composable
fun PantallaPrestamos(vm: PrestamoViewModel, esAdmin: Boolean, onVolver: () -> Unit) {
    val estado by vm.estado.collectAsState()
    val context = LocalContext.current
    var porDevolver by remember { mutableStateOf<Prestamo?>(null) }

    PantallaConBarra(titulo = if (esAdmin) "Préstamos" else "Mis préstamos", onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                FiltroPrestamo.entries.forEach { filtro ->
                    FilterChip(
                        selected = estado.filtro == filtro,
                        onClick = { vm.onFiltro(filtro) },
                        label = { Text(filtro.etiqueta) }
                    )
                }
            }

            val visibles = estado.visibles
            if (visibles.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No hay préstamos en esta lista.\nSolicita uno desde el detalle de un equipo.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(visibles, key = { it.id }) { prestamo ->
                        TarjetaPrestamo(
                            prestamo = prestamo,
                            puedeDevolver = esAdmin,
                            onDevolver = { porDevolver = prestamo },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        porDevolver?.let { prestamo ->
            AlertDialog(
                onDismissRequest = { porDevolver = null },
                title = { Text("Registrar devolución") },
                text = { Text("¿${prestamo.solicitante} devolvió ${prestamo.cantidad} x ${prestamo.equipoNombre}?") },
                confirmButton = {
                    TextButton(onClick = {
                        porDevolver = null
                        vm.devolver(prestamo) { ok ->
                            if (ok) {
                                vibrar(context)
                                Toast.makeText(context, "Devolución registrada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("Confirmar") }
                },
                dismissButton = { TextButton(onClick = { porDevolver = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun TarjetaPrestamo(prestamo: Prestamo, puedeDevolver: Boolean, onDevolver: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(prestamo.equipoNombre, style = MaterialTheme.typography.titleMedium)
                when {
                    prestamo.vencido() -> EtiquetaEstado("Vencido", EstadoAviso)
                    prestamo.activo -> EtiquetaEstado("Activo", EstadoOk)
                    else -> EtiquetaEstado("Devuelto", EstadoNeutro)
                }
            }
            Text("${prestamo.cantidad} unidad(es) · ${prestamo.solicitante}")
            Text(
                "Prestado: ${Formatos.fecha(prestamo.fechaPrestamo)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (prestamo.activo) {
                Text(
                    "Vence: ${Formatos.fecha(prestamo.fechaLimite)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (puedeDevolver) {
                    TextButton(onClick = onDevolver, modifier = Modifier.align(Alignment.End)) {
                        Text("Registrar devolución")
                    }
                }
            } else {
                prestamo.fechaDevolucion?.let {
                    Text("Devuelto: ${Formatos.fecha(it)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

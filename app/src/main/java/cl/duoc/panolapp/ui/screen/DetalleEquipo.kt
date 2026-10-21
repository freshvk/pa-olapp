package cl.duoc.panolapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.recursos.vibrar
import cl.duoc.panolapp.ui.components.AvisoError
import cl.duoc.panolapp.ui.components.BotonCarga
import cl.duoc.panolapp.ui.components.CampoTexto
import cl.duoc.panolapp.ui.components.IconoEquipo
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.ui.theme.EstadoAviso
import cl.duoc.panolapp.ui.theme.EstadoOk
import cl.duoc.panolapp.ui.components.EtiquetaEstado
import cl.duoc.panolapp.viewmodel.EquipoViewModel
import cl.duoc.panolapp.viewmodel.PrestamoViewModel

@Composable
fun PantallaDetalleEquipo(
    equipoVm: EquipoViewModel,
    prestamoVm: PrestamoViewModel,
    esAdmin: Boolean,
    nombreUsuario: String,
    idEquipo: Long,
    onVolver: () -> Unit,
    onEditar: () -> Unit
) {
    val equipo by equipoVm.detalle.collectAsState()
    val form by prestamoVm.formulario.collectAsState()
    val context = LocalContext.current
    var confirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(idEquipo) {
        equipoVm.cargarDetalle(idEquipo)
        prestamoVm.reiniciarFormulario()
    }

    LaunchedEffect(form.registrado) {
        if (form.registrado) {
            vibrar(context)
            Toast.makeText(context, "Préstamo registrado", Toast.LENGTH_SHORT).show()
            prestamoVm.consumirRegistro()
        }
    }

    PantallaConBarra(
        titulo = equipo?.nombre ?: "Equipo",
        onVolver = onVolver,
        acciones = {
            if (esAdmin) {
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = { confirmarEliminar = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    ) { padding ->
        val e = equipo
        if (e == null) {
            Text("Cargando...", modifier = Modifier.padding(padding).padding(24.dp))
            return@PantallaConBarra
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconoEquipo(Modifier.fillMaxWidth().height(200.dp))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaEstado(e.categoria, MaterialTheme.colorScheme.primary)
                if (e.stockBajo) EtiquetaEstado("Stock bajo", EstadoAviso)
                else EtiquetaEstado("Stock OK", EstadoOk)
            }
            Spacer(Modifier.height(8.dp))
            Text("Total: ${e.cantidadTotal}  ·  Disponibles: ${e.disponible}  ·  Prestadas: ${e.prestadas}")
            Text(
                "Alerta bajo ${e.stockMinimo} unidades",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text(if (esAdmin) "Registrar préstamo" else "Solicitar préstamo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (e.disponible == 0) {
                Text("No quedan unidades disponibles.", color = MaterialTheme.colorScheme.error)
            } else {
                if (esAdmin) {
                    CampoTexto(
                        valor = form.solicitante,
                        onCambio = prestamoVm::onSolicitante,
                        etiqueta = "Solicitante",
                        icono = Icons.Default.Person,
                        error = form.errorSolicitante
                    )
                } else {
                    Text("Solicitante: $nombreUsuario", style = MaterialTheme.typography.bodyMedium)
                }
                CampoTexto(
                    valor = form.cantidad,
                    onCambio = { prestamoVm.onCantidad(it, e) },
                    etiqueta = "Cantidad (máx. ${e.disponible})",
                    icono = Icons.Default.Edit,
                    error = form.errorCantidad,
                    teclado = KeyboardType.Number
                )
                CampoTexto(
                    valor = form.dias,
                    onCambio = prestamoVm::onDias,
                    etiqueta = "Días de préstamo",
                    icono = Icons.Default.DateRange,
                    error = form.errorDias,
                    teclado = KeyboardType.Number,
                    accion = ImeAction.Done,
                    alTerminar = { prestamoVm.prestar(e, if (esAdmin) null else nombreUsuario) }
                )
                AvisoError(form.errorGeneral)
                Spacer(Modifier.height(8.dp))
                BotonCarga(
                    texto = if (esAdmin) "Prestar equipo" else "Solicitar equipo",
                    cargando = form.guardando,
                    onClick = { prestamoVm.prestar(e, if (esAdmin) null else nombreUsuario) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        if (confirmarEliminar) {
            AlertDialog(
                onDismissRequest = { confirmarEliminar = false },
                title = { Text("Eliminar equipo") },
                text = { Text("¿Eliminar \"${e.nombre}\"? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmarEliminar = false
                        equipoVm.eliminar(e) { ok ->
                            if (ok) onVolver()
                            else Toast.makeText(
                                context,
                                "No se puede eliminar: tiene préstamos activos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

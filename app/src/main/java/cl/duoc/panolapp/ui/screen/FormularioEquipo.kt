package cl.duoc.panolapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.data.model.Categorias
import cl.duoc.panolapp.recursos.vibrar
import cl.duoc.panolapp.ui.components.BotonCarga
import cl.duoc.panolapp.ui.components.CampoTexto
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.viewmodel.EquipoViewModel

@Composable
fun PantallaFormularioEquipo(
    vm: EquipoViewModel,
    idEquipo: Long?,
    onVolver: () -> Unit
) {
    val estado by vm.formulario.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(idEquipo) { vm.iniciarFormulario(idEquipo) }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            vibrar(context)
            Toast.makeText(context, "Equipo guardado", Toast.LENGTH_SHORT).show()
            vm.consumirGuardado()
            onVolver()
        }
    }

    PantallaConBarra(
        titulo = if (estado.esEdicion) "Editar equipo" else "Nuevo equipo",
        onVolver = onVolver
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {

            CampoTexto(
                valor = estado.nombre,
                onCambio = vm::onNombre,
                etiqueta = "Nombre del equipo",
                icono = Icons.Default.Build,
                error = estado.errorNombre
            )

            Text("Categoría", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categorias.todas.forEach { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = { vm.onCategoria(categoria) },
                        label = { Text(categoria) }
                    )
                }
            }
            estado.errorCategoria?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))

            CampoTexto(
                valor = estado.cantidad,
                onCambio = vm::onCantidad,
                etiqueta = "Cantidad total",
                icono = Icons.Default.Edit,
                error = estado.errorCantidad,
                teclado = KeyboardType.Number
            )
            CampoTexto(
                valor = estado.stockMinimo,
                onCambio = vm::onStockMinimo,
                etiqueta = "Stock mínimo (alerta)",
                icono = Icons.Default.Info,
                error = estado.errorStockMinimo,
                teclado = KeyboardType.Number,
                accion = ImeAction.Done,
                alTerminar = vm::guardar
            )

            Spacer(Modifier.height(16.dp))
            BotonCarga("Guardar equipo", estado.guardando, vm::guardar)
            Spacer(Modifier.height(24.dp))
        }
    }
}

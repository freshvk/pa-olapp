package cl.duoc.panolapp.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.data.model.Categorias
import cl.duoc.panolapp.data.model.Equipo
import cl.duoc.panolapp.recursos.compartirTexto
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.ui.components.TarjetaEquipo
import cl.duoc.panolapp.viewmodel.EquipoViewModel

@Composable
fun PantallaInventario(
    vm: EquipoViewModel,
    esAdmin: Boolean,
    onVolver: () -> Unit,
    onAbrirEquipo: (Long) -> Unit,
    onNuevoEquipo: () -> Unit
) {
    val estado by vm.inventario.collectAsState()
    val context = LocalContext.current

    PantallaConBarra(
        titulo = if (esAdmin) "Inventario" else "Catálogo",
        onVolver = onVolver,
        acciones = {
            IconButton(onClick = {
                compartirTexto(context, "Inventario del pañol", textoInventario(estado.visibles))
            }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir inventario")
            }
        },
        botonFlotante = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = onNuevoEquipo,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo equipo")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = estado.consulta,
                onValueChange = vm::onConsulta,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar equipo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categorias.todas.forEach { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = { vm.onCategoriaFiltro(categoria) },
                        label = { Text(categoria) }
                    )
                }
            }

            val visibles = estado.visibles
            if (visibles.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (estado.equipos.isEmpty()) "Aún no hay equipos registrados."
                        else "Ningún equipo coincide con tu búsqueda.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(visibles, key = { it.id }) { equipo ->
                        TarjetaEquipo(
                            equipo = equipo,
                            onClick = { onAbrirEquipo(equipo.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

private fun textoInventario(equipos: List<Equipo>): String =
    if (equipos.isEmpty()) "Inventario vacío"
    else equipos.joinToString(separator = "\n", prefix = "Inventario del pañol:\n") {
        "• ${it.nombre}: ${it.disponible} disponibles de ${it.cantidadTotal}"
    }

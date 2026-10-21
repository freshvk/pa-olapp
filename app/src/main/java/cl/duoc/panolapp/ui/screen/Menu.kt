package cl.duoc.panolapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.recursos.EstadoBateria
import cl.duoc.panolapp.recursos.rememberBateria
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.ui.components.TarjetaResumen
import cl.duoc.panolapp.viewmodel.AuthViewModel
import cl.duoc.panolapp.viewmodel.EquipoViewModel
import cl.duoc.panolapp.viewmodel.PrestamoViewModel

@Composable
fun PantallaMenu(
    authVm: AuthViewModel,
    equipoVm: EquipoViewModel,
    prestamoVm: PrestamoViewModel,
    onInventario: () -> Unit,
    onNuevoEquipo: () -> Unit,
    onPrestamos: () -> Unit,
    onUsuarios: () -> Unit,
    onPerfil: () -> Unit
) {
    val usuario by authVm.usuario.collectAsState()
    val inventario by equipoVm.inventario.collectAsState()
    val prestamos by prestamoVm.estado.collectAsState()

    LaunchedEffect(Unit) { authVm.cargarUsuario() }

    val esAdmin = usuario?.esAdmin == true
    val bateria = rememberBateria()

    PantallaConBarra(titulo = if (esAdmin) "Panel de administración" else "Mi panel") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Hola, ${usuario?.nombre?.substringBefore(' ') ?: ""}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Rol: ${usuario?.rol ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaResumen(inventario.equipos.size, "Equipos", Modifier.weight(1f))
                TarjetaResumen(inventario.totalDisponibles, "Disponibles", Modifier.weight(1f))
                TarjetaResumen(prestamos.activos, if (esAdmin) "Prestados" else "Mis préstamos", Modifier.weight(1f))
            }

            bateria?.let { TarjetaBateria(it) }

            if (prestamos.vencidos > 0) {
                AvisoMenu("Hay ${prestamos.vencidos} préstamo(s) vencido(s). Revísalos en Préstamos.", onPrestamos)
            }
            if (esAdmin && inventario.conStockBajo > 0) {
                AvisoMenu("${inventario.conStockBajo} equipo(s) con stock bajo.", onInventario)
            }

            if (esAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaMenu("Inventario", Icons.Default.Build, onInventario, Modifier.weight(1f))
                    TarjetaMenu("Nuevo equipo", Icons.Default.AddCircle, onNuevoEquipo, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaMenu("Préstamos", Icons.Default.DateRange, onPrestamos, Modifier.weight(1f))
                    TarjetaMenu("Usuarios", Icons.Default.Person, onUsuarios, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaMenu("Mi perfil", Icons.Default.Person, onPerfil, Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaMenu("Catálogo", Icons.Default.Build, onInventario, Modifier.weight(1f))
                    TarjetaMenu("Mis préstamos", Icons.Default.DateRange, onPrestamos, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaMenu("Mi perfil", Icons.Default.Person, onPerfil, Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TarjetaMenu(titulo: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(titulo, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun AvisoMenu(mensaje: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Text(mensaje, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
private fun TarjetaBateria(bateria: EstadoBateria) {
    val color = if (bateria.baja) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Batería del dispositivo: ${bateria.nivel}%" + if (bateria.cargando) " (cargando)" else "",
                style = MaterialTheme.typography.titleSmall
            )
            LinearProgressIndicator(
                progress = { bateria.nivel / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
            if (bateria.baja) {
                Text(
                    "Batería baja: conecta el cargador para no perder el registro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

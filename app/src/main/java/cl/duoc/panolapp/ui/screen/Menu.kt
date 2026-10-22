package cl.duoc.panolapp.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Scaffold
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
    val esAdmin = usuario?.esAdmin == true
    val bateria = rememberBateria()

    LaunchedEffect(Unit) { authVm.cargarUsuario() }

    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (esAdmin) "PANEL DE ADMINISTRACIÓN" else "MI PANEL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Hola, ${usuario?.nombre?.substringBefore(' ') ?: ""}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            (usuario?.rol ?: "").uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Indicador(inventario.equipos.size, "equipos")
                        Indicador(inventario.totalDisponibles, "disponibles")
                        Indicador(prestamos.activos, if (esAdmin) "prestados" else "mis préstamos")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bateria?.let { TarjetaBateria(it) }

                if (prestamos.vencidos > 0) {
                    AvisoMenu("Hay ${prestamos.vencidos} préstamo(s) vencido(s).", onPrestamos)
                }
                if (esAdmin && inventario.conStockBajo > 0) {
                    AvisoMenu("${inventario.conStockBajo} equipo(s) con stock bajo.", onInventario)
                }

                if (esAdmin) {
                    FilaMenu("Inventario", "Consulta y filtra el stock", Icons.Default.Build, onInventario)
                    FilaMenu("Nuevo equipo", "Registra una herramienta o equipo", Icons.Default.AddCircle, onNuevoEquipo)
                    FilaMenu("Préstamos", "Salidas, vencimientos y devoluciones", Icons.Default.DateRange, onPrestamos)
                    FilaMenu("Usuarios", "Administra roles y accesos", Icons.Default.Person, onUsuarios)
                } else {
                    FilaMenu("Catálogo", "Revisa qué equipos hay disponibles", Icons.Default.Build, onInventario)
                    FilaMenu("Mis préstamos", "Tus solicitudes y vencimientos", Icons.Default.DateRange, onPrestamos)
                }
                FilaMenu("Mi perfil", "Tus datos y cierre de sesión", Icons.Default.Person, onPerfil)
            }
        }
    }
}

@Composable
private fun Indicador(valor: Int, etiqueta: String) {
    Column {
        AnimatedContent(targetState = valor, label = "indicador") { numero ->
            Text(
                "$numero",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun FilaMenu(titulo: String, descripcion: String, icono: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AvisoMenu(mensaje: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
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
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Batería del dispositivo: ${bateria.nivel}%" + if (bateria.cargando) " (cargando)" else "",
                style = MaterialTheme.typography.titleMedium
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

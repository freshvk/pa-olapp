package cl.duoc.panolapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.data.model.Usuario
import cl.duoc.panolapp.ui.components.EtiquetaEstado
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.ui.theme.EstadoNeutro
import cl.duoc.panolapp.ui.theme.EstadoOk
import cl.duoc.panolapp.viewmodel.AuthViewModel

@Composable
fun PantallaUsuarios(vm: AuthViewModel, onVolver: () -> Unit) {
    val usuarios by vm.usuarios.collectAsState()
    val actual by vm.usuario.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.cargarUsuarios() }

    PantallaConBarra(titulo = "Usuarios", onVolver = onVolver) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(usuarios, key = { it.id }) { usuario ->
                TarjetaUsuario(
                    usuario = usuario,
                    esActual = usuario.id == actual?.id,
                    onCambiarRol = {
                        vm.cambiarRol(usuario) { ok ->
                            val mensaje = if (ok) "Rol actualizado" else "No puedes cambiar tu propio rol"
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(usuario: Usuario, esActual: Boolean, onCambiarRol: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(usuario.rol, if (usuario.esAdmin) EstadoOk else EstadoNeutro)
            }
            Text(usuario.correo, style = MaterialTheme.typography.bodyMedium)
            Text(usuario.telefono, style = MaterialTheme.typography.bodySmall)
            if (!esActual) {
                TextButton(onClick = onCambiarRol, modifier = Modifier.align(Alignment.End)) {
                    Text(if (usuario.esAdmin) "Quitar permisos de administrador" else "Hacer administrador")
                }
            }
        }
    }
}

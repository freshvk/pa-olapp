package cl.duoc.panolapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import cl.duoc.panolapp.ui.components.BotonCarga
import cl.duoc.panolapp.ui.components.CampoTexto
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.viewmodel.AuthViewModel

@Composable
fun PantallaPerfil(vm: AuthViewModel, onVolver: () -> Unit, onCerrarSesion: () -> Unit) {
    val usuario by vm.usuario.collectAsState()
    val estado by vm.perfil.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(usuario?.id) { vm.prepararPerfil(usuario) }
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
    }

    PantallaConBarra(titulo = "Mi perfil", onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Rol: ${usuario?.rol ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                usuario?.correo ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            CampoTexto(
                valor = estado.nombre,
                onCambio = vm::onPerfilNombre,
                etiqueta = "Nombre completo",
                icono = Icons.Default.Person,
                error = estado.errorNombre
            )
            CampoTexto(
                valor = estado.telefono,
                onCambio = vm::onPerfilTelefono,
                etiqueta = "Teléfono",
                icono = Icons.Default.Phone,
                error = estado.errorTelefono,
                teclado = KeyboardType.Phone,
                accion = ImeAction.Done,
                alTerminar = vm::guardarPerfil
            )
            Spacer(Modifier.height(16.dp))
            BotonCarga("Guardar cambios", false, vm::guardarPerfil)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    vm.cerrarSesion()
                    onCerrarSesion()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }
        }
    }
}

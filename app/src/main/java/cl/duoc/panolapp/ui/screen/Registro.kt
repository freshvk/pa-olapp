package cl.duoc.panolapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.ui.components.AvisoError
import cl.duoc.panolapp.ui.components.BotonCarga
import cl.duoc.panolapp.ui.components.CampoTexto
import cl.duoc.panolapp.ui.components.PantallaConBarra
import cl.duoc.panolapp.viewmodel.AuthViewModel

@Composable
fun PantallaRegistro(
    vm: AuthViewModel,
    onRegistrado: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by vm.registro.collectAsState()

    LaunchedEffect(estado.exito) {
        if (estado.exito) {
            vm.consumirRegistro()
            onRegistrado()
        }
    }

    PantallaConBarra(titulo = "Crear cuenta", onVolver = onVolver) { padding ->
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
                onCambio = vm::onRegistroNombre,
                etiqueta = "Nombre completo",
                icono = Icons.Default.Person,
                error = estado.errorNombre
            )
            CampoTexto(
                valor = estado.correo,
                onCambio = vm::onRegistroCorreo,
                etiqueta = "Correo institucional",
                icono = Icons.Default.Email,
                error = estado.errorCorreo,
                teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = estado.telefono,
                onCambio = vm::onRegistroTelefono,
                etiqueta = "Teléfono de contacto",
                icono = Icons.Default.Phone,
                error = estado.errorTelefono,
                teclado = KeyboardType.Phone
            )
            CampoTexto(
                valor = estado.clave,
                onCambio = vm::onRegistroClave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                error = estado.errorClave,
                esClave = true
            )
            CampoTexto(
                valor = estado.confirmacion,
                onCambio = vm::onRegistroConfirmacion,
                etiqueta = "Confirmar contraseña",
                icono = Icons.Default.Lock,
                error = estado.errorConfirmacion,
                esClave = true,
                accion = ImeAction.Done,
                alTerminar = vm::registrar
            )
            AvisoError(estado.errorGeneral)
            Spacer(Modifier.height(16.dp))
            BotonCarga("Registrarme", estado.cargando, vm::registrar)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onVolver) { Text("¿Ya tienes cuenta? Inicia sesión") }
        }
    }
}

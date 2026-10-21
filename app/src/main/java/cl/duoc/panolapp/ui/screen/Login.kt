package cl.duoc.panolapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.panolapp.ui.components.AvisoError
import cl.duoc.panolapp.ui.components.BotonCarga
import cl.duoc.panolapp.ui.components.CampoTexto
import cl.duoc.panolapp.viewmodel.AuthViewModel

@Composable
fun PantallaLogin(
    vm: AuthViewModel,
    onIngreso: () -> Unit,
    onIrRegistro: () -> Unit
) {
    val estado by vm.login.collectAsState()

    LaunchedEffect(estado.exito) {
        if (estado.exito) {
            vm.consumirLogin()
            onIngreso()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("PañolApp", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "Control de equipos y préstamos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            CampoTexto(
                valor = estado.correo,
                onCambio = vm::onLoginCorreo,
                etiqueta = "Correo institucional",
                icono = Icons.Default.Email,
                error = estado.errorCorreo,
                teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = estado.clave,
                onCambio = vm::onLoginClave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                error = estado.errorClave,
                esClave = true,
                accion = ImeAction.Done,
                alTerminar = vm::iniciarSesion
            )
            AvisoError(estado.errorGeneral)
            Spacer(Modifier.height(16.dp))
            BotonCarga("Ingresar", estado.cargando, vm::iniciarSesion)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onIrRegistro) { Text("¿No tienes cuenta? Regístrate") }
        }
    }
}

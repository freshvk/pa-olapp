package cl.duoc.panolapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.panolapp.PanolApp
import cl.duoc.panolapp.data.model.ResultadoAuth
import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.data.model.Usuario
import cl.duoc.panolapp.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val exito: Boolean = false
)

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorClave: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val exito: Boolean = false
)

data class PerfilUiState(
    val nombre: String = "",
    val telefono: String = "",
    val errorNombre: String? = null,
    val errorTelefono: String? = null,
    val guardado: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = getApplication<PanolApp>().contenedor.usuarios

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro.asStateFlow()

    private val _perfil = MutableStateFlow(PerfilUiState())
    val perfil: StateFlow<PerfilUiState> = _perfil.asStateFlow()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    fun haySesion(): Boolean = repositorio.haySesion()

    fun cargarUsuarios() {
        viewModelScope.launch { _usuarios.value = repositorio.listar() }
    }

    fun cambiarRol(objetivo: Usuario, alTerminar: (Boolean) -> Unit) {
        val nuevoRol = if (objetivo.esAdmin) Roles.USUARIO else Roles.ADMIN
        viewModelScope.launch {
            val ok = repositorio.cambiarRol(objetivo.id, nuevoRol)
            if (ok) _usuarios.value = repositorio.listar()
            alTerminar(ok)
        }
    }

    fun cargarUsuario() {
        viewModelScope.launch { _usuario.value = repositorio.obtenerActual() }
    }

    fun onLoginCorreo(valor: String) = _login.update {
        it.copy(correo = valor, errorCorreo = Validaciones.correo(valor), errorGeneral = null)
    }

    fun onLoginClave(valor: String) = _login.update {
        it.copy(
            clave = valor,
            errorClave = if (valor.isBlank()) "La contraseña es obligatoria" else null,
            errorGeneral = null
        )
    }

    fun iniciarSesion() {
        val actual = _login.value
        val errorCorreo = Validaciones.correo(actual.correo)
        val errorClave = if (actual.clave.isBlank()) "La contraseña es obligatoria" else null
        if (errorCorreo != null || errorClave != null) {
            _login.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave) }
            return
        }
        _login.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val resultado = repositorio.iniciarSesion(actual.correo, actual.clave)) {
                is ResultadoAuth.Exito -> {
                    cargarUsuario()
                    _login.update { it.copy(cargando = false, exito = true) }
                }
                is ResultadoAuth.Error ->
                    _login.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun consumirLogin() { _login.value = LoginUiState() }

    fun onRegistroNombre(valor: String) = _registro.update {
        it.copy(nombre = valor, errorNombre = Validaciones.nombre(valor))
    }

    fun onRegistroCorreo(valor: String) = _registro.update {
        it.copy(correo = valor, errorCorreo = Validaciones.correo(valor), errorGeneral = null)
    }

    fun onRegistroTelefono(valor: String) = _registro.update {
        it.copy(telefono = valor, errorTelefono = Validaciones.telefono(valor))
    }

    fun onRegistroClave(valor: String) = _registro.update {
        it.copy(
            clave = valor,
            errorClave = Validaciones.clave(valor),
            errorConfirmacion = if (it.confirmacion.isNotEmpty()) Validaciones.confirmarClave(valor, it.confirmacion) else null
        )
    }

    fun onRegistroConfirmacion(valor: String) = _registro.update {
        it.copy(confirmacion = valor, errorConfirmacion = Validaciones.confirmarClave(it.clave, valor))
    }

    fun registrar() {
        val actual = _registro.value
        val errorNombre = Validaciones.nombre(actual.nombre)
        val errorCorreo = Validaciones.correo(actual.correo)
        val errorTelefono = Validaciones.telefono(actual.telefono)
        val errorClave = Validaciones.clave(actual.clave)
        val errorConfirmacion = Validaciones.confirmarClave(actual.clave, actual.confirmacion)
        if (listOf(errorNombre, errorCorreo, errorTelefono, errorClave, errorConfirmacion).any { it != null }) {
            _registro.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorCorreo = errorCorreo,
                    errorTelefono = errorTelefono,
                    errorClave = errorClave,
                    errorConfirmacion = errorConfirmacion
                )
            }
            return
        }
        _registro.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val resultado = repositorio.registrar(actual.nombre, actual.correo, actual.telefono, actual.clave)) {
                is ResultadoAuth.Exito -> {
                    cargarUsuario()
                    _registro.update { it.copy(cargando = false, exito = true) }
                }
                is ResultadoAuth.Error ->
                    _registro.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun consumirRegistro() { _registro.value = RegistroUiState() }

    fun prepararPerfil(usuario: Usuario?) {
        if (usuario != null) _perfil.value = PerfilUiState(nombre = usuario.nombre, telefono = usuario.telefono)
    }

    fun onPerfilNombre(valor: String) = _perfil.update {
        it.copy(nombre = valor, errorNombre = Validaciones.nombre(valor), guardado = false)
    }

    fun onPerfilTelefono(valor: String) = _perfil.update {
        it.copy(telefono = valor, errorTelefono = Validaciones.telefono(valor), guardado = false)
    }

    fun guardarPerfil() {
        val actual = _perfil.value
        val errorNombre = Validaciones.nombre(actual.nombre)
        val errorTelefono = Validaciones.telefono(actual.telefono)
        if (errorNombre != null || errorTelefono != null) {
            _perfil.update { it.copy(errorNombre = errorNombre, errorTelefono = errorTelefono) }
            return
        }
        viewModelScope.launch {
            repositorio.actualizarPerfil(actual.nombre, actual.telefono)
            cargarUsuario()
            _perfil.update { it.copy(guardado = true) }
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _usuario.value = null
        _login.value = LoginUiState()
        _registro.value = RegistroUiState()
        _perfil.value = PerfilUiState()
    }
}

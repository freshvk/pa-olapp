package cl.duoc.panolapp.domain

object Validaciones {
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_TELEFONO = Regex("^\\+?[0-9]{8,12}$")

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !valor.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor.trim()) -> "Ingresa un correo válido (ej: nombre@duoc.cl)"
        else -> null
    }

    fun telefono(valor: String): String? = when {
        valor.isBlank() -> "El teléfono es obligatorio"
        !REGEX_TELEFONO.matches(valor.trim()) -> "Ingresa de 8 a 12 dígitos (ej: +56912345678)"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 8 -> "Debe tener al menos 8 caracteres"
        valor.none { it.isDigit() } -> "Debe incluir al menos un número"
        valor.none { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
        else -> null
    }

    fun confirmarClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        clave != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun nombreEquipo(valor: String): String? = when {
        valor.isBlank() -> "El nombre del equipo es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.trim().length > 40 -> "Máximo 40 caracteres"
        else -> null
    }

    fun categoria(valor: String?): String? =
        if (valor.isNullOrBlank()) "Selecciona una categoría" else null

    fun cantidadTotal(valor: String, minimo: Int = 1): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            n < minimo -> if (minimo > 1) "Hay $minimo unidades prestadas: no puede ser menor" else "Debe ser al menos 1"
            n > MAXIMO_UNIDADES -> "Máximo $MAXIMO_UNIDADES"
            else -> null
        }
    }

    fun stockMinimo(valor: String, cantidadTotal: Int?): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            n < 0 -> "No puede ser negativo"
            cantidadTotal != null && n > cantidadTotal -> "No puede superar la cantidad total"
            else -> null
        }
    }

    fun solicitante(valor: String): String? = when {
        valor.isBlank() -> "Indica quién retira el equipo"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    fun cantidadPrestamo(valor: String, disponible: Int): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            disponible <= 0 -> "No hay unidades disponibles"
            n < 1 -> "Debe ser al menos 1"
            n > disponible -> "Solo hay $disponible disponibles"
            else -> null
        }
    }

    fun diasPrestamo(valor: String): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            n < 1 -> "Mínimo 1 día"
            n > MAXIMO_DIAS -> "Máximo $MAXIMO_DIAS días"
            else -> null
        }
    }

    const val MAXIMO_UNIDADES = 9999
    const val MAXIMO_DIAS = 30
}

package cl.duoc.panolapp.domain

import java.security.MessageDigest
import java.security.SecureRandom

object Seguridad {
    fun hash(clave: String, sal: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((sal + clave).toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

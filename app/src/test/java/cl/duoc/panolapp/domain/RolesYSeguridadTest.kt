package cl.duoc.panolapp.domain

import cl.duoc.panolapp.data.model.Roles
import cl.duoc.panolapp.data.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RolesYSeguridadTest {

    @Test
    fun usuarioConRolAdmin_esAdmin() =
        assertTrue(Usuario(1, "Ana", "a@a.cl", "123", Roles.ADMIN).esAdmin)

    @Test
    fun usuarioPorDefecto_noEsAdmin() =
        assertFalse(Usuario(1, "Ana", "a@a.cl", "123").esAdmin)

    @Test
    fun hashEsDeterministaConLaMismaSal() =
        assertEquals(Seguridad.hash("Clave123", "sal"), Seguridad.hash("Clave123", "sal"))

    @Test
    fun hashCambiaConOtraSal() =
        assertNotEquals(Seguridad.hash("Clave123", "sal1"), Seguridad.hash("Clave123", "sal2"))

    @Test
    fun hashNoContieneLaClave() =
        assertFalse(Seguridad.hash("Clave123", "sal").contains("Clave123"))

    @Test
    fun salesNuevasSonDistintas() =
        assertNotEquals(Seguridad.nuevaSal(), Seguridad.nuevaSal())
}

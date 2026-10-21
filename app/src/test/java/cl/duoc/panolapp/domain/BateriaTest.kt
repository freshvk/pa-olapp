package cl.duoc.panolapp.domain

import cl.duoc.panolapp.recursos.EstadoBateria
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BateriaTest {

    @Test
    fun nivelBajoSinCargar_esBaja() = assertTrue(EstadoBateria(15, false).baja)

    @Test
    fun nivelBajoCargando_noEsBaja() = assertFalse(EstadoBateria(15, true).baja)

    @Test
    fun nivelAltoSinCargar_noEsBaja() = assertFalse(EstadoBateria(80, false).baja)
}

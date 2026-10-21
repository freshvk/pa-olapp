package cl.duoc.panolapp.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun correoValido_noDaError() = assertNull(Validaciones.correo("ana@duocuc.cl"))

    @Test
    fun correoSinArroba_daError() = assertNotNull(Validaciones.correo("anaduocuc.cl"))

    @Test
    fun correoVacio_daError() = assertNotNull(Validaciones.correo(""))

    @Test
    fun claveCorta_daError() = assertNotNull(Validaciones.clave("ab1"))

    @Test
    fun confirmacionDistinta_daError() = assertNotNull(Validaciones.confirmarClave("Clave123", "Clave124"))

    @Test
    fun confirmacionIgual_noDaError() = assertNull(Validaciones.confirmarClave("Clave123", "Clave123"))

    @Test
    fun cantidadNoNumerica_daError() = assertNotNull(Validaciones.cantidadTotal("abc"))

    @Test
    fun cantidadCero_daError() = assertNotNull(Validaciones.cantidadTotal("0"))

    @Test
    fun cantidadMenorQuePrestadas_daError() = assertNotNull(Validaciones.cantidadTotal("2", 3))

    @Test
    fun cantidadValida_noDaError() = assertNull(Validaciones.cantidadTotal("10"))

    @Test
    fun stockMinimoMayorQueTotal_daError() = assertNotNull(Validaciones.stockMinimo("11", 10))

    @Test
    fun stockMinimoNegativo_daError() = assertNotNull(Validaciones.stockMinimo("-1", 10))

    @Test
    fun prestamoMayorQueDisponible_daError() = assertNotNull(Validaciones.cantidadPrestamo("5", 3))

    @Test
    fun prestamoValido_noDaError() = assertNull(Validaciones.cantidadPrestamo("2", 3))

    @Test
    fun diasFueraDeRango_daError() = assertNotNull(Validaciones.diasPrestamo("31"))

    @Test
    fun diasValidos_noDaError() = assertNull(Validaciones.diasPrestamo("3"))
}

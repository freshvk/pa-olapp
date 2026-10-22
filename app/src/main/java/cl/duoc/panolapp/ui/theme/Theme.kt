package cl.duoc.panolapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = MarinoNoche,
    onPrimary = Marino,
    primaryContainer = Color(0xFF2B3B57),
    onPrimaryContainer = MarinoClaro,
    secondary = AmbarNoche,
    onSecondary = Grafito,
    secondaryContainer = Color(0xFF5A4200),
    onSecondaryContainer = AmbarClaro,
    tertiary = AmbarNoche,
    tertiaryContainer = Color(0xFF4A3300),
    onTertiaryContainer = AmbarClaro,
    background = FondoNoche,
    onBackground = Color(0xFFE6E8EC),
    surface = SuperficieNoche,
    onSurface = Color(0xFFE6E8EC),
    surfaceVariant = Color(0xFF2A3341),
    onSurfaceVariant = Color(0xFFB8C0CC)
)

private val LightColorScheme = lightColorScheme(
    primary = Marino,
    onPrimary = Color.White,
    primaryContainer = MarinoClaro,
    onPrimaryContainer = Marino,
    secondary = Ambar,
    onSecondary = Grafito,
    secondaryContainer = AmbarClaro,
    onSecondaryContainer = Color(0xFF3B2A00),
    tertiary = Color(0xFF9A5B00),
    tertiaryContainer = Color(0xFFFFE3B8),
    onTertiaryContainer = Color(0xFF3B2300),
    background = Arena,
    onBackground = Grafito,
    surface = Color.White,
    onSurface = Grafito,
    surfaceVariant = ArenaOscura,
    onSurfaceVariant = Color(0xFF4A4F57)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun PanolAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}

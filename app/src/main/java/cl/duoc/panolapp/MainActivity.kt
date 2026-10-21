package cl.duoc.panolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import cl.duoc.panolapp.ui.navigation.AppNavegacion
import cl.duoc.panolapp.ui.theme.PanolAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanolAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavegacion()
                }
            }
        }
    }
}

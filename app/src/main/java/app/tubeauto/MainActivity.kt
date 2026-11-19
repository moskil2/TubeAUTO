package app.tubeauto

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.tubeauto.ui.PhoneApp

/** Aplikacja na telefonie: nazwa na środku i menu boczne. Obraz w aucie daje YtCarAppService. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Jasne ikony paska stanu i paska nawigacji na ciemnym tle
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { PhoneApp() }
    }

    override fun onResume() {
        super.onResume()
        // Odśwież stan "Android Auto" (np. po powrocie z ustawień lub z Shizuku)
        AutoInstall.resumeTick.intValue++
    }
}

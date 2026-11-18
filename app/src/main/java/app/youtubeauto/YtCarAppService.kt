package app.youtubeauto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Punkt wejścia dla Android Auto. Aplikacja jest tylko do użytku prywatnego,
 * więc przyjmuje połączenie od każdego hosta (bez listy dozwolonych).
 */
class YtCarAppService : CarAppService() {

    override fun onCreate() {
        super.onCreate()
        Dziennik.start(this)
        Dziennik.event("usługa Android Auto utworzona")
    }

    override fun onDestroy() {
        Dziennik.event("usługa Android Auto zamknięta")
        super.onDestroy()
    }

    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        init {
            Dziennik.event("sesja Android Auto start")
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = Dziennik.event("sesja widoczna")
                override fun onStop(owner: LifecycleOwner) = Dziennik.event("sesja w tle")
                override fun onDestroy(owner: LifecycleOwner) = Dziennik.event("sesja zakończona")
            })
        }

        override fun onCreateScreen(intent: Intent): Screen = MainScreen(carContext)

        override fun onNewIntent(intent: Intent) {
            Dziennik.event("sesja: nowe wywołanie $intent")
        }
    }
}

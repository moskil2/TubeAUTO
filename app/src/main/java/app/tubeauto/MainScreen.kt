package app.tubeauto

import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyCharacterMap
import android.view.KeyEvent
import java.net.URLEncoder
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.mozilla.geckoview.GeckoDisplay
import org.mozilla.geckoview.GeckoSession

/**
 * Zamiast mapy GeckoView rysuje stronę bezpośrednio na ekranie auta.
 * Etap 3: dotyk z ekranu auta trafia do strony, przyciski Wstecz / Start / Odśwież / Wyjdź.
 */
class MainScreen(carContext: CarContext) : Screen(carContext) {

    private val session = GeckoSession()
    private val touch = TouchInput(session)
    private val audioFocus = AudioFocus(carContext).also { session.mediaSessionDelegate = it }

    // Pełny ekran filmu: strona sama powiększa film na całą powierzchnię,
    // tu tylko zapis do dziennika (znacznik TubeAUTO) do sprawdzenia w teście.
    init {
        // Etap 5: stuknięte pole tekstowe otwiera ekran z klawiaturą Android Auto
        session.textInput.setDelegate(object : GeckoSession.TextInputDelegate {
            override fun showSoftInput(session: GeckoSession) {
                if (searchOpen) return
                searchOpen = true
                Log.i("TubeAUTO", "Strona prosi o klawiaturę")
                screenManager.push(SearchScreen(carContext) { text -> typeText(text) })
            }
        })
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                currentUrl = url ?: ""
            }
        }
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                Log.i("TubeAUTO", "Pełny ekran filmu: $fullScreen")
            }
        }
    }
    private var display: GeckoDisplay? = null
    private var width = 0
    private var height = 0

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(container: SurfaceContainer) {
            val surface = container.surface ?: return
            width = container.width
            height = container.height
            Log.i("TubeAUTO", "Ekran auta: ${container.width}x${container.height}, dpi ${container.dpi}")
            Browser.saveCarScreen(carContext, container.dpi, container.width, container.height)
            if (!session.isOpen) {
                session.open(
                    Browser.runtime(carContext, container.dpi, container.width, container.height)
                )
                // Napisy i jakość filmów z Settings > YouTube video
                Browser.attachVideoSettings(carContext, session)
                session.loadUri(Browser.HOME_URL)
            }
            val d = display ?: session.acquireDisplay().also { display = it }
            d.surfaceChanged(
                GeckoDisplay.SurfaceInfo.Builder(surface)
                    .size(container.width, container.height)
                    .build()
            )
            session.setActive(true)
            surfaceReady = true
            // Tekst wpisany na klawiaturze auta czekał, aż strona wróci na ekran
            pendingText?.let { text ->
                pendingText = null
                main.postDelayed({ sendText(text) }, TYPE_DELAY_MS)
            }
        }

        override fun onVisibleAreaChanged(area: Rect) {
            Log.i("TubeAUTO", "Widoczny obszar: $area")
        }

        override fun onStableAreaChanged(area: Rect) {
            Log.i("TubeAUTO", "Stały obszar: $area")
        }

        override fun onSurfaceDestroyed(container: SurfaceContainer) {
            Dziennik.event("ekran auta zniknął")
            surfaceReady = false
            session.setActive(false)
            display?.surfaceDestroyed()
        }

        override fun onClick(x: Float, y: Float) {
            Log.i("TubeAUTO", "AA stuknięcie x=$x y=$y")
            if (session.isOpen) touch.click(x, y)
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            if (session.isOpen) touch.scroll(distanceX, distanceY, width, height)
        }

        override fun onFling(velocityX: Float, velocityY: Float) {
            Log.i("TubeAUTO", "AA rzut vx=$velocityX vy=$velocityY")
            if (session.isOpen) touch.fling(velocityX, velocityY)
        }

        override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
            Log.i("TubeAUTO", "AA powiększanie fx=$focusX fy=$focusY skala=$scaleFactor")
            if (session.isOpen) touch.scale(focusX, focusY, scaleFactor, width, height)
        }
    }

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = Dziennik.event("ekran aplikacji widoczny")
            override fun onStop(owner: LifecycleOwner) = Dziennik.event("ekran aplikacji w tle")
            override fun onDestroy(owner: LifecycleOwner) {
                Dziennik.event("ekran aplikacji zamknięty")
                display?.let { session.releaseDisplay(it) }
                display = null
                audioFocus.release()
                if (session.isOpen) session.close()
            }
        })
    }

    // Napisy (próba z samymi ikonami w 0.2.0: ikony wyświetlały się źle,
    // a przyciski prawie się nie zmniejszyły).
    private fun button(title: String, onClick: () -> Unit): Action =
        Action.Builder().setTitle(title).setOnClickListener(onClick).build()

    private val GOOGLE_URL = "https://www.google.com"

    private var searchOpen = false
    private var currentUrl = ""

    // 0.3.4: tekst wysłany zanim strona wróciła na ekran auta ginął (Google: puste pole)
    private val main = Handler(Looper.getMainLooper())
    private var surfaceReady = false
    private var pendingText: String? = null
    private val TYPE_DELAY_MS = 500L

    /**
     * Tekst z klawiatury auta. Na YouTube i Google od razu otwieramy wyniki wyszukiwania.
     * Na innych stronach wpisujemy tekst w stuknięte pole i naciskamy Enter,
     * ale dopiero gdy strona wróci na ekran auta.
     */
    private fun typeText(text: String) {
        searchOpen = false
        if (!session.isOpen) return
        Log.i("TubeAUTO", "Wpisany tekst (${text.length} znaków), strona: $currentUrl")
        val host = Uri.parse(currentUrl).host ?: ""
        val query = URLEncoder.encode(text, "UTF-8")
        if (host.endsWith("youtube.com")) {
            session.loadUri("https://m.youtube.com/results?search_query=$query")
            return
        }
        if (host == "google.com" || host.startsWith("www.google.") || host.startsWith("google.")) {
            session.loadUri("https://www.google.com/search?q=$query")
            return
        }
        if (surfaceReady) main.postDelayed({ sendText(text) }, TYPE_DELAY_MS)
        else pendingText = text
    }

    private fun sendText(text: String) {
        if (!session.isOpen) return
        Log.i("TubeAUTO", "Wpisuję tekst w pole strony")
        val input = session.textInput
        val now = SystemClock.uptimeMillis()
        input.onKeyMultiple(
            KeyEvent.KEYCODE_UNKNOWN, 1,
            KeyEvent(now, text, KeyCharacterMap.VIRTUAL_KEYBOARD, 0)
        )
        input.onKeyDown(KeyEvent.KEYCODE_ENTER, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        input.onKeyUp(KeyEvent.KEYCODE_ENTER, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    override fun onGetTemplate(): Template {
        // Powrót z ekranu wpisywania ("Wstecz") też zwalnia blokadę ponownego otwarcia
        searchOpen = false
        // 0.3.3: BACK, HOME (YouTube), GOOGLE (google.com), EXIT; bez Reload i URL (decyzja użytkownika)
        val strip = ActionStrip.Builder()
            .addAction(button("BACK") { if (session.isOpen) session.goBack() })
            .addAction(button("HOME") { if (session.isOpen) session.loadUri(Browser.HOME_URL) })
            .addAction(button("GOOGLE") { if (session.isOpen) session.loadUri(GOOGLE_URL) })
            .addAction(button("EXIT") { carContext.finishCarApp() })
            .build()
        return NavigationTemplate.Builder()
            .setActionStrip(strip)
            .build()
    }
}

package app.youtubeauto

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.mozilla.gecko.GeckoScreenOrientation
import org.mozilla.gecko.GeckoScreenOrientation.ScreenOrientation
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.OrientationController
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController

/**
 * Jeden silnik GeckoView na całą aplikację (Android pozwala tylko na jeden w procesie).
 *
 * Silnik startuje dopiero, gdy Android Auto poda dane ekranu auta. Gęstość pikseli
 * i rozmiar ekranu da się ustawić tylko przy starcie, a bez tego GeckoView bierze
 * gęstość ekranu telefonu i strona w aucie jest kilka razy za duża.
 */
object Browser {
    const val HOME_URL = "https://m.youtube.com"

    private const val TAG = "TubeAUTO"

    // uBlock Origin wbudowany w APK (podpisany plik z addons.mozilla.org)
    private const val UBLOCK_ID = "uBlock0@raymondhill.net"
    private const val UBLOCK_ASSET = "resource://android/assets/ublock_origin.xpi"

    private var runtime: GeckoRuntime? = null

    /** Zapamiętuje ekran auta, żeby silnik mógł też wystartować z telefonu w tej samej skali. */
    fun saveCarScreen(context: Context, dpi: Int, width: Int, height: Int) {
        context.applicationContext.getSharedPreferences("ekran", Context.MODE_PRIVATE).edit()
            .putInt("dpi", dpi).putInt("w", width).putInt("h", height).apply()
    }

    /** Silnik uruchamiany z telefonu: ekran auta z ostatniego połączenia (za pierwszym razem 800x400). */
    fun runtimeFromPhone(context: Context): GeckoRuntime {
        val p = context.applicationContext.getSharedPreferences("ekran", Context.MODE_PRIVATE)
        return runtime(context, p.getInt("dpi", 160), p.getInt("w", 800), p.getInt("h", 400))
    }

    /** Sprawdza i od razu instaluje nowszą wersję uBlock; wynik słowami. */
    fun updateUblockNow(context: Context, done: (String) -> Unit) {
        val rt = runtimeFromPhone(context)
        whenUblockReady { ext -> checkUpdate(rt.webExtensionController, ext, context.applicationContext, done) }
    }

    fun runtime(context: Context, dpi: Int, width: Int, height: Int): GeckoRuntime =
        runtime ?: GeckoRuntime.create(
            context.applicationContext,
            GeckoRuntimeSettings.Builder()
                .displayDpiOverride(dpi)
                .displayDensityOverride(dpi / 160f)
                .screenSizeOverride(width, height)
                // Podgląd strony z Firefoksa na komputerze (about:debugging, tylko przez USB)
                .remoteDebuggingEnabled(true)
                .build()
        ).also {
            runtime = it
            setupLandscape(it)
            setupUblock(it, context.applicationContext)
            setupFullscreenFix(it)
        }

    /** Wbudowany dodatek: sygnał zmiany rozmiaru przy pełnym ekranie (assets/fsfix). */
    private fun setupFullscreenFix(rt: GeckoRuntime) {
        rt.webExtensionController.installBuiltIn("resource://android/assets/fsfix/").accept(
            { ext -> Log.i(TAG, "Poprawka pełnego ekranu: ${ext?.metaData?.version}") },
            { e -> Log.w(TAG, "Poprawka pełnego ekranu: instalacja nieudana", e) }
        )
    }

    /**
     * Silnik bierze ustawienie ekranu z telefonu (pionowe), a ekran auta jest poziomy.
     * YouTube na "pionowym" ekranie nie powiększa filmu w trybie pełnego ekranu,
     * więc zawsze podajemy poziome ustawienie i zgadzamy się na prośbę strony o obrót.
     */
    private fun setupLandscape(rt: GeckoRuntime) {
        // Silnik sam przelicza ustawienie z prawdziwego ekranu telefonu i nadpisuje
        // poziome na pionowe. Nasłuchujemy zmian i od razu przywracamy poziome.
        // Uwaga: GeckoScreenOrientation to wewnętrzna część GeckoView (nie oficjalna).
        val orientation = GeckoScreenOrientation.getInstance()
        orientation.addListener { now ->
            if (now != ScreenOrientation.LANDSCAPE_PRIMARY) {
                Log.i(TAG, "Silnik zmienił ustawienie ekranu na $now, przywracam poziome")
                mainHandler.post { orientation.update(ScreenOrientation.LANDSCAPE_PRIMARY) }
            }
        }
        orientation.update(ScreenOrientation.LANDSCAPE_PRIMARY)

        rt.orientationController.delegate = object : OrientationController.OrientationDelegate {
            override fun onOrientationLock(aOrientation: Int): GeckoResult<AllowOrDeny> {
                Log.i(TAG, "Strona prosi o ustawienie ekranu: $aOrientation")
                orientation.update(ScreenOrientation.LANDSCAPE_PRIMARY)
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Pierwszy start: instalacja uBlock z pliku w APK (działa bez internetu).
     * Każdy start: włączenie, jeśli był wyłączony, i sprawdzenie nowszej wersji
     * na addons.mozilla.org. Bez internetu zostaje obecna wersja.
     * Zgody na instalację i aktualizację aplikacja daje sama, bez okienka.
     */
    /** uBlock gotowy do użycia (po instalacji albo odczycie listy dodatków). */
    private var ublock: WebExtension? = null
    private val ublockWaiters = mutableListOf<(WebExtension) -> Unit>()

    /** Wywołuje [action], gdy uBlock jest gotowy (od razu, jeśli już jest). */
    fun whenUblockReady(action: (WebExtension) -> Unit) {
        val ext = ublock
        if (ext != null) action(ext) else ublockWaiters.add(action)
    }

    private fun ublockReady(context: Context, ext: WebExtension) {
        ublock = ext
        UblockStatus.saveVersion(context, ext.metaData.version)
        val waiting = ublockWaiters.toList()
        ublockWaiters.clear()
        waiting.forEach { it(ext) }
    }

    private fun setupUblock(rt: GeckoRuntime, context: Context) {
        val controller = rt.webExtensionController
        controller.promptDelegate = object : WebExtensionController.PromptDelegate {
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<WebExtension.PermissionPromptResponse> =
                GeckoResult.fromValue(
                    WebExtension.PermissionPromptResponse(true, false, false)
                )

            override fun onUpdatePrompt(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<AllowOrDeny> = GeckoResult.fromValue(AllowOrDeny.ALLOW)
        }

        controller.list().accept({ list ->
            val installed = list?.firstOrNull { it.id == UBLOCK_ID }
            if (installed == null) {
                controller.install(
                    UBLOCK_ASSET,
                    WebExtensionController.INSTALLATION_METHOD_FROM_FILE
                ).accept(
                    { ext ->
                        Log.i(TAG, "uBlock zainstalowany z APK: ${ext?.metaData?.version}")
                        if (ext != null) ublockReady(context, ext)
                    },
                    { e -> Log.w(TAG, "uBlock: instalacja nieudana", e) }
                )
            } else {
                Log.i(TAG, "uBlock obecny: ${installed.metaData.version}, włączony: ${installed.metaData.enabled}")
                if (!installed.metaData.enabled) {
                    controller.enable(installed, WebExtensionController.EnableSource.USER)
                }
                ublockReady(context, installed)
                checkUpdate(controller, installed, context)
            }
        }, { e -> Log.w(TAG, "uBlock: lista dodatków niedostępna", e) })
    }

    private fun checkUpdate(
        controller: WebExtensionController,
        ext: WebExtension,
        context: Context,
        done: (String) -> Unit = {}
    ) {
        controller.update(ext).accept(
            { updated ->
                if (updated == null) {
                    Log.i(TAG, "uBlock: brak nowszej wersji")
                    UblockStatus.saveCheck(context, "aktualna")
                    done("Masz aktualną wersję ${ext.metaData.version}.")
                } else {
                    Log.i(TAG, "uBlock zaktualizowany do ${updated.metaData.version}")
                    ublock = updated
                    UblockStatus.saveVersion(context, updated.metaData.version)
                    UblockStatus.saveCheck(context, "zaktualizowano do ${updated.metaData.version}")
                    done("Zaktualizowano do ${updated.metaData.version}.")
                }
            },
            { e ->
                Log.i(TAG, "uBlock: sprawdzenie aktualizacji niemożliwe (${e?.message})")
                UblockStatus.saveCheck(context, "nie udało się sprawdzić")
                done("Nie udało się sprawdzić (brak internetu?).")
            }
        )
    }
}

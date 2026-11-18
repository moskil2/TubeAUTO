package app.tubeauto

import android.content.Context
import org.mozilla.geckoview.GeckoRuntimeSettings

/** Ustawienia aplikacji (SharedPreferences), wspólne dla auta i telefonu. */
object Settings {

    private const val PREFS = "ustawienia"
    private const val KEY_THEME = "theme"

    /** Tryb stron: ciemny, jasny albo jak w telefonie. Wartości jak w GeckoRuntimeSettings. */
    enum class Theme(val label: String, val gecko: Int) {
        Dark("Dark", GeckoRuntimeSettings.COLOR_SCHEME_DARK),
        Light("Light", GeckoRuntimeSettings.COLOR_SCHEME_LIGHT),
        System("System", GeckoRuntimeSettings.COLOR_SCHEME_SYSTEM),
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun theme(context: Context): Theme =
        prefs(context).getString(KEY_THEME, null)
            ?.let { name -> Theme.entries.firstOrNull { it.name == name } }
            ?: Theme.System

    fun saveTheme(context: Context, theme: Theme) {
        prefs(context).edit().putString(KEY_THEME, theme.name).apply()
    }
}

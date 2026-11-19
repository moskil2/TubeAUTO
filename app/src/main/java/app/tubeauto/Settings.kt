package app.tubeauto

import android.content.Context

/** Ustawienia aplikacji (SharedPreferences), wspólne dla auta i telefonu. */
object Settings {

    private const val PREFS = "ustawienia"

    // 0.3.6: wybór trybu stron (Page theme) usunięty, wygląd ustawia się w samym YouTube

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Napisy w filmach YouTube; YouTube = bez zmian (jak ustawi sam YouTube). */
    enum class Captions(val label: String, val yt: String) {
        Off("Off", "off"),
        On("On", "on"),
        YouTube("YouTube", "default"),
    }

    /** Jakość filmów YouTube; nazwy jak w odtwarzaczu YouTube, auto = bez zmian. */
    enum class Quality(val label: String, val yt: String) {
        Auto("Auto", "auto"),
        P2160("2160p", "hd2160"),
        P1440("1440p", "hd1440"),
        P1080("1080p", "hd1080"),
        P720("720p", "hd720"),
        P480("480p", "large"),
        P360("360p", "medium"),
    }

    private const val KEY_CAPTIONS = "captions"
    private const val KEY_QUALITY = "quality"

    fun captions(context: Context): Captions =
        prefs(context).getString(KEY_CAPTIONS, null)
            ?.let { name -> Captions.entries.firstOrNull { it.name == name } }
            ?: Captions.YouTube

    fun saveCaptions(context: Context, value: Captions) {
        prefs(context).edit().putString(KEY_CAPTIONS, value.name).apply()
    }

    fun quality(context: Context): Quality =
        prefs(context).getString(KEY_QUALITY, null)
            ?.let { name -> Quality.entries.firstOrNull { it.name == name } }
            ?: Quality.Auto

    fun saveQuality(context: Context, value: Quality) {
        prefs(context).edit().putString(KEY_QUALITY, value.name).apply()
    }
}

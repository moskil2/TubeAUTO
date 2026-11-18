package app.youtubeauto

import android.content.Context

/**
 * Stan uBlock Origin wspólny dla auta i telefonu (ten sam proces, SharedPreferences).
 * Zapisywane wersja i wynik ostatniego sprawdzenia (w aucie albo z telefonu).
 */
object UblockStatus {

    private const val PREFS = "ublock"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun installedVersion(context: Context): String? = prefs(context).getString("version", null)
    fun lastCheckTime(context: Context): Long = prefs(context).getLong("checkTime", 0L)
    fun lastCheckResult(context: Context): String? = prefs(context).getString("checkResult", null)

    fun saveVersion(context: Context, version: String?) {
        prefs(context).edit().putString("version", version).apply()
    }

    fun saveCheck(context: Context, result: String) {
        prefs(context).edit()
            .putLong("checkTime", System.currentTimeMillis())
            .putString("checkResult", result)
            .apply()
    }
}

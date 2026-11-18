package app.tubeauto

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trwały dziennik na telefonie (do odczytu po teście w aucie, także następnego dnia).
 *
 * - files/dziennik/log*: wszystko, co zapisuje aplikacja (także biblioteka Android Auto
 *   i silnik Gecko), maks. 5 plików po 1 MB, najstarszy usuwany.
 * - files/dziennik/bledy.txt: nieobsłużone błędy programu z pełnym opisem.
 *
 * Odczyt: adb shell run-as app.tubeauto ls files/dziennik
 */
object Dziennik {

    private const val TAG = "TubeAUTO"
    private var started = false

    @Synchronized
    fun start(context: Context) {
        if (started) return
        started = true
        val dir = File(context.filesDir, "dziennik").apply { mkdirs() }
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "logcat",
                    "-v", "threadtime",
                    "--uid=${Process.myUid()}",
                    "-f", File(dir, "log").absolutePath,
                    "-r", "1024",
                    "-n", "4"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Nie udało się uruchomić zapisu dziennika", e)
        }

        val crashFile = File(dir, "bledy.txt")
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val sw = StringWriter()
                error.printStackTrace(PrintWriter(sw))
                crashFile.appendText("=== ${now()} wątek ${thread.name}\n$sw\n")
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, error)
        }
        val version = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "?"
        }
        Log.i(TAG, "Dziennik: start procesu, wersja $version")
    }

    fun event(text: String) {
        Log.i(TAG, "Zdarzenie: $text")
    }

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
}

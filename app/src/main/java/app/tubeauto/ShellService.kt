package app.tubeauto

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import kotlin.system.exitProcess

/**
 * Działa w procesie uruchomionym przez Shizuku, z tożsamością "shell" (uid 2000).
 * Dzięki temu systemowy instalator uruchomiony stąd zapisuje instalację jako
 * "ze Sklepu Play" i Android Auto przyjmuje TubeAUTO.
 * Wzorowane na ShellService z UnknownAuto (GitMag, licencja MIT).
 */
class ShellService() : IShellService.Stub() {

    // Shizuku próbuje najpierw konstruktora z Context, potem bez argumentów.
    @Suppress("UNUSED_PARAMETER")
    constructor(context: Context) : this()

    override fun destroy() {
        exitProcess(0)
    }

    override fun run(cmd: Array<String>): String? = try {
        val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        val input = p.inputStream
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            if (out.size() < MAX_OUTPUT) out.write(buf, 0, minOf(n, MAX_OUTPUT - out.size()))
        }
        val code = p.waitFor()
        val s = out.toString("UTF-8")
        if (code == 0) s else "$s\n[exit $code]"
    } catch (t: Throwable) {
        null
    }

    /**
     * Uruchamia [intent] jako shell, z aktywnością aplikacji ([resultTo]) jako odbiorcą wyniku.
     * Ukryte API; ten proces nie jest aplikacją, więc ograniczenia go nie dotyczą.
     */
    override fun startForResult(intent: Intent, resultTo: IBinder?, requestCode: Int): String = try {
        val atm = if (Build.VERSION.SDK_INT >= 29) {
            Class.forName("android.app.ActivityTaskManager").getMethod("getService").invoke(null)
        } else {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        }
        val type = intent.type
        var result: String? = null
        for (m in atm.javaClass.methods) {
            if (m.name != "startActivity") continue
            val p = m.parameterTypes
            val args: Array<Any?> = when {
                // API 30+: (caller, callingPackage, callingFeatureId, intent, resolvedType,
                //           resultTo, resultWho, requestCode, flags, profilerInfo, options)
                p.size == 11 && p[3] == Intent::class.java ->
                    arrayOf(null, SHELL, null, intent, type, resultTo, null, requestCode, 0, null, null)
                // API 26-29: to samo bez callingFeatureId
                p.size == 10 && p[2] == Intent::class.java ->
                    arrayOf(null, SHELL, intent, type, resultTo, null, requestCode, 0, null, null)
                else -> continue
            }
            val res = m.invoke(atm, *args) as Int
            result = if (res >= 0) "ok" else "startActivity returned $res"
            break
        }
        result ?: "no usable startActivity on ${atm.javaClass.name}"
    } catch (e: InvocationTargetException) {
        e.cause.toString()
    } catch (t: Throwable) {
        t.toString()
    }

    private companion object {
        const val SHELL = "com.android.shell"
        const val MAX_OUTPUT = 256 * 1024
    }
}

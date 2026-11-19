package app.tubeauto

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import rikka.shizuku.Shizuku

/**
 * Włączenie TubeAUTO w Android Auto bez osobnego programu (zamiast UnknownAuto).
 *
 * Android Auto pokazuje aplikacje Car App Library tylko zainstalowane ze Sklepu Play
 * (Android 11+: initiatingPackageName = instalator systemowy lub Play, installer = Play).
 * Przez Shizuku TubeAUTO uruchamia systemowy instalator jako "shell" z własnym plikiem APK
 * i oznaczeniem com.android.vending. Użytkownik stuka raz "Aktualizuj".
 * Metoda z UnknownAuto (GitMag, licencja MIT).
 */
object AutoInstall {

    private const val VENDING = "com.android.vending"
    private const val SHIZUKU_PKG = "moe.shizuku.privileged.api"
    private const val GOOGLE_INSTALLER = "com.google.android.packageinstaller"
    private const val AOSP_INSTALLER = "com.android.packageinstaller"
    private const val REQ_SHIZUKU = 7301
    private const val REQ_INSTALL = 7302

    /** Zwiększane w MainActivity.onResume, żeby ekran telefonu odświeżył stan. */
    val resumeTick = mutableIntStateOf(0)

    private val main = Handler(Looper.getMainLooper())

    /** Czy Android Auto przyjmie TubeAUTO (kto zainstalował aplikację). */
    fun isEnabled(context: Context): Boolean = try {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= 30) {
            val info = pm.getInstallSourceInfo(context.packageName)
            accepts(info.initiatingPackageName, info.installingPackageName)
        } else {
            @Suppress("DEPRECATION")
            VENDING == pm.getInstallerPackageName(context.packageName)
        }
    } catch (_: Exception) {
        false
    }

    private fun accepts(initiating: String?, installer: String?): Boolean =
        initiating != null && installer == VENDING && initiating != "com.android.shell" &&
            (initiating == VENDING || initiating.contains("packageinstaller"))

    private fun sourceText(context: Context): String = try {
        if (Build.VERSION.SDK_INT >= 30) {
            val i = context.packageManager.getInstallSourceInfo(context.packageName)
            "initiating=${i.initiatingPackageName} installer=${i.installingPackageName}"
        } else "sdk ${Build.VERSION.SDK_INT}"
    } catch (e: Exception) {
        e.toString()
    }

    /** Stuknięcie "Enable in Android Auto". [report] dostaje komunikat dla użytkownika (po angielsku). */
    fun enable(activity: Activity, report: (String) -> Unit) {
        Dziennik.event("Android Auto: włączanie, ${sourceText(activity)}")
        if (!isInstalled(activity, SHIZUKU_PKG)) {
            report("Shizuku is not installed. Install Shizuku (github.com/RikkaApps/Shizuku), start it, then tap again.")
            return
        }
        try {
            if (!Shizuku.pingBinder()) {
                report("Shizuku is not running. Open Shizuku, start it, then tap again.")
                return
            }
            if (Shizuku.isPreV11()) {
                report("Shizuku is too old. Update Shizuku, then tap again.")
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.addRequestPermissionResultListener(object : Shizuku.OnRequestPermissionResultListener {
                    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                        if (requestCode != REQ_SHIZUKU) return
                        Shizuku.removeRequestPermissionResultListener(this)
                        if (grantResult == PackageManager.PERMISSION_GRANTED) launch(activity, report)
                        else report("Shizuku permission was not granted.")
                    }
                })
                Shizuku.requestPermission(REQ_SHIZUKU)
                report("Allow TubeAUTO in the Shizuku prompt.")
                return
            }
        } catch (t: Throwable) {
            Dziennik.event("Android Auto: Shizuku niedostępne: $t")
            report("Shizuku is not available. Start Shizuku, then tap again.")
            return
        }
        launch(activity, report)
    }

    /** Shizuku gotowe: uruchom systemowy instalator jako shell z własnym APK. */
    private fun launch(activity: Activity, report: (String) -> Unit) {
        // Android 14 i starsze: instalator sprawdza aplikację, która czeka na wynik,
        // więc TubeAUTO potrzebuje zgody "Instalowanie nieznanych aplikacji".
        // Android 15+ sprawdza tylko uruchamiającego (shell): wystarczy "am start".
        val asResult = Build.VERSION.SDK_INT <= 34
        if (asResult && !activity.packageManager.canRequestPackageInstalls()) {
            report("Allow \"Install unknown apps\" for TubeAUTO, then come back and tap again.")
            activity.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}"))
            )
            return
        }
        val target = systemInstaller(activity)
        if (target == null) {
            report("No system package installer found on this phone.")
            return
        }
        grantToInstallers(activity, target.packageName)
        report("Tap \"Update\" in the installer. TubeAUTO closes during the update, then open it again.")
        Dziennik.event("Android Auto: instalator ${target.flattenToString()}, asResult=$asResult")

        val resultTo = activity.window.attributes.token
        withShell(activity, { s ->
            if (asResult) s.startForResult(installIntent(target), resultTo, REQ_INSTALL)
            else s.run(installCommand(target))
        }) { out ->
            Dziennik.event("Android Auto: uruchomienie instalatora: $out")
            val ok = if (asResult) out == "ok" else out != null && !out.contains("Error") && !out.contains("[exit ")
            if (!ok) report("The installer could not be started ($out).")
        }
    }

    private fun installIntent(installer: ComponentName) = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
        component = installer
        setDataAndType(ApkProvider.uri(), ApkProvider.MIME)
        putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", VENDING)
        @Suppress("DEPRECATION")
        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        putExtra("android.intent.extra.INSTALL_REASON", 1)
        putExtra(Intent.EXTRA_REFERRER_NAME, "android-app://$VENDING")
        @Suppress("DEPRECATION")
        putExtra(Intent.EXTRA_RETURN_RESULT, true)
    }

    private fun installCommand(installer: ComponentName) = arrayOf(
        "/system/bin/am", "start",
        "-n", installer.flattenToString(),
        "-a", "android.intent.action.INSTALL_PACKAGE",
        "-d", ApkProvider.uri().toString(),
        "-t", ApkProvider.MIME,
        "--es", "android.intent.extra.INSTALLER_PACKAGE_NAME", VENDING,
        "--ez", "android.intent.extra.NOT_UNKNOWN_SOURCE", "true",
        "--ei", "android.intent.extra.INSTALL_REASON", "1",
        "--es", "android.intent.extra.REFERRER_NAME", "android-app://$VENDING",
    )

    /** Systemowy instalator: Google, potem AOSP, potem producenta telefonu. */
    private fun systemInstaller(context: Context): ComponentName? {
        val probe = Intent(Intent.ACTION_INSTALL_PACKAGE).setDataAndType(ApkProvider.uri(), ApkProvider.MIME)
        val handlers = try {
            context.packageManager.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY)
        } catch (_: Throwable) {
            return null
        }
        return handlers
            .filter { isSystemApp(context, it.activityInfo.packageName) }
            .minByOrNull {
                when (it.activityInfo.packageName) {
                    GOOGLE_INSTALLER -> 0
                    AOSP_INSTALLER -> 1
                    else -> 2
                }
            }
            ?.let { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
    }

    private fun grantToInstallers(context: Context, systemPkg: String) {
        for (p in listOf(GOOGLE_INSTALLER, AOSP_INSTALLER, VENDING, systemPkg)) {
            try {
                context.grantUriPermission(p, ApkProvider.uri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                // brak takiej aplikacji
            }
        }
    }

    /** Podpina usługę shell przez Shizuku, wykonuje jedno wywołanie, wynik na wątku głównym. */
    private fun withShell(context: Context, call: (IShellService) -> String?, done: (String?) -> Unit) {
        val args = Shizuku.UserServiceArgs(ComponentName(context.packageName, ShellService::class.java.name))
            .daemon(false).processNameSuffix("shell").debuggable(false).version(BuildConfig.VERSION_CODE)
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val self = this
                Thread {
                    val out = try {
                        call(IShellService.Stub.asInterface(binder))
                    } catch (t: Throwable) {
                        t.toString()
                    }
                    main.post { done(out) }
                    try {
                        Shizuku.unbindUserService(args, self, true)
                    } catch (_: Throwable) {
                    }
                }.start()
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        try {
            Shizuku.bindUserService(args, conn)
        } catch (t: Throwable) {
            done("Shizuku bind failed: ${t.message}")
        }
    }

    private fun isInstalled(context: Context, pkg: String) = try {
        context.packageManager.getPackageInfo(pkg, 0)
        true
    } catch (_: Exception) {
        false
    }

    private fun isSystemApp(context: Context, pkg: String) = try {
        (context.packageManager.getApplicationInfo(pkg, 0).flags and ApplicationInfo.FLAG_SYSTEM) != 0
    } catch (_: Throwable) {
        false
    }
}

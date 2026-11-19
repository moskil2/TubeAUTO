// Usługa uruchamiana przez Shizuku z tożsamością "shell" (uid 2000), jak adb.
// Wzorowane na UnknownAuto (GitMag, licencja MIT).
package app.tubeauto;

import android.content.Intent;

interface IShellService {
    // Zwyczaj Shizuku: destroy() musi mieć ten numer, żeby odpięcie zatrzymało usługę.
    void destroy() = 16777114;
    // Uruchamia polecenie jako shell; zwraca wyjście, z "[exit N]" przy błędzie, null gdy nie ruszyło.
    String run(in String[] cmd) = 1;
    // Uruchamia aktywność jako shell, z aktywnością aplikacji jako odbiorcą wyniku; "ok" albo powód.
    String startForResult(in Intent intent, IBinder resultTo, int requestCode) = 2;
}

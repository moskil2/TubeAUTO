"""Uruchamia symulator Android Auto (DHU) i przekazuje mu polecenia z pliku.

Użycie:  python -I symulator.py
Polecenia: dopisz linię do narzedzia/polecenia.txt, np.
    tap 400 200
    screenshot C:\\TEMP\\TubeAUTO\\narzedzia\\zrzut.png
Odpowiedzi symulatora trafiają do narzedzia/symulator.log.
Wymaga wcześniej: adb forward tcp:5277 tcp:5277 i serwera radioodtwarzacza w Android Auto.
"""
import os
import subprocess
import time

DHU_DIR = r"C:\Android\Sdk\extras\google\auto"
HERE = os.path.dirname(os.path.abspath(__file__))
CMD_FILE = os.path.join(HERE, "polecenia.txt")
LOG_FILE = os.path.join(HERE, "symulator.log")

open(CMD_FILE, "w").close()
log = open(LOG_FILE, "w", buffering=1, encoding="utf-8", errors="replace")
dhu = subprocess.Popen(
    [os.path.join(DHU_DIR, "desktop-head-unit.exe")],
    cwd=DHU_DIR,
    stdin=subprocess.PIPE,
    stdout=log,
    stderr=subprocess.STDOUT,
    text=True,
)

done = 0
while dhu.poll() is None:
    with open(CMD_FILE, encoding="utf-8") as f:
        lines = f.read().splitlines()
    for line in lines[done:]:
        if line.strip():
            log.write(f">>> {line}\n")
            dhu.stdin.write(line + "\n")
            dhu.stdin.flush()
    done = len(lines)
    time.sleep(0.3)
log.write(f"symulator zakończony, kod {dhu.returncode}\n")

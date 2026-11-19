// W aucie okno strony ma stały rozmiar, więc przy pełnym ekranie YouTube nie dostaje
// sygnału zmiany rozmiaru i nie powiększa filmu. Wysyłamy ten sygnał sami.
document.addEventListener("fullscreenchange", () => {
  for (const delay of [50, 300]) {
    setTimeout(() => window.dispatchEvent(new Event("resize")), delay);
  }
});

// Stuknięcie w pasek postępu przenosi film w miejsce stuknięcia. W samym YouTube
// stuknięcie tylko pokazuje miniaturkę, a przewija się przeciąganiem kropki, czego
// w Android Auto nie da się zrobić (aplikacja nie zna miejsca, gdzie zaczyna się ruch).
// Nasłuch rusza przed skryptami YouTube, więc widzi dotknięcia, zanim YouTube je przechwyci.
let barTouch = null;

window.addEventListener("touchstart", (e) => {
  const bar = e.target.closest && e.target.closest("yt-progress-bar, .ytPlayerProgressBarHost");
  if (!bar || e.touches.length !== 1) {
    barTouch = null;
    return;
  }
  const t = e.touches[0];
  barTouch = { x: t.clientX, y: t.clientY, time: Date.now(), bar };
}, true);

window.addEventListener("touchend", (e) => {
  const s = barTouch;
  barTouch = null;
  if (!s) return;
  const t = e.changedTouches[0];
  // Tylko krótkie stuknięcie bez przesunięcia; przeciąganie zostaje dla YouTube.
  if (Math.hypot(t.clientX - s.x, t.clientY - s.y) > 10 || Date.now() - s.time > 500) return;
  const line = s.bar.querySelector("yt-progress-bar-line, .ytProgressBarLineHost") || s.bar;
  const r = line.getBoundingClientRect();
  const video = document.querySelector("video");
  if (!video || !isFinite(video.duration) || r.width <= 0) return;
  const f = Math.min(1, Math.max(0, (t.clientX - r.left) / r.width));
  video.currentTime = f * video.duration;
}, true);

// Napisy i jakość filmów z ustawień aplikacji (telefon: Settings > YouTube video).
// Ustawienia przychodzą z aplikacji przez wiadomość "tubeauto"; odtwarzacz YouTube
// (#movie_player) jest w kodzie strony, więc wołamy go przez wrappedJSObject.
// Wynik każdej próby idzie do dziennika aplikacji.
const toApp = (msg) => browser.runtime.sendNativeMessage("tubeauto", msg);
const appLog = (text) => toApp({ log: text }).catch(() => {});
const QUALITY_ORDER = ["hd2160", "hd1440", "hd1080", "hd720", "large", "medium", "small", "tiny"];
let doneVideo = "";
let tries = 0;

function currentVideo() {
  return new URLSearchParams(location.search).get("v") || "";
}

function player() {
  const el = document.getElementById("movie_player");
  const p = el && el.wrappedJSObject;
  return p && typeof p.getAvailableQualityLevels === "function" ? p : null;
}

function applyTo(p, s) {
  const levels = Array.from(p.getAvailableQualityLevels() || []);
  let q = "bez zmian";
  if (s.quality !== "auto" && levels.length) {
    // Wybrana jakość albo najbliższa niższa, jeśli filmu nie ma w wybranej
    const from = Math.max(0, QUALITY_ORDER.indexOf(s.quality));
    const target = QUALITY_ORDER.slice(from).find((l) => levels.includes(l)) || levels[levels.length - 1];
    if (typeof p.setPlaybackQualityRange === "function") p.setPlaybackQualityRange(target, target);
    if (typeof p.setPlaybackQuality === "function") p.setPlaybackQuality(target);
    q = target + ", teraz " + (typeof p.getPlaybackQuality === "function" ? p.getPlaybackQuality() : "?");
  }
  let c = "bez zmian";
  if (s.captions === "off" && typeof p.unloadModule === "function") {
    p.unloadModule("captions");
    c = "wyłączone";
  } else if (s.captions === "on" && typeof p.loadModule === "function") {
    p.loadModule("captions");
    c = "włączone";
  }
  return `jakości [${levels.join(",")}], jakość ${q}, napisy ${c}`;
}

async function applySettings() {
  const id = currentVideo();
  if (!id) {
    doneVideo = "";
    return;
  }
  if (id === doneVideo) return;
  const p = player();
  if (!p) return;
  doneVideo = id;
  try {
    const raw = await toApp({ get: "settings" });
    appLog(`film ${id}: ustawienia z aplikacji: ${raw}`);
    const s = typeof raw === "string" ? JSON.parse(raw) : raw;
    if (s.quality === "auto" && s.captions === "default") return;
    appLog(`film ${id} (start): ${applyTo(p, s)}`);
    // YouTube potrafi później sam włączyć napisy lub zmienić jakość: powtórka po chwili
    for (const delay of [3000, 8000]) {
      setTimeout(() => {
        const p2 = player();
        if (p2 && currentVideo() === id) {
          const r = applyTo(p2, s);
          if (delay === 8000) appLog(`film ${id} (po 8 s): ${r}`);
        }
      }, delay);
    }
  } catch (e) {
    if (tries++ < 3) {
      appLog(`film ${id}: ustawienia nieudane: ${e}`);
      doneVideo = "";
    }
  }
}

// 0.3.5: diagnoza - znak, że dodatek ruszył na stronie
toApp({ log: `dodatek ruszył: ${location.href}` }).catch((e) => console.log("TubeAUTO: brak połączenia z aplikacją: " + e));

setInterval(applySettings, 1000);

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

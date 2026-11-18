package app.youtubeauto

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import kotlin.math.hypot
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.PanZoomController
import org.mozilla.geckoview.ScreenLength

/**
 * Zamienia gesty z ekranu auta (Android Auto podaje je jako kliknięcie, przewijanie,
 * rzut i skalowanie) na dotyk w GeckoView.
 */
class TouchInput(private val session: GeckoSession) {

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Stuknięcie jak prawdziwym palcem: w dół, po 30 ms ruch o 1 px, po 80 ms w górę.
     * Natychmiastowe w dół i w górę (ok. 2 ms) pasek postępu YouTube ignorował.
     */
    fun click(x: Float, y: Float) {
        val down = SystemClock.uptimeMillis()
        send(touch(down, down, MotionEvent.ACTION_DOWN, x, y))
        handler.postDelayed({
            send(touch(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x + 1f, y))
        }, CLICK_MOVE_MS)
        handler.postDelayed({
            send(touch(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x + 1f, y))
        }, CLICK_UP_MS)
    }

    // --- Przewijanie jako udawane przeciąganie palcem ---
    // Android Auto podaje kolejne odcinki ruchu (distance = poprzednia pozycja - nowa).
    // Udajemy palec położony na środku ekranu i przesuwany o te odcinki. Silnik Firefoxa
    // traktuje to jak prawdziwy palec, a po puszczeniu sam nadaje stronie rozpęd.

    private var dragActive = false
    private var dragMoved = false
    private var pendingX = 0f
    private var pendingY = 0f
    private var dragDown = 0L
    private var dragX = 0f
    private var dragY = 0f
    private val endDrag = Runnable { finishDrag() }

    fun scroll(distanceX: Float, distanceY: Float, width: Int, height: Int) {
        Log.d(TAG, "scroll dx=$distanceX dy=$distanceY")
        // Android Auto po zakończeniu przeciągania wysyła jeszcze ruch o zerowej długości.
        // Nie może on położyć palca, bo strona odczytałaby to jako stuknięcie.
        if (distanceX == 0f && distanceY == 0f) return
        val now = SystemClock.uptimeMillis()
        handler.removeCallbacks(endDrag)
        handler.postDelayed(endDrag, DRAG_END_DELAY_MS)
        var dx = distanceX
        var dy = distanceY
        if (!dragActive) {
            // Nowy palec dopiero po ruchu większym niż START_SLOP. Drobne resztki ruchu
            // (1-2 px) strona uznałaby za stuknięcie.
            pendingX += dx
            pendingY += dy
            if (hypot(pendingX, pendingY) < START_SLOP) return
            startDrag(now, width, height)
            dx = pendingX
            dy = pendingY
            pendingX = 0f
            pendingY = 0f
        }
        dragX -= dx
        dragY -= dy
        dragMoved = true
        // Palec doszedł do krawędzi ekranu: podnosimy go. Kolejny ruch położy nowy palec
        // na środku, ale dopiero po przekroczeniu START_SLOP (żeby nie było stuknięcia).
        if (dragX < 0 || dragX > width || dragY < 0 || dragY > height) {
            finishDrag()
        } else {
            send(touch(dragDown, now, MotionEvent.ACTION_MOVE, dragX, dragY))
        }
    }

    private fun startDrag(now: Long, width: Int, height: Int) {
        dragX = width / 2f
        dragY = height / 2f
        dragDown = now
        dragActive = true
        dragMoved = false
        send(touch(dragDown, now, MotionEvent.ACTION_DOWN, dragX, dragY))
    }

    /** Podniesienie palca. Bez przesunięcia: "gest anulowany", żeby nie było kliknięcia. */
    private fun finishDrag() {
        pendingX = 0f
        pendingY = 0f
        if (!dragActive) return
        handler.removeCallbacks(endDrag)
        val action = if (dragMoved) MotionEvent.ACTION_UP else MotionEvent.ACTION_CANCEL
        send(touch(dragDown, SystemClock.uptimeMillis(), action, dragX, dragY))
        dragActive = false
        dragMoved = false
    }

    /**
     * Szybkie przesunięcie. Jeśli trwa udawane przeciąganie, wystarczy podnieść palec:
     * silnik sam policzy rozpęd z ruchu. W innym razie przewijamy z rozpędem ręcznie.
     */
    fun fling(velocityX: Float, velocityY: Float) {
        Log.d(TAG, "fling vx=$velocityX vy=$velocityY drag=$dragActive")
        if (dragActive) {
            finishDrag()
            return
        }
        session.panZoomController.scrollBy(
            ScreenLength.fromPixels((-velocityX * FLING_SECONDS).toDouble()),
            ScreenLength.fromPixels((-velocityY * FLING_SECONDS).toDouble()),
            PanZoomController.SCROLL_BEHAVIOR_SMOOTH
        )
    }

    // --- Powiększanie dwoma palcami ---
    // Android Auto podaje kolejne kroki skalowania. Udajemy dwa palce wokół punktu
    // skupienia i rozsuwamy je albo zsuwamy. Gest kończy się po chwili bez nowych kroków.

    private var pinchDown = 0L
    private var pinchActive = false
    private var focusX = 0f
    private var focusY = 0f
    private var spread = PINCH_START_SPREAD
    private val endPinch = Runnable { finishPinch() }

    // Podwójne stuknięcie Android Auto podaje jako jeden krok skalowania 2,0 w miejscu
    // stuknięcia. Czekamy chwilę: jeśli nie przyjdzie kolejny krok, to było podwójne
    // stuknięcie i wysyłamy do strony dwa stuknięcia zamiast dwóch palców.
    private var pendingScale: FloatArray? = null
    private val doubleTap = Runnable {
        val p = pendingScale ?: return@Runnable
        pendingScale = null
        Log.i(TAG, "podwójne stuknięcie x=${p[0]} y=${p[1]}")
        click(p[0], p[1])
        handler.postDelayed({ click(p[0], p[1]) }, DOUBLE_TAP_GAP_MS)
    }

    fun scale(fx: Float, fy: Float, scaleFactor: Float, width: Int, height: Int) {
        if (!pinchActive) {
            val p = pendingScale
            if (p == null && scaleFactor == 2f) {
                pendingScale = floatArrayOf(
                    if (fx < 0) width / 2f else fx,
                    if (fy < 0) height / 2f else fy
                )
                handler.postDelayed(doubleTap, DOUBLE_TAP_WAIT_MS)
                return
            }
            if (p != null) {
                // Kolejny krok: to jednak prawdziwe powiększanie. Najpierw krok odłożony.
                handler.removeCallbacks(doubleTap)
                pendingScale = null
                pinchStep(p[0], p[1], 2f, width, height)
            }
        }
        pinchStep(fx, fy, scaleFactor, width, height)
    }

    private fun pinchStep(fx: Float, fy: Float, scaleFactor: Float, width: Int, height: Int) {
        val now = SystemClock.uptimeMillis()
        if (!pinchActive) {
            // Gdy Android Auto nie zna punktu skupienia, podaje -1: bierzemy środek ekranu.
            focusX = if (fx < 0) width / 2f else fx
            focusY = if (fy < 0) height / 2f else fy
            spread = PINCH_START_SPREAD
            pinchDown = now
            pinchActive = true
            send(pinch(now, MotionEvent.ACTION_DOWN, 1))
            send(pinch(now, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2))
        }
        spread = (spread * scaleFactor).coerceIn(20f, 2000f)
        send(pinch(now, MotionEvent.ACTION_MOVE, 2))
        handler.removeCallbacks(endPinch)
        handler.postDelayed(endPinch, PINCH_END_DELAY_MS)
    }

    private fun finishPinch() {
        if (!pinchActive) return
        val now = SystemClock.uptimeMillis()
        send(pinch(now, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2))
        send(pinch(now, MotionEvent.ACTION_UP, 1))
        pinchActive = false
    }

    private fun pinch(time: Long, action: Int, pointers: Int): MotionEvent {
        val props = Array(pointers) { i ->
            MotionEvent.PointerProperties().apply {
                id = i
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        }
        val coords = Array(pointers) { i ->
            MotionEvent.PointerCoords().apply {
                x = if (i == 0) focusX - spread / 2 else focusX + spread / 2
                y = focusY
                pressure = 1f
                size = 1f
            }
        }
        return MotionEvent.obtain(
            pinchDown, time, action, pointers, props, coords,
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
        )
    }

    private fun touch(down: Long, time: Long, action: Int, x: Float, y: Float): MotionEvent =
        MotionEvent.obtain(down, time, action, x, y, 0).apply {
            source = InputDevice.SOURCE_TOUCHSCREEN
        }

    private fun send(event: MotionEvent) {
        session.panZoomController.onTouchEvent(event)
        event.recycle()
    }

    private companion object {
        const val TAG = "TubeAUTO"
        const val DRAG_END_DELAY_MS = 350L
        const val START_SLOP = 10f
        const val FLING_SECONDS = 0.3f
        const val PINCH_START_SPREAD = 200f
        const val PINCH_END_DELAY_MS = 300L
        const val DOUBLE_TAP_WAIT_MS = 100L
        const val DOUBLE_TAP_GAP_MS = 100L
        const val CLICK_MOVE_MS = 30L
        const val CLICK_UP_MS = 80L
    }
}

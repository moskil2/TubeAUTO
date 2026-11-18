package app.tubeauto

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.MediaSession

/**
 * Pierwszeństwo dźwięku (audio focus) dla filmów w GeckoView.
 *
 * - Film zaczyna grać: prosimy Androida o pierwszeństwo. Inna muzyka się zatrzymuje,
 *   a Android Auto od razu przekazuje dźwięk do auta.
 * - Inna muzyka albo rozmowa: film się zatrzymuje (po krótkiej przerwie, np. rozmowie,
 *   wznawia się sam).
 * - Komunikat nawigacji: Android sam przycisza film na czas komunikatu.
 */
class AudioFocus(context: Context) : MediaSession.Delegate {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var media: MediaSession? = null
    private var hasFocus = false
    private var pausedByUs = false

    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()
        )
        .setWillPauseWhenDucked(false)
        .setOnAudioFocusChangeListener { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    hasFocus = false
                    pausedByUs = false
                    media?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    pausedByUs = true
                    media?.pause()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasFocus = true
                    if (pausedByUs) {
                        pausedByUs = false
                        media?.play()
                    }
                }
            }
        }
        .build()

    override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
        media = mediaSession
    }

    override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
        media = null
        release()
    }

    override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
        media = mediaSession
        if (!hasFocus) {
            hasFocus = audioManager.requestAudioFocus(request) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
        // Pauza od użytkownika: oddajemy pierwszeństwo. Pauza od nas (rozmowa): trzymamy.
        if (!pausedByUs) release()
    }

    override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
        release()
    }

    fun release() {
        if (hasFocus) audioManager.abandonAudioFocusRequest(request)
        hasFocus = false
        pausedByUs = false
    }
}

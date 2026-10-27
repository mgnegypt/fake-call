package com.mgneg.fakecall.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

/** Plays the answer audio file on the voice-communication route. */
class CallAudioPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    fun play(uriString: String) {
        stop()
        val am = context.getSystemService(AudioManager::class.java) ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attrs)
            .build()
        am.requestAudioFocus(focusRequest!!)
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        // prepareAsync: a slow or broken audio file must never block the
        // calling thread (this runs on the main thread on the app path),
        // and a failure must surface in the log instead of playing silence.
        player = MediaPlayer().apply {
            setAudioAttributes(attrs)
            setOnPreparedListener { mp -> runCatching { mp.start() } }
            setOnErrorListener { _, what, extra ->
                Log.w("FakeCallTelecom", "answer audio failed: what=$what extra=$extra")
                this@CallAudioPlayer.stop()
                true
            }
            runCatching {
                setDataSource(context, Uri.parse(uriString))
                prepareAsync()
            }.onFailure {
                Log.w("FakeCallTelecom", "answer audio could not be opened: $uriString", it)
                this@CallAudioPlayer.stop()
            }
        }
    }

    /** Honor the system call UI's mute toggle for the answer clip. */
    fun setMuted(muted: Boolean) {
        val level = if (muted) 0f else 1f
        player?.setVolume(level, level)
    }

    fun stop() {
        player?.runCatching { if (isPlaying) stop() }
        player?.release()
        player = null
        val am = context.getSystemService(AudioManager::class.java) ?: return
        focusRequest?.let { am.abandonAudioFocusRequest(it) }
        focusRequest = null
        // Reset the audio route so a speaker choice never leaks past the call.
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            am.clearCommunicationDevice()
        } else {
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = false
        }
        if (am.mode == AudioManager.MODE_IN_COMMUNICATION) am.mode = AudioManager.MODE_NORMAL
    }
}

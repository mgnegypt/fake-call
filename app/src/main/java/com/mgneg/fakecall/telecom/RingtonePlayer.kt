package com.mgneg.fakecall.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Ringtone (user-selected or system default) + optional vibration. */
class RingtonePlayer(
    private val context: Context,
    ringtoneUri: String? = null,
    private val vibrate: Boolean = true
) {

    private val ringtone: Ringtone? = runCatching {
        RingtoneManager.getRingtone(
            context,
            ringtoneUri?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )
    }.getOrNull()
    private var vibrator: Vibrator? = null

    fun start() {
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.isLooping = true
        ringtone?.play()
        if (!vibrate) return
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0))
    }

    fun stop() {
        ringtone?.stop()
        vibrator?.cancel()
    }
}

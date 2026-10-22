package com.mgneg.fakecall.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
/**
 * Ringtone playback for the in-app call screen (user-selected or
 * system default). Vibration is deliberately not driven by the app:
 * it follows the device's own settings.
 */
class RingtonePlayer(
    private val context: Context,
    ringtoneUri: String? = null
) {

    private val ringtone: Ringtone? = runCatching {
        RingtoneManager.getRingtone(
            context,
            ringtoneUri?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )
    }.getOrNull()
    fun start() {
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.isLooping = true
        ringtone?.play()
    }

    fun stop() {
        ringtone?.stop()
    }
}

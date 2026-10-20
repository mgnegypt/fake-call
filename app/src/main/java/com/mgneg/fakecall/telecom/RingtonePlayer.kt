package com.mgneg.fakecall.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Default ringtone + vibration while the app call screen is ringing. */
class RingtonePlayer(private val context: Context) {

    private val ringtone = RingtoneManager.getRingtone(
        context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    )
    private var vibrator: Vibrator? = null

    fun start() {
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.isLooping = true
        ringtone?.play()
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

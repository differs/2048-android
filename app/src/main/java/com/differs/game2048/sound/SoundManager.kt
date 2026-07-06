package com.differs.game2048.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes
import com.differs.game2048.R

/**
 * Lightweight sound + haptics helper. Sound effects are generated procedurally
 * as raw resources; if the raw files are absent the load simply no-ops so the
 * game still runs (and vibration still works).
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val moveSound: Int = safeLoad(R.raw.move)
    private val mergeSound: Int = safeLoad(R.raw.merge)

    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun safeLoad(@RawRes resId: Int): Int =
        runCatching { soundPool.load(appContext, resId, 1) }.getOrDefault(0)

    fun playMove(enabled: Boolean) {
        if (enabled && moveSound != 0) soundPool.play(moveSound, 0.4f, 0.4f, 1, 0, 1f)
    }

    fun playMerge(enabled: Boolean) {
        if (enabled && mergeSound != 0) soundPool.play(mergeSound, 0.6f, 0.6f, 1, 0, 1f)
    }

    fun vibrateTick(enabled: Boolean) {
        if (!enabled) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(18)
        }
    }

    fun release() {
        soundPool.release()
    }
}

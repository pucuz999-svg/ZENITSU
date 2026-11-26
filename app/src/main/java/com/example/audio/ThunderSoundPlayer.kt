package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.R

/**
 * Plays the bundled thunderclap & electric surge sound effect when Boost Mode activates.
 * Safely cleans up previous instances and guards all platform audio calls for resilience.
 */
class ThunderSoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private var activePlayer: MediaPlayer? = null

    fun playThunder(): Boolean {
        return runCatching {
            stopCurrentInternal()
            val afd = appContext.resources.openRawResourceFd(R.raw.thunder_boost) ?: return false
            val player = MediaPlayer()
            try {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                player.setVolume(1.0f, 1.0f)
                player.setOnCompletionListener { mp ->
                    if (activePlayer === mp) {
                        activePlayer = null
                    }
                    runCatching { mp.release() }
                }
                player.setOnErrorListener { mp, what, extra ->
                    Log.w(TAG, "MediaPlayer error what=$what extra=$extra")
                    if (activePlayer === mp) {
                        activePlayer = null
                    }
                    runCatching { mp.release() }
                    true
                }
                player.prepare()
                player.start()
                activePlayer = player
                true
            } catch (t: Throwable) {
                runCatching { afd.close() }
                runCatching { player.release() }
                throw t
            }
        }.onFailure { err ->
            Log.w(TAG, "Unable to play thunder boost sound", err)
        }.getOrDefault(false)
    }

    fun release() {
        runCatching { stopCurrentInternal() }
    }

    private fun stopCurrentInternal() {
        val current = activePlayer ?: return
        activePlayer = null
        runCatching {
            if (current.isPlaying) {
                current.stop()
            }
        }
        runCatching { current.release() }
    }

    private companion object {
        const val TAG = "ThunderSoundPlayer"
    }
}

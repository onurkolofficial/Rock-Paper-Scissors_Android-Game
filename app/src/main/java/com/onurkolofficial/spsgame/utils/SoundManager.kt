package com.onurkolofficial.spsgame.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences

class SoundManager(private val context: Context, private val prefs: GamePreferences) {
    private var soundPool: SoundPool? = null
    private var bgmPlayer: MediaPlayer? = null

    private var clickSoundId = -1
    private var winSoundId = -1
    private var loseSoundId = -1
    private var drawSoundId = -1
    private val loadedSounds = mutableSetOf<Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSounds.add(sampleId)
                    }
                }
            }

        soundPool?.let { pool ->
            clickSoundId = pool.load(context, R.raw.game_effect_button_click, 1)
            winSoundId = pool.load(context, R.raw.game_effect_win, 1)
            loseSoundId = pool.load(context, R.raw.game_effect_lose, 1)
            drawSoundId = pool.load(context, R.raw.game_effect_draw, 1)
        }
    }

    fun playClick() {
        playSound(clickSoundId)
    }

    fun playWin() {
        playSound(winSoundId)
    }

    fun playLose() {
        playSound(loseSoundId)
    }

    fun playDraw() {
        playSound(drawSoundId)
    }

    private fun playSound(soundId: Int) {
        if (!prefs.soundEnabled || soundId == -1) return
        val vol = prefs.sfxVolume
        soundPool?.play(soundId, vol, vol, 1, 0, 1.0f)
    }

    fun startBgm() {
        if (!prefs.soundEnabled) return
        try {
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer.create(context, R.raw.game_bgm)?.apply {
                    isLooping = true
                }
            }
            bgmPlayer?.let { player ->
                val vol = prefs.musicVolume
                player.setVolume(vol, vol)
                if (!player.isPlaying) {
                    player.start()
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error starting BGM", e)
        }
    }

    fun pauseBgm() {
        try {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error pausing BGM", e)
        }
    }

    fun resumeBgm() {
        if (!prefs.soundEnabled) return
        try {
            if (bgmPlayer != null) {
                val vol = prefs.musicVolume
                bgmPlayer?.setVolume(vol, vol)
                if (bgmPlayer?.isPlaying == false) {
                    bgmPlayer?.start()
                }
            } else {
                startBgm()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error resuming BGM", e)
        }
    }

    fun stopBgm() {
        try {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            bgmPlayer = null
        } catch (e: Exception) {
            Log.e("SoundManager", "Error stopping BGM", e)
        }
    }

    fun updateBgmVolume() {
        if (!prefs.soundEnabled) {
            pauseBgm()
            return
        }
        bgmPlayer?.let { player ->
            val vol = prefs.musicVolume
            player.setVolume(vol, vol)
            if (!player.isPlaying) {
                player.start()
            }
        } ?: run {
            startBgm()
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        stopBgm()
    }
}

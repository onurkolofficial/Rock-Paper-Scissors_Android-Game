package com.onurkolofficial.spsgame.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sps_game_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USER_NAME = "sps_user_name"
        private const val KEY_USER_IMAGE_URL = "sps_user_image_url"
        private const val KEY_STATS_WINS = "sps_stats_wins"
        private const val KEY_STATS_DRAWS = "sps_stats_draws"
        private const val KEY_STATS_LOSSES = "sps_stats_losses"
        private const val KEY_STATS_ONLINE_WINS = "sps_stats_online_wins"
        private const val KEY_STATS_ONLINE_DRAWS = "sps_stats_online_draws"
        private const val KEY_STATS_ONLINE_LOSSES = "sps_stats_online_losses"
        private const val KEY_STATS_ONLINE_ABANDONS = "sps_stats_online_abandons"
        private const val KEY_STATS_ONLINE_HISTORY = "sps_stats_online_history"
        private const val KEY_SOUND = "sps_sound"
        private const val KEY_VOLUME = "sps_volume"
        private const val KEY_MUSIC_VOLUME = "sps_music_volume"
        private const val KEY_SFX_VOLUME = "sps_sfx_volume"
        private const val KEY_VIBRATION = "sps_vibration"
        private const val KEY_ADS_INTERSTITIAL = "sps_ads_interstitial"
        private const val KEY_LANG = "sps_lang"
        private const val KEY_IRON_COUNT = "sps_iron_count"
        private const val KEY_ICE_COUNT = "sps_ice_count"
        private const val KEY_STEEL_COUNT = "sps_steel_count"
        private const val KEY_FIRE_COUNT = "sps_fire_count"
        private const val KEY_LIGHTNING_COUNT = "sps_lightning_count"
        private const val KEY_BOMB_COUNT = "sps_bomb_count"
        private const val KEY_STATS_CASH = "sps_stats_cash"
        private const val KEY_OWNED_SKINS = "sps_owned_skins"
        private const val KEY_ACTIVE_SKIN = "sps_active_skin"
        private const val KEY_USER_CHANGED_NAME = "sps_user_changed_name"
        private const val KEY_LAST_SEEN_UPDATE_VERSION = "sps_last_seen_update_version"
        private const val KEY_LAST_SEEN_NOTIFICATIONS_VERSION = "sps_last_seen_notifications_version"
    }

    var lastSeenUpdateDialogVersion: String
        get() = prefs.getString(KEY_LAST_SEEN_UPDATE_VERSION, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SEEN_UPDATE_VERSION, value).apply()

    var lastSeenNotificationsVersion: String
        get() = prefs.getString(KEY_LAST_SEEN_NOTIFICATIONS_VERSION, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SEEN_NOTIFICATIONS_VERSION, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Guest") ?: "Guest"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userImageUrl: String
        get() = prefs.getString(KEY_USER_IMAGE_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_IMAGE_URL, value).apply()

    var statsWins: Int
        get() = prefs.getInt(KEY_STATS_WINS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_WINS, value).apply()

    var statsDraws: Int
        get() = prefs.getInt(KEY_STATS_DRAWS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_DRAWS, value).apply()

    var statsLosses: Int
        get() = prefs.getInt(KEY_STATS_LOSSES, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_LOSSES, value).apply()

    var statsOnlineWins: Int
        get() = prefs.getInt(KEY_STATS_ONLINE_WINS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_ONLINE_WINS, value).apply()

    var statsOnlineDraws: Int
        get() = prefs.getInt(KEY_STATS_ONLINE_DRAWS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_ONLINE_DRAWS, value).apply()

    var statsOnlineLosses: Int
        get() = prefs.getInt(KEY_STATS_ONLINE_LOSSES, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_ONLINE_LOSSES, value).apply()

    var statsOnlineAbandons: Int
        get() = prefs.getInt(KEY_STATS_ONLINE_ABANDONS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_ONLINE_ABANDONS, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var volume: Float
        get() = prefs.getFloat(KEY_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOLUME, value).apply()

    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOLUME, value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SFX_VOLUME, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var adsInterstitialEnabled: Boolean
        get() = prefs.getBoolean(KEY_ADS_INTERSTITIAL, true)
        set(value) = prefs.edit().putBoolean(KEY_ADS_INTERSTITIAL, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANG, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANG, value).apply()

    var ironCount: Int
        get() = prefs.getInt(KEY_IRON_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_IRON_COUNT, value).apply()

    var iceCount: Int
        get() = prefs.getInt(KEY_ICE_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_ICE_COUNT, value).apply()

    var steelCount: Int
        get() = prefs.getInt(KEY_STEEL_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_STEEL_COUNT, value).apply()

    var fireCount: Int
        get() = prefs.getInt(KEY_FIRE_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_FIRE_COUNT, value).apply()

    var lightningCount: Int
        get() = prefs.getInt(KEY_LIGHTNING_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_LIGHTNING_COUNT, value).apply()

    var bombCount: Int
        get() = prefs.getInt(KEY_BOMB_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_BOMB_COUNT, value).apply()

    var statsCash: Int
        get() = prefs.getInt(KEY_STATS_CASH, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_CASH, value).apply()

    var activeSkin: String
        get() = prefs.getString(KEY_ACTIVE_SKIN, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ACTIVE_SKIN, value).apply()

    var userChangedName: Boolean
        get() = prefs.getBoolean(KEY_USER_CHANGED_NAME, false)
        set(value) = prefs.edit().putBoolean(KEY_USER_CHANGED_NAME, value).apply()

    var ownedSkins: List<String>
        get() {
            val json = prefs.getString(KEY_OWNED_SKINS, null) ?: return listOf("default")
            return try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type) ?: listOf("default")
            } catch (e: Exception) {
                listOf("default")
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString(KEY_OWNED_SKINS, json).apply()
        }

    var onlineHistory: List<String>
        get() {
            val json = prefs.getString(KEY_STATS_ONLINE_HISTORY, null) ?: return emptyList()
            return try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString(KEY_STATS_ONLINE_HISTORY, json).apply()
        }

    fun getAllLocalDataMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            if (value != null) {
                map[key] = value.toString()
            }
        }
        return map
    }

    fun applyAllLocalDataMap(data: Map<String, String>) {
        val editor = prefs.edit()
        data.forEach { (key, value) ->
            when (key) {
                KEY_USER_NAME -> editor.putString(key, value)
                KEY_USER_IMAGE_URL -> editor.putString(key, value)
                KEY_STATS_WINS -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_DRAWS -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_LOSSES -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_ONLINE_WINS -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_ONLINE_DRAWS -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_ONLINE_LOSSES -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_ONLINE_HISTORY -> editor.putString(key, value) // is already json string
                KEY_SOUND -> editor.putBoolean(key, value.toBoolean())
                KEY_VOLUME -> editor.putFloat(key, value.toFloatOrNull() ?: 1.0f)
                KEY_MUSIC_VOLUME -> editor.putFloat(key, value.toFloatOrNull() ?: 1.0f)
                KEY_SFX_VOLUME -> editor.putFloat(key, value.toFloatOrNull() ?: 1.0f)
                KEY_VIBRATION -> editor.putBoolean(key, value.toBoolean())
                KEY_ADS_INTERSTITIAL -> editor.putBoolean(key, value.toBoolean())
                KEY_LANG -> editor.putString(key, value)
                KEY_IRON_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_ICE_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STEEL_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_FIRE_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_LIGHTNING_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_BOMB_COUNT -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_STATS_CASH -> editor.putInt(key, value.toIntOrNull() ?: 0)
                KEY_ACTIVE_SKIN -> editor.putString(key, value)
                KEY_OWNED_SKINS -> editor.putString(key, value) // is already json string
                KEY_USER_CHANGED_NAME -> editor.putBoolean(key, value.toBoolean())
            }
        }
        editor.apply()
    }

    fun clearStats() {
        prefs.edit().apply {
            putInt(KEY_STATS_WINS, 0)
            putInt(KEY_STATS_DRAWS, 0)
            putInt(KEY_STATS_LOSSES, 0)
            putInt(KEY_STATS_ONLINE_WINS, 0)
            putInt(KEY_STATS_ONLINE_DRAWS, 0)
            putInt(KEY_STATS_ONLINE_LOSSES, 0)
            putString(KEY_STATS_ONLINE_HISTORY, gson.toJson(emptyList<String>()))
            putInt(KEY_STATS_CASH, 0)
            putInt(KEY_IRON_COUNT, 0)
            putInt(KEY_ICE_COUNT, 0)
            putInt(KEY_STEEL_COUNT, 0)
            putInt(KEY_FIRE_COUNT, 0)
            putInt(KEY_LIGHTNING_COUNT, 0)
            putInt(KEY_BOMB_COUNT, 0)
            putString(KEY_ACTIVE_SKIN, "default")
            putString(KEY_OWNED_SKINS, gson.toJson(listOf("default")))
            apply()
        }
    }
}

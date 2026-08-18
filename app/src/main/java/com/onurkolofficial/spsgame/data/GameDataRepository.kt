package com.onurkolofficial.spsgame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sps_game_datastore")

data class UserGameData(
    val userName: String = "Guest",
    val userImageUrl: String = "",
    val statsWins: Int = 0,
    val statsDraws: Int = 0,
    val statsLosses: Int = 0,
    val statsOnlineWins: Int = 0,
    val statsOnlineDraws: Int = 0,
    val statsOnlineLosses: Int = 0,
    val statsOnlineAbandons: Int = 0,
    val statsCash: Int = 0,
    val ironCount: Int = 0,
    val iceCount: Int = 0,
    val steelCount: Int = 0,
    val activeSkin: String = "default",
    val ownedSkins: List<String> = listOf("default"),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: String = "en"
)

class GameDataRepository(private val context: Context) {

    private val gson = Gson()

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("sps_user_name")
        val KEY_USER_IMAGE_URL = stringPreferencesKey("sps_user_image_url")
        val KEY_STATS_WINS = intPreferencesKey("sps_stats_wins")
        val KEY_STATS_DRAWS = intPreferencesKey("sps_stats_draws")
        val KEY_STATS_LOSSES = intPreferencesKey("sps_stats_losses")
        val KEY_STATS_ONLINE_WINS = intPreferencesKey("sps_stats_online_wins")
        val KEY_STATS_ONLINE_DRAWS = intPreferencesKey("sps_stats_online_draws")
        val KEY_STATS_ONLINE_LOSSES = intPreferencesKey("sps_stats_online_losses")
        val KEY_STATS_ONLINE_ABANDONS = intPreferencesKey("sps_stats_online_abandons")
        val KEY_STATS_CASH = intPreferencesKey("sps_stats_cash")
        val KEY_IRON_COUNT = intPreferencesKey("sps_iron_count")
        val KEY_ICE_COUNT = intPreferencesKey("sps_ice_count")
        val KEY_STEEL_COUNT = intPreferencesKey("sps_steel_count")
        val KEY_ACTIVE_SKIN = stringPreferencesKey("sps_active_skin")
        val KEY_OWNED_SKINS = stringPreferencesKey("sps_owned_skins")
        val KEY_SOUND = booleanPreferencesKey("sps_sound")
        val KEY_VIBRATION = booleanPreferencesKey("sps_vibration")
        val KEY_LANG = stringPreferencesKey("sps_lang")
    }

    val userGameDataFlow: Flow<UserGameData> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val ownedSkinsJson = preferences[KEY_OWNED_SKINS]
            val ownedSkinsList: List<String> = if (ownedSkinsJson != null) {
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson(ownedSkinsJson, type) ?: listOf("default")
                } catch (e: Exception) {
                    listOf("default")
                }
            } else {
                listOf("default")
            }

            UserGameData(
                userName = preferences[KEY_USER_NAME] ?: "Guest",
                userImageUrl = preferences[KEY_USER_IMAGE_URL] ?: "",
                statsWins = preferences[KEY_STATS_WINS] ?: 0,
                statsDraws = preferences[KEY_STATS_DRAWS] ?: 0,
                statsLosses = preferences[KEY_STATS_LOSSES] ?: 0,
                statsOnlineWins = preferences[KEY_STATS_ONLINE_WINS] ?: 0,
                statsOnlineDraws = preferences[KEY_STATS_ONLINE_DRAWS] ?: 0,
                statsOnlineLosses = preferences[KEY_STATS_ONLINE_LOSSES] ?: 0,
                statsOnlineAbandons = preferences[KEY_STATS_ONLINE_ABANDONS] ?: 0,
                statsCash = preferences[KEY_STATS_CASH] ?: 0,
                ironCount = preferences[KEY_IRON_COUNT] ?: 0,
                iceCount = preferences[KEY_ICE_COUNT] ?: 0,
                steelCount = preferences[KEY_STEEL_COUNT] ?: 0,
                activeSkin = preferences[KEY_ACTIVE_SKIN] ?: "default",
                ownedSkins = ownedSkinsList,
                soundEnabled = preferences[KEY_SOUND] ?: true,
                vibrationEnabled = preferences[KEY_VIBRATION] ?: true,
                language = preferences[KEY_LANG] ?: "en"
            )
        }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun addCash(amount: Int) {
        context.dataStore.edit {
            val current = it[KEY_STATS_CASH] ?: 0
            it[KEY_STATS_CASH] = current + amount
        }
    }

    suspend fun updateActiveSkin(skinId: String) {
        context.dataStore.edit { it[KEY_ACTIVE_SKIN] = skinId }
    }
}

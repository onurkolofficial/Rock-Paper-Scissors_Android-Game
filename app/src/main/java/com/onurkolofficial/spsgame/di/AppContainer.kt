package com.onurkolofficial.spsgame.di

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.AdManager
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

class AppContainer(activity: Activity) {
    val prefs: GamePreferences by lazy { GamePreferences(activity.applicationContext) }
    val soundManager: SoundManager by lazy { SoundManager(activity.applicationContext, prefs) }
    val vibrationManager: VibrationManager by lazy { VibrationManager(activity.applicationContext, prefs) }
    val playGamesManager: PlayGamesManager by lazy { PlayGamesManager(activity, prefs) }
    val adManager: AdManager by lazy { AdManager(activity.applicationContext, prefs) }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided. Ensure LocalAppContainer is provided at the root Composable.")
}

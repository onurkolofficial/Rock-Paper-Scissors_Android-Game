package com.onurkolofficial.spsgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.ui.screens.*
import com.onurkolofficial.spsgame.ui.theme.RPSGameTheme
import com.onurkolofficial.spsgame.utils.*
import android.content.Context
import java.util.Locale
import android.content.res.Configuration

class MainActivity : ComponentActivity() {
    private lateinit var prefs: GamePreferences
    private lateinit var soundManager: SoundManager
    private lateinit var vibrationManager: VibrationManager
    private lateinit var playGamesManager: PlayGamesManager
    private lateinit var adManager: AdManager

    override fun attachBaseContext(newBase: Context) {
        val tempPrefs = GamePreferences(newBase)
        val context = com.onurkolofficial.spsgame.ui.localization.LocaleHelper.updateLocale(newBase, tempPrefs.language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = GamePreferences(this)
        soundManager = SoundManager(this, prefs)
        vibrationManager = VibrationManager(this, prefs)
        playGamesManager = PlayGamesManager(this, prefs)
        adManager = AdManager(this, prefs)

        enableEdgeToEdge()

        setContent {
            var appLanguage by remember { mutableStateOf(prefs.language) }
            val baseContext = androidx.compose.ui.platform.LocalContext.current
            
            val newContext = com.onurkolofficial.spsgame.ui.localization.LocaleHelper.updateLocale(baseContext, appLanguage)
            val config = newContext.resources.configuration

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides config,
                androidx.compose.ui.platform.LocalContext provides newContext,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                RPSGameTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF0F1112)
                    ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    var targetGameScreen by remember { mutableStateOf<String?>(null) }

                    BackHandler(enabled = currentScreen != "menu" && currentScreen != "splash" && currentScreen != "game_loading") {
                        if (currentScreen == "online") {
                            // Handled by its own screen confirmation triggers
                        } else {
                            soundManager.playClick()
                            if (currentScreen == "single" || currentScreen == "two") {
                                adManager.showInterstitialAd()
                            }
                            currentScreen = "menu"
                        }
                    }

                    when (currentScreen) {
                        "splash" -> {
                            SplashScreen(
                                onNavigateToMenu = { currentScreen = "menu" }
                            )
                        }
                        "menu" -> {
                            MainMenuScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateToSinglePlayer = { 
                                    targetGameScreen = "single"
                                    currentScreen = "game_loading" 
                                },
                                onNavigateToTwoPlayer = { 
                                    targetGameScreen = "two"
                                    currentScreen = "game_loading" 
                                },
                                onNavigateToOnlineMultiplayer = { 
                                    currentScreen = "online" 
                                },
                                onNavigateToSettings = { currentScreen = "settings" },
                                onNavigateToStats = { currentScreen = "stats" },
                                onNavigateToAchievements = { currentScreen = "achievements" },
                                onNavigateToLeaderboard = { currentScreen = "leaderboard" },
                                onNavigateToProfile = { currentScreen = "profile" }
                            )
                        }
                        "game_loading" -> {
                            GameLoadingScreen(
                                targetMode = targetGameScreen ?: "menu",
                                onNavigateNext = {
                                    currentScreen = targetGameScreen ?: "menu"
                                }
                            )
                        }
                        "single" -> {
                            SinglePlayerScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = {
                                    adManager.showInterstitialAd()
                                    currentScreen = "menu"
                                }
                            )
                        }
                        "two" -> {
                            TwoPlayerScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = {
                                    adManager.showInterstitialAd()
                                    currentScreen = "menu"
                                }
                            )
                        }
                        "online" -> {
                            OnlineMultiplayerScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = {
                                    adManager.showInterstitialAd()
                                    currentScreen = "menu"
                                }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                onNavigateBack = { currentScreen = "menu" },
                                onLanguageChanged = { newLang ->
                                    appLanguage = newLang
                                }
                            )
                        }
                        "stats" -> {
                            StatsScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                onNavigateBack = { currentScreen = "menu" }
                            )
                        }
                        "achievements" -> {
                            AchievementsScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = { currentScreen = "menu" }
                            )
                        }
                        "leaderboard" -> {
                            LeaderboardScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = { currentScreen = "menu" }
                            )
                        }
                        "profile" -> {
                            ProfileScreen(
                                prefs = prefs,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                playGamesManager = playGamesManager,
                                onNavigateBack = { currentScreen = "menu" }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    override fun onResume() {
        super.onResume()
        soundManager.startBgm()
    }

    override fun onPause() {
        super.onPause()
        soundManager.stopBgm()
        playGamesManager.saveGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

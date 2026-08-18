package com.onurkolofficial.spsgame

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.di.AppContainer
import com.onurkolofficial.spsgame.di.LocalAppContainer
import com.onurkolofficial.spsgame.ui.localization.LocaleHelper
import com.onurkolofficial.spsgame.ui.navigation.AppNavHost
import com.onurkolofficial.spsgame.ui.theme.RPSGameTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun attachBaseContext(newBase: Context) {
        val tempPrefs = GamePreferences(newBase)
        val context = LocaleHelper.updateLocale(newBase, tempPrefs.language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this)

        enableEdgeToEdge()

        setContent {
            var appLanguage by remember { mutableStateOf(appContainer.prefs.language) }
            val baseContext = androidx.compose.ui.platform.LocalContext.current

            val newContext = LocaleHelper.updateLocale(baseContext, appLanguage)
            val config = newContext.resources.configuration
            val navController = rememberNavController()

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides config,
                androidx.compose.ui.platform.LocalContext provides newContext,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalAppContainer provides appContainer
            ) {
                RPSGameTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF0F1112)
                    ) {
                        AppNavHost(
                            navController = navController,
                            onLanguageChanged = { newLang ->
                                appLanguage = newLang
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appContainer.isInitialized) {
            appContainer.soundManager.resumeBgm()
            appContainer.socketManager.connect()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::appContainer.isInitialized) {
            appContainer.soundManager.pauseBgm()
            appContainer.playGamesManager.saveGame()
            appContainer.socketManager.disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appContainer.isInitialized) {
            appContainer.soundManager.release()
            appContainer.socketManager.disconnect()
        }
    }
}

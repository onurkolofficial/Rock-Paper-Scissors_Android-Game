package com.onurkolofficial.spsgame.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.onurkolofficial.spsgame.di.LocalAppContainer
import com.onurkolofficial.spsgame.ui.screens.*
import com.onurkolofficial.spsgame.ui.viewmodels.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    onLanguageChanged: (String) -> Unit
) {
    val appContainer = LocalAppContainer.current

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Splash,
        enterTransition = { fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
    ) {
        composable<ScreenRoute.Splash> {
            SplashScreen(
                onNavigateToMenu = {
                    navController.navigate(ScreenRoute.MainMenu) {
                        popUpTo(ScreenRoute.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenRoute.MainMenu> {
            val viewModel = remember {
                MainMenuViewModel(
                    prefs = appContainer.prefs,
                    playGamesManager = appContainer.playGamesManager,
                    socketManager = appContainer.socketManager
                )
            }

            MainMenuScreen(
                viewModel = viewModel,
                onNavigateToSinglePlayer = {
                    navController.navigate(ScreenRoute.GameLoading(targetMode = "single"))
                },
                onNavigateToTwoPlayer = {
                    navController.navigate(ScreenRoute.GameLoading(targetMode = "two"))
                },
                onNavigateToOnlineMultiplayer = {
                    navController.navigate(ScreenRoute.OnlineMultiplayer)
                },
                onNavigateToSettings = {
                    navController.navigate(ScreenRoute.Settings)
                },
                onNavigateToStats = {
                    navController.navigate(ScreenRoute.Stats)
                },
                onNavigateToAchievements = {
                    navController.navigate(ScreenRoute.Achievements)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(ScreenRoute.Leaderboard)
                },
                onNavigateToProfile = {
                    navController.navigate(ScreenRoute.Profile)
                }
            )
        }

        composable<ScreenRoute.GameLoading> { backStackEntry ->
            val route = backStackEntry.toRoute<ScreenRoute.GameLoading>()
            GameLoadingScreen(
                targetMode = route.targetMode,
                onNavigateNext = {
                    val dest: ScreenRoute = if (route.targetMode == "single") {
                        ScreenRoute.SinglePlayer
                    } else {
                        ScreenRoute.TwoPlayer
                    }
                    navController.navigate(dest) {
                        popUpTo(ScreenRoute.MainMenu)
                    }
                }
            )
        }

        composable<ScreenRoute.SinglePlayer> {
            LaunchedEffect(Unit) {
                appContainer.socketManager.disconnect()
            }

            val viewModel = remember {
                SinglePlayerViewModel(
                    prefs = appContainer.prefs,
                    soundManager = appContainer.soundManager,
                    vibrationManager = appContainer.vibrationManager,
                    playGamesManager = appContainer.playGamesManager
                )
            }

            SinglePlayerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    appContainer.adManager.showInterstitialAd()
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.TwoPlayer> {
            LaunchedEffect(Unit) {
                appContainer.socketManager.disconnect()
            }

            val viewModel = remember {
                TwoPlayerViewModel(
                    prefs = appContainer.prefs,
                    soundManager = appContainer.soundManager,
                    vibrationManager = appContainer.vibrationManager,
                    playGamesManager = appContainer.playGamesManager
                )
            }

            TwoPlayerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    appContainer.adManager.showInterstitialAd()
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.OnlineMultiplayer> {
            val viewModel = remember {
                OnlineMultiplayerViewModel(
                    prefs = appContainer.prefs,
                    soundManager = appContainer.soundManager,
                    vibrationManager = appContainer.vibrationManager,
                    playGamesManager = appContainer.playGamesManager,
                    socketManager = appContainer.socketManager
                )
            }

            OnlineMultiplayerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    appContainer.adManager.showInterstitialAd()
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.Settings> {
            SettingsScreen(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                vibrationManager = appContainer.vibrationManager,
                onNavigateBack = { navController.popBackStack() },
                onLanguageChanged = onLanguageChanged
            )
        }

        composable<ScreenRoute.Stats> {
            StatsScreen(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                vibrationManager = appContainer.vibrationManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenRoute.Achievements> {
            AchievementsScreen(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                vibrationManager = appContainer.vibrationManager,
                playGamesManager = appContainer.playGamesManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenRoute.Leaderboard> {
            LeaderboardScreen(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                vibrationManager = appContainer.vibrationManager,
                playGamesManager = appContainer.playGamesManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenRoute.Profile> {
            ProfileScreen(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                vibrationManager = appContainer.vibrationManager,
                playGamesManager = appContainer.playGamesManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

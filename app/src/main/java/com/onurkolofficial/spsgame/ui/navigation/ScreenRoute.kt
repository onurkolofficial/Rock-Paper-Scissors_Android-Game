package com.onurkolofficial.spsgame.ui.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenRoute {
    @Serializable
    data object Splash : ScreenRoute

    @Serializable
    data object MainMenu : ScreenRoute

    @Serializable
    data class GameLoading(val targetMode: String) : ScreenRoute

    @Serializable
    data object SinglePlayer : ScreenRoute

    @Serializable
    data object TwoPlayer : ScreenRoute

    @Serializable
    data object OnlineMultiplayer : ScreenRoute

    @Serializable
    data object Settings : ScreenRoute

    @Serializable
    data object Stats : ScreenRoute

    @Serializable
    data object Achievements : ScreenRoute

    @Serializable
    data object Leaderboard : ScreenRoute

    @Serializable
    data class Profile(
        val userName: String? = null,
        val wins: Int? = null,
        val rank: String? = null,
        val isOtherUser: Boolean = false
    ) : ScreenRoute
}

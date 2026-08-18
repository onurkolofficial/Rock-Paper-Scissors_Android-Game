package com.onurkolofficial.spsgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkolofficial.spsgame.BuildConfig
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.GameSocketManager
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainMenuUiState(
    val userName: String = "Guest",
    val statsCash: Int = 0,
    val onlinePlayersCount: Int? = null,
    val showUpdateDialog: Boolean = false,
    val showStore: Boolean = false
)

class MainMenuViewModel(
    private val prefs: GamePreferences,
    private val playGamesManager: PlayGamesManager,
    private val socketManager: GameSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainMenuUiState(
            userName = prefs.userName,
            statsCash = prefs.statsCash,
            showUpdateDialog = prefs.lastSeenUpdateDialogVersion != BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<MainMenuUiState> = _uiState.asStateFlow()

    init {
        checkSilentSignIn()
        socketManager.connect()
        viewModelScope.launch {
            socketManager.onlinePlayerCount.collect { count ->
                _uiState.update { it.copy(onlinePlayersCount = count) }
            }
        }
    }

    fun ensureOnlineConnected() {
        socketManager.connect()
    }

    fun refreshProfile() {
        _uiState.update {
            it.copy(
                userName = prefs.userName,
                statsCash = prefs.statsCash
            )
        }
    }

    fun setShowStore(show: Boolean) {
        _uiState.update { it.copy(showStore = show) }
        if (!show) refreshProfile()
    }

    fun dismissUpdateDialog() {
        prefs.lastSeenUpdateDialogVersion = BuildConfig.VERSION_NAME
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun checkSilentSignIn() {
        viewModelScope.launch {
            playGamesManager.checkSilentSignIn { success, name, _ ->
                if (success && name != null) {
                    prefs.userName = name
                    _uiState.update { it.copy(userName = name) }
                }
            }
        }
    }
}

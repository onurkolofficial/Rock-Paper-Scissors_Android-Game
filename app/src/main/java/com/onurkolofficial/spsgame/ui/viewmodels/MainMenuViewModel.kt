package com.onurkolofficial.spsgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkolofficial.spsgame.BuildConfig
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.GameSocketManager
import com.onurkolofficial.spsgame.utils.InAppUpdateManager
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
    val showStore: Boolean = false,
    val showRules: Boolean = false,
    val showNotifications: Boolean = false,
    val showUpdater: Boolean = false,
    val hasUnreadNotifications: Boolean = false,
    val isAppUpdateAvailable: Boolean = false
)

class MainMenuViewModel(
    private val prefs: GamePreferences,
    private val playGamesManager: PlayGamesManager,
    private val socketManager: GameSocketManager,
    private val updateManager: InAppUpdateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainMenuUiState(
            userName = prefs.userName,
            statsCash = prefs.statsCash,
            showUpdateDialog = prefs.lastSeenUpdateDialogVersion != BuildConfig.VERSION_NAME,
            hasUnreadNotifications = prefs.lastSeenNotificationsVersion != BuildConfig.VERSION_NAME
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
        viewModelScope.launch {
            updateManager.checkForUpdate()
            updateManager.uiState.collect { updateUiState ->
                if (updateUiState.isUpdateAvailable) {
                    _uiState.update {
                        it.copy(
                            isAppUpdateAvailable = true,
                            hasUnreadNotifications = true
                        )
                    }
                }
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

    fun setShowRules(show: Boolean) {
        _uiState.update { it.copy(showRules = show) }
    }

    fun setShowUpdater(show: Boolean) {
        _uiState.update { it.copy(showUpdater = show) }
    }

    fun setShowNotifications(show: Boolean) {
        if (show) {
            prefs.lastSeenNotificationsVersion = BuildConfig.VERSION_NAME
            _uiState.update { it.copy(showNotifications = true, hasUnreadNotifications = false) }
        } else {
            _uiState.update { it.copy(showNotifications = false) }
        }
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

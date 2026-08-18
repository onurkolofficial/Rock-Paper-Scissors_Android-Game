package com.onurkolofficial.spsgame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkolofficial.spsgame.BuildConfig
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.GameAppConfig
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class MainMenuUiState(
    val userName: String = "Guest",
    val statsCash: Int = 0,
    val onlinePlayersCount: Int? = null,
    val showUpdateDialog: Boolean = false,
    val showStore: Boolean = false
)

class MainMenuViewModel(
    private val prefs: GamePreferences,
    private val playGamesManager: PlayGamesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainMenuUiState(
            userName = prefs.userName,
            statsCash = prefs.statsCash,
            showUpdateDialog = prefs.lastSeenUpdateDialogVersion != BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<MainMenuUiState> = _uiState.asStateFlow()

    private var socket: Socket? = null

    init {
        checkSilentSignIn()
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

    fun startListeningPlayerCount() {
        if (socket != null && socket?.connected() == true) return
        try {
            val opts = IO.Options().apply {
                forceNew = false
                reconnection = true
            }
            socket = IO.socket(GameAppConfig.SOCKET_URL, opts)
            socket?.on(Socket.EVENT_CONNECT) {
                socket?.emit("request_player_count")
            }
            socket?.on("player_count") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject
                    val count = data?.optInt("count", 0) ?: 0
                    _uiState.update { it.copy(onlinePlayersCount = count) }
                }
            }
            socket?.connect()
        } catch (e: Exception) {
            Log.e("MainMenuVM", "Error connecting socket for player count", e)
        }
    }

    fun stopListeningPlayerCount() {
        try {
            socket?.off()
            socket?.disconnect()
            socket = null
        } catch (e: Exception) {
            Log.e("MainMenuVM", "Error disconnecting socket", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningPlayerCount()
    }
}

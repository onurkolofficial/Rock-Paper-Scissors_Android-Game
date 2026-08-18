package com.onurkolofficial.spsgame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.screens.MatchStatus
import com.onurkolofficial.spsgame.ui.screens.MultiplayerMode
import com.onurkolofficial.spsgame.utils.GameSocketManager
import com.onurkolofficial.spsgame.utils.PlayGamesConstants
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import io.socket.client.Socket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class OnlineMultiplayerUiState(
    val gameMode: MultiplayerMode = MultiplayerMode.SELECTION,
    val matchStatus: MatchStatus = MatchStatus.CONNECTING,
    val roomCodeInput: String = "",
    val roomId: String? = null,
    val errorMsg: String? = null,
    val opponentName: String = "Opponent",
    val opponentSkinId: String = "default",
    val currentRound: Int = 1,
    val totalDraws: Int = 0,
    val myMove: Move? = null,
    val opponentMove: Move? = null,
    val opponentHasMoved: Boolean = false,
    val roundResult: GameResult? = null,
    val finalResult: GameResult? = null,
    val myScore: Int = 0,
    val opponentScore: Int = 0,
    val timerVal: Int? = null,
    val nextRoundTimerVal: Int? = null,
    val startingTimerVal: Int? = null,
    val activeSkinId: String = "default",
    val ironCount: Int = 0,
    val iceCount: Int = 0,
    val steelCount: Int = 0
)

class OnlineMultiplayerViewModel(
    private val prefs: GamePreferences,
    private val soundManager: SoundManager,
    private val vibrationManager: VibrationManager,
    private val playGamesManager: PlayGamesManager,
    private val socketManager: GameSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnlineMultiplayerUiState(
            activeSkinId = prefs.activeSkin,
            ironCount = prefs.ironCount,
            iceCount = prefs.iceCount,
            steelCount = prefs.steelCount
        )
    )
    val uiState: StateFlow<OnlineMultiplayerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startingTimerJob: Job? = null
    private var nextRoundTimerJob: Job? = null

    init {
        socketManager.connect()
        setupSocketListeners()
    }

    fun setRoomCodeInput(code: String) {
        _uiState.update { it.copy(roomCodeInput = code) }
    }

    fun setGameMode(mode: MultiplayerMode) {
        _uiState.update { it.copy(gameMode = mode, errorMsg = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun joinMatchmaking(name: String, skin: String) {
        _uiState.update { it.copy(gameMode = MultiplayerMode.CONNECTING, matchStatus = MatchStatus.CONNECTING) }
        val data = JSONObject().apply {
            put("name", name)
            put("skin", skin)
        }
        connectAndEmit("join_matchmaking", data)
    }

    fun createPrivateRoom(name: String, skin: String) {
        _uiState.update { it.copy(gameMode = MultiplayerMode.CREATE_ROOM, matchStatus = MatchStatus.CONNECTING) }
        val data = JSONObject().apply {
            put("name", name)
            put("skin", skin)
        }
        connectAndEmit("create_private_room", data)
    }

    fun joinPrivateRoom(code: String, name: String, skin: String) {
        if (code.isBlank()) return
        _uiState.update { it.copy(gameMode = MultiplayerMode.CONNECTING, matchStatus = MatchStatus.CONNECTING) }
        val data = JSONObject().apply {
            put("roomId", code.trim().uppercase())
            put("name", name)
            put("skin", skin)
        }
        connectAndEmit("join_private_room", data)
    }

    fun sendMove(move: Move) {
        val state = _uiState.value
        if (state.myMove != null || state.matchStatus != MatchStatus.PLAYING) return

        var canPlay = true
        when (move) {
            Move.IRON -> {
                if (state.ironCount > 0) {
                    val newCount = state.ironCount - 1
                    prefs.ironCount = newCount
                    _uiState.update { it.copy(ironCount = newCount) }
                } else canPlay = false
            }
            Move.ICE -> {
                if (state.iceCount > 0) {
                    val newCount = state.iceCount - 1
                    prefs.iceCount = newCount
                    _uiState.update { it.copy(iceCount = newCount) }
                } else canPlay = false
            }
            Move.STEEL -> {
                if (state.steelCount > 0) {
                    val newCount = state.steelCount - 1
                    prefs.steelCount = newCount
                    _uiState.update { it.copy(steelCount = newCount) }
                } else canPlay = false
            }
            else -> {}
        }

        if (!canPlay) return

        vibrationManager.vibrateClick()
        soundManager.playClick()

        _uiState.update { it.copy(myMove = move) }

        val data = JSONObject().apply {
            put("move", move.toId())
        }
        socketManager.getSocket().emit("send_move", data)
    }

    private fun connectAndEmit(event: String, data: JSONObject) {
        val s = socketManager.getSocket()
        if (s.connected()) {
            s.emit(event, data)
        } else {
            s.once(Socket.EVENT_CONNECT) {
                s.emit(event, data)
            }
            socketManager.connect()
        }
    }

    private fun setupSocketListeners() {
        val s = socketManager.getSocket()

        s.on("waiting_for_opponent") { args ->
            val res = args[0] as JSONObject
            _uiState.update {
                it.copy(
                    matchStatus = MatchStatus.WAITING,
                    roomId = res.optString("roomId")
                )
            }
        }

        s.on("private_room_created") { args ->
            val res = args[0] as JSONObject
            _uiState.update {
                it.copy(
                    matchStatus = MatchStatus.WAITING,
                    roomId = res.optString("roomId")
                )
            }
        }

        s.on("join_error") { args ->
            val res = args[0] as JSONObject
            _uiState.update {
                it.copy(
                    errorMsg = res.optString("message"),
                    gameMode = MultiplayerMode.SELECTION,
                    matchStatus = MatchStatus.CONNECTING
                )
            }
        }

        s.on("match_found") { args ->
            val res = args[0] as JSONObject
            val players = res.optJSONArray("players")
            var oppName = "Opponent"
            var oppSkin = "default"
            if (players != null) {
                for (i in 0 until players.length()) {
                    val p = players.getJSONObject(i)
                    if (p.optString("id") != s.id()) {
                        oppName = p.optString("name")
                        oppSkin = p.optString("skin", "default")
                    }
                }
            }

            _uiState.update {
                it.copy(
                    matchStatus = MatchStatus.STARTING,
                    opponentName = oppName,
                    opponentSkinId = oppSkin,
                    currentRound = res.optInt("round", 1),
                    totalDraws = res.optInt("draws", 0)
                )
            }

            soundManager.playClick()
            vibrationManager.vibrate(50)

            startStartingCountdown()
        }

        s.on("game_starting") {
            _uiState.update {
                it.copy(
                    matchStatus = MatchStatus.LOADING,
                    gameMode = MultiplayerMode.IN_GAME
                )
            }
        }

        s.on("round_result") { args ->
            val res = args[0] as JSONObject
            val outcome = GameResult.fromId(res.optString("result"))
            val oppMove = Move.fromId(res.optString("opponentMove"))
            val score = res.optInt("score")
            val oppScore = res.optInt("opponentScore")
            val draws = res.optInt("draws")
            val round = res.optInt("round")

            timerJob?.cancel()

            _uiState.update {
                it.copy(
                    roundResult = outcome,
                    opponentMove = oppMove,
                    myScore = score,
                    opponentScore = oppScore,
                    totalDraws = draws,
                    currentRound = round,
                    matchStatus = MatchStatus.RESULT,
                    timerVal = null
                )
            }

            when (outcome) {
                GameResult.WIN -> soundManager.playWin()
                GameResult.LOSE -> soundManager.playLose()
                else -> soundManager.playDraw()
            }
            vibrationManager.vibrate(100)

            if (round < 10) {
                startNextRoundTimer()
            }
        }

        s.on("next_round") { args ->
            val res = args[0] as JSONObject
            nextRoundTimerJob?.cancel()
            _uiState.update {
                it.copy(
                    currentRound = res.optInt("round"),
                    myMove = null,
                    opponentMove = null,
                    opponentHasMoved = false,
                    roundResult = null,
                    matchStatus = MatchStatus.PLAYING,
                    nextRoundTimerVal = null
                )
            }
            startRoundTimer()
        }

        s.on("opponent_moved") {
            _uiState.update { it.copy(opponentHasMoved = true) }
        }

        s.on("game_over") { args ->
            val res = args[0] as JSONObject
            val finalOutcome = GameResult.fromId(res.optString("result"))

            _uiState.update {
                it.copy(
                    finalResult = finalOutcome,
                    matchStatus = MatchStatus.GAME_OVER,
                    timerVal = null,
                    nextRoundTimerVal = null
                )
            }

            when (finalOutcome) {
                GameResult.WIN -> {
                    prefs.statsOnlineWins++
                    prefs.statsCash += 200
                    soundManager.playWin()
                    vibrationManager.vibrateSuccess()
                    playGamesManager.submitScore(PlayGamesConstants.LEADERBOARD_WINS, prefs.statsOnlineWins.toLong())
                }
                GameResult.LOSE -> {
                    prefs.statsOnlineLosses++
                    soundManager.playLose()
                    vibrationManager.vibrateFailure()
                }
                else -> {
                    prefs.statsOnlineDraws++
                    soundManager.playDraw()
                }
            }
            playGamesManager.saveGame()
        }

        s.on("opponent_disconnected") { args ->
            val res = args.getOrNull(0) as? JSONObject
            val wasPlaying = res?.optBoolean("wasPlaying", true) ?: true

            if (wasPlaying && _uiState.value.matchStatus != MatchStatus.GAME_OVER) {
                prefs.statsOnlineWins++
                prefs.statsCash += 200
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
            }

            _uiState.update {
                it.copy(
                    matchStatus = MatchStatus.OPPONENT_DISCONNECTED,
                    timerVal = null,
                    nextRoundTimerVal = null
                )
            }
        }
    }

    private fun startStartingCountdown() {
        startingTimerJob?.cancel()
        startingTimerJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(startingTimerVal = i) }
                delay(1000)
            }
            _uiState.update { it.copy(startingTimerVal = null) }
        }
    }

    fun onLoadingAnimationFinished() {
        _uiState.update { it.copy(matchStatus = MatchStatus.PLAYING) }
        startRoundTimer()
    }

    private fun startRoundTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 10 downTo 1) {
                _uiState.update { it.copy(timerVal = i) }
                delay(1000)
            }
            _uiState.update { it.copy(timerVal = 0) }
            socketManager.getSocket().emit("timeout_from_client")
        }
    }

    private fun startNextRoundTimer() {
        nextRoundTimerJob?.cancel()
        nextRoundTimerJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(nextRoundTimerVal = i) }
                delay(1000)
            }
            _uiState.update { it.copy(nextRoundTimerVal = null) }
        }
    }

    fun leaveMatchmakingOrGame() {
        timerJob?.cancel()
        startingTimerJob?.cancel()
        nextRoundTimerJob?.cancel()
        _uiState.update {
            OnlineMultiplayerUiState(
                activeSkinId = prefs.activeSkin,
                ironCount = prefs.ironCount,
                iceCount = prefs.iceCount,
                steelCount = prefs.steelCount
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        startingTimerJob?.cancel()
        nextRoundTimerJob?.cancel()
    }
}

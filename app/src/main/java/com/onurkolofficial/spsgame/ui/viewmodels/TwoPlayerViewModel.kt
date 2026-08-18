package com.onurkolofficial.spsgame.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.model.GameEngine
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.utils.PlayGamesConstants
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TwoPlayerUiState(
    val p1Score: Int = 0,
    val p2Score: Int = 0,
    val p1Move: Move? = null,
    val p2Move: Move? = null,
    val gameFinished: Boolean = false,
    val resultText: String = "",
    val resultColor: Color = Color.White,
    val timerVal: Int? = null,
    val nextRoundTimerVal: Int? = null,
    val activeSkinId: String = "default"
)

class TwoPlayerViewModel(
    private val prefs: GamePreferences,
    private val soundManager: SoundManager,
    private val vibrationManager: VibrationManager,
    private val playGamesManager: PlayGamesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TwoPlayerUiState(activeSkinId = prefs.activeSkin)
    )
    val uiState: StateFlow<TwoPlayerUiState> = _uiState.asStateFlow()

    private var choiceTimeoutJob: Job? = null
    private var nextRoundJob: Job? = null

    init {
        playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T7_FUN)
    }

    fun onP1MoveSelected(move: Move) {
        val state = _uiState.value
        if (state.gameFinished || state.p1Move != null) return

        vibrationManager.vibrateClick()
        soundManager.playClick()

        _uiState.update { it.copy(p1Move = move) }
        checkBothMoved()
    }

    fun onP2MoveSelected(move: Move) {
        val state = _uiState.value
        if (state.gameFinished || state.p2Move != null) return

        vibrationManager.vibrateClick()
        soundManager.playClick()

        _uiState.update { it.copy(p2Move = move) }
        checkBothMoved()
    }

    private fun checkBothMoved() {
        val state = _uiState.value
        val m1 = state.p1Move
        val m2 = state.p2Move

        if (m1 != null && m2 != null) {
            choiceTimeoutJob?.cancel()
            _uiState.update { it.copy(timerVal = null) }
            evaluateRound(m1, m2)
        } else if (m1 != null || m2 != null) {
            startChoiceTimeout()
        }
    }

    private fun startChoiceTimeout() {
        choiceTimeoutJob?.cancel()
        choiceTimeoutJob = viewModelScope.launch {
            _uiState.update { it.copy(timerVal = 1) }
            delay(1000)
            _uiState.update { it.copy(timerVal = 0) }

            val state = _uiState.value
            if (state.p1Move != null && state.p2Move == null) {
                _uiState.update {
                    it.copy(
                        p1Score = it.p1Score + 1,
                        resultText = "Player 1 Wins!",
                        resultColor = Color.Green,
                        gameFinished = true,
                        timerVal = null
                    )
                }
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
                startNextRoundCountdown()
            } else if (state.p2Move != null && state.p1Move == null) {
                _uiState.update {
                    it.copy(
                        p2Score = it.p2Score + 1,
                        resultText = "Player 2 Wins!",
                        resultColor = Color.Green,
                        gameFinished = true,
                        timerVal = null
                    )
                }
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
                startNextRoundCountdown()
            }
        }
    }

    private fun evaluateRound(m1: Move, m2: Move) {
        val res = GameEngine.determineWinner(m1, m2)
        when (res) {
            GameResult.WIN -> {
                _uiState.update {
                    it.copy(
                        p1Score = it.p1Score + 1,
                        resultText = "Player 1 Wins!",
                        resultColor = Color(0xFF10B981),
                        gameFinished = true
                    )
                }
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
            }
            GameResult.LOSE -> {
                _uiState.update {
                    it.copy(
                        p2Score = it.p2Score + 1,
                        resultText = "Player 2 Wins!",
                        resultColor = Color(0xFF10B981),
                        gameFinished = true
                    )
                }
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
            }
            GameResult.DRAW -> {
                _uiState.update {
                    it.copy(
                        resultText = "Draw!",
                        resultColor = Color(0xFFFFD700),
                        gameFinished = true
                    )
                }
                soundManager.playDraw()
                vibrationManager.vibrate(100)
            }
        }
        startNextRoundCountdown()
    }

    private fun startNextRoundCountdown() {
        nextRoundJob?.cancel()
        nextRoundJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(nextRoundTimerVal = i) }
                delay(1000)
            }
            _uiState.update {
                it.copy(
                    p1Move = null,
                    p2Move = null,
                    gameFinished = false,
                    resultText = "",
                    nextRoundTimerVal = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        choiceTimeoutJob?.cancel()
        nextRoundJob?.cancel()
    }
}

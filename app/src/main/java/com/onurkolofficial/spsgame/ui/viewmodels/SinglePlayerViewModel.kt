package com.onurkolofficial.spsgame.ui.viewmodels

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SinglePlayerUiState(
    val playerScore: Int = 0,
    val computerScore: Int = 0,
    val sessionDraws: Int = 0,
    val roundNum: Int = 1,
    val streak: Int = 0,
    val currentCash: Int = 0,
    val playerMove: Move? = null,
    val computerMove: Move? = null,
    val gameResult: GameResult? = null,
    val isPlayingAnimation: Boolean = false,
    val animationFrame: Int = 0,
    val isButtonsEnabled: Boolean = true,
    val ironCount: Int = 0,
    val iceCount: Int = 0,
    val steelCount: Int = 0,
    val activeSkinId: String = "default"
)

class SinglePlayerViewModel(
    private val prefs: GamePreferences,
    private val soundManager: SoundManager,
    private val vibrationManager: VibrationManager,
    private val playGamesManager: PlayGamesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SinglePlayerUiState(
            currentCash = prefs.statsCash,
            ironCount = prefs.ironCount,
            iceCount = prefs.iceCount,
            steelCount = prefs.steelCount,
            activeSkinId = prefs.activeSkin
        )
    )
    val uiState: StateFlow<SinglePlayerUiState> = _uiState.asStateFlow()

    fun refreshInventory() {
        _uiState.update {
            it.copy(
                currentCash = prefs.statsCash,
                ironCount = prefs.ironCount,
                iceCount = prefs.iceCount,
                steelCount = prefs.steelCount,
                activeSkinId = prefs.activeSkin
            )
        }
    }

    fun onMoveSelected(move: Move) {
        val state = _uiState.value
        if (!state.isButtonsEnabled || state.isPlayingAnimation) return

        vibrationManager.vibrateClick()
        soundManager.playClick()

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

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isButtonsEnabled = false,
                    isPlayingAnimation = true,
                    playerMove = move,
                    computerMove = null,
                    gameResult = null
                )
            }

            for (i in 1..4) {
                _uiState.update { it.copy(animationFrame = i) }
                vibrationManager.vibrate(30)
                delay(250)
            }

            val cpuMove = GameEngine.getRandomMove()
            val res = GameEngine.determineWinner(move, cpuMove)

            var newPlayerScore = _uiState.value.playerScore
            var newCpuScore = _uiState.value.computerScore
            var newDraws = _uiState.value.sessionDraws
            var newStreak = _uiState.value.streak

            when (res) {
                GameResult.WIN -> {
                    newPlayerScore++
                    newStreak++
                    prefs.statsWins++
                    prefs.statsCash += 100
                    soundManager.playWin()
                    vibrationManager.vibrateSuccess()
                    playGamesManager.submitScore(PlayGamesConstants.LEADERBOARD_WINS, newPlayerScore.toLong() * 100)
                }
                GameResult.LOSE -> {
                    newCpuScore++
                    newStreak = 0
                    prefs.statsLosses++
                    soundManager.playLose()
                    vibrationManager.vibrateFailure()
                }
                GameResult.DRAW -> {
                    newDraws++
                    newStreak = 0
                    prefs.statsDraws++
                    soundManager.playDraw()
                    vibrationManager.vibrate(100)
                }
            }

            checkAchievements(newStreak)

            _uiState.update {
                it.copy(
                    computerMove = cpuMove,
                    gameResult = res,
                    isPlayingAnimation = false,
                    playerScore = newPlayerScore,
                    computerScore = newCpuScore,
                    sessionDraws = newDraws,
                    streak = newStreak,
                    currentCash = prefs.statsCash,
                    roundNum = it.roundNum + 1
                )
            }

            delay(500)
            _uiState.update { it.copy(isButtonsEnabled = true) }
        }
    }

    private fun checkAchievements(streak: Int) {
        val totalWins = prefs.statsWins
        val totalDraws = prefs.statsDraws

        if (streak > 5) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T1_WINNER)
        if (streak >= 5) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T2_STREAK_5)
        if (streak >= 3) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T3_STREAK_3)
        if (totalDraws >= 10) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T4_DRAW_MASTER)
        if (totalWins >= 5) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T5_APPRENTICE)
        if (totalWins >= 10) playGamesManager.unlockAchievement(PlayGamesConstants.ACH_T6_RICH)
    }
}

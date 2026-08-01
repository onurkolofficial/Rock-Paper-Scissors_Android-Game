package com.onurkolofficial.spsgame.ui.screens
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.model.GameEngine
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.ui.components.Skin
import com.onurkolofficial.spsgame.ui.components.SKINS_LIST
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import kotlinx.coroutines.delay

@Composable
fun TwoPlayerScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    onNavigateBack: () -> Unit
) {
    var showConfirmExit by remember { mutableStateOf(false) }

    var p1Score by remember { mutableIntStateOf(0) }
    var p2Score by remember { mutableIntStateOf(0) }

    var p1Move by remember { mutableStateOf<Move?>(null) }
    var p2Move by remember { mutableStateOf<Move?>(null) }
    var gameFinished by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var resultColor by remember { mutableStateOf(Color.White) }
    
    var timerVal by remember { mutableStateOf<Int?>(null) }
    var nextRoundTimerVal by remember { mutableStateOf<Int?>(null) }

    val activeSkin = remember { SKINS_LIST.find { it.id == prefs.activeSkin } ?: SKINS_LIST[0] }

    LaunchedEffect(Unit) {
        playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T7_FUN)
    }

    val getOutcomeP1 = {
        val m1 = p1Move
        val m2 = p2Move
        if (m1 != null && m2 != null) {
            GameEngine.determineWinner(m1, m2)
        } else if (m1 != null && m2 == null) {
            GameResult.WIN
        } else if (m2 != null && m1 == null) {
            GameResult.LOSE
        } else {
            GameResult.DRAW
        }
    }

    val getOutcomeP2 = {
        val m1 = p1Move
        val m2 = p2Move
        if (m1 != null && m2 != null) {
            val res = GameEngine.determineWinner(m1, m2)
            when (res) {
                GameResult.WIN -> GameResult.LOSE
                GameResult.LOSE -> GameResult.WIN
                else -> GameResult.DRAW
            }
        } else if (m2 != null && m1 == null) {
            GameResult.WIN
        } else if (m1 != null && m2 == null) {
            GameResult.LOSE
        } else {
            GameResult.DRAW
        }
    }

    // Choice timeout timer (starts when exactly one player has made a choice)
    LaunchedEffect(p1Move, p2Move) {
        if ((p1Move != null && p2Move == null) || (p2Move != null && p1Move == null)) {
            timerVal = 1
            while (timerVal!! > 0) {
                delay(1000)
                timerVal = timerVal!! - 1
            }
            // Timer expired, resolve the game based on who made a choice
            if (p1Move != null && p2Move == null) {
                p1Score++
                resultText = "Player 1 Wins!"
                resultColor = Color.Green
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
            } else if (p2Move != null && p1Move == null) {
                p2Score++
                resultText = "Player 2 Wins!"
                resultColor = Color.Green
                soundManager.playWin()
                vibrationManager.vibrateSuccess()
            }
            gameFinished = true
            timerVal = null
        } else {
            timerVal = null
        }
    }

    // Auto next round transition (3 seconds after game finishes)
    LaunchedEffect(gameFinished) {
        if (gameFinished) {
            nextRoundTimerVal = 3
            while (nextRoundTimerVal!! > 0) {
                delay(1000)
                nextRoundTimerVal = nextRoundTimerVal!! - 1
            }
            // Auto transition
            p1Move = null
            p2Move = null
            gameFinished = false
            resultText = ""
            nextRoundTimerVal = null
        } else {
            nextRoundTimerVal = null
        }
    }

    val evaluateRound = {
        val move1 = p1Move
        val move2 = p2Move
        if (move1 != null && move2 != null) {
            val res = GameEngine.determineWinner(move1, move2)
            when (res) {
                GameResult.WIN -> {
                    p1Score++
                    resultText = "Player 1 Wins!"
                    resultColor = Color.Green
                    soundManager.playWin()
                    vibrationManager.vibrateSuccess()
                }
                GameResult.LOSE -> {
                    p2Score++
                    resultText = "Player 2 Wins!"
                    resultColor = Color.Green
                    soundManager.playWin()
                    vibrationManager.vibrateSuccess()
                }
                GameResult.DRAW -> {
                    resultText = "Draw!"
                    resultColor = Color.White
                    soundManager.playDraw()
                    vibrationManager.vibrate(100)
                }
            }
            gameFinished = true
        }
    }

    if (showConfirmExit) {
        ConfirmModal(
            message = stringResource(id = R.string.game_back_confirm),
            onConfirm = {
                showConfirmExit = false
                onNavigateBack()
            },
            onCancel = {
                showConfirmExit = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1112))
            .statusBarsPadding()
    ) {
        // Player 2 Area (Top, Rotated 180 degrees)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(180f)
                .background(Color.White.copy(alpha = 0.01f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.game_player_2).toAppUppercase(),
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${stringResource(id = R.string.score_label)}: $p2Score",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }



                if (gameFinished) {
                    val p2Outcome = getOutcomeP2()
                    val p2Text = when (p2Outcome) {
                        GameResult.WIN -> stringResource(id = R.string.game_win)
                        GameResult.LOSE -> stringResource(id = R.string.game_lose)
                        else -> stringResource(id = R.string.game_draw)
                    }
                    val p2Color = when (p2Outcome) {
                        GameResult.WIN -> Color.Green
                        GameResult.LOSE -> Color.Red
                        else -> Color.White
                    }
                    Text(
                        text = p2Text.toAppUppercase(),
                        color = p2Color,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameFinished) {
                        if (p2Move != null) {
                            HandImage(move = p2Move!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
                        } else {
                            Text(text = "❌", color = Color.Red, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (p2Move != null) {
                        Text(text = "✓", color = Color.Green, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "❓", color = Color.White.copy(alpha = 0.2f), fontSize = 48.sp)
                    }
                }

                if (!gameFinished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                            TwoPlayerMoveCard(move = move, skin = activeSkin, isSelected = p2Move == move, enabled = p2Move == null) {
                                if (p2Move == null) {
                                    vibrationManager.vibrateClick()
                                    soundManager.playClick()
                                    p2Move = move
                                    evaluateRound()
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }

        // Middle Divider Control Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2124))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    soundManager.playClick()
                    showConfirmExit = true
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            val centerText = if (gameFinished) {
                val sec = nextRoundTimerVal ?: 3
                stringResource(id = R.string.game_next_round_auto, sec)
            } else if (timerVal != null) {
                stringResource(id = R.string.game_timeout, timerVal!!)
            } else {
                stringResource(id = R.string.game_make_choices)
            }

            Text(
                text = centerText.toAppUppercase(),
                color = if (timerVal != null) Color.Red else Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.size(36.dp))
        }

        // Player 1 Area (Bottom, Normal)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.01f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.game_player_1).toAppUppercase(),
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${stringResource(id = R.string.score_label)}: $p1Score",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }



                if (gameFinished) {
                    val p1Outcome = getOutcomeP1()
                    val p1Text = when (p1Outcome) {
                        GameResult.WIN -> stringResource(id = R.string.game_win)
                        GameResult.LOSE -> stringResource(id = R.string.game_lose)
                        else -> stringResource(id = R.string.game_draw)
                    }
                    val p1Color = when (p1Outcome) {
                        GameResult.WIN -> Color.Green
                        GameResult.LOSE -> Color.Red
                        else -> Color.White
                    }
                    Text(
                        text = p1Text.toAppUppercase(),
                        color = p1Color,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameFinished) {
                        if (p1Move != null) {
                            HandImage(move = p1Move!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
                        } else {
                            Text(text = "❌", color = Color.Red, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (p1Move != null) {
                        Text(text = "✓", color = Color.Green, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "❓", color = Color.White.copy(alpha = 0.2f), fontSize = 48.sp)
                    }
                }

                if (!gameFinished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                            TwoPlayerMoveCard(move = move, skin = activeSkin, isSelected = p1Move == move, enabled = p1Move == null) {
                                if (p1Move == null) {
                                    vibrationManager.vibrateClick()
                                    soundManager.playClick()
                                    p1Move = move
                                    evaluateRound()
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
fun TwoPlayerMoveCard(
    move: Move,
    skin: Skin,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(94.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) Color(0xFFFFD700).copy(alpha = 0.15f)
                else Color.White.copy(alpha = if (enabled) 0.05f else 0.02f)
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFFFFD700)
                else Color.White.copy(alpha = if (enabled) 0.1f else 0.03f),
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HandImage(
                move = move,
                skin = skin,
                modifier = Modifier
                    .size(42.dp)
                    .padding(bottom = 6.dp),
                fontSize = 24.sp
            )
            
            Text(
                text = stringResource(id = move.getNameRes()).toAppUppercase(),
                color = Color.White.copy(alpha = if (enabled) 1f else 0.3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun MoveCard(
    move: Move,
    skin: Skin,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) Color(0xFFFFD700).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f)
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        HandImage(move = move, skin = skin, modifier = Modifier.size(32.dp))
    }
}


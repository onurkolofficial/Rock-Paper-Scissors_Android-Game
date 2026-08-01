package com.onurkolofficial.spsgame.ui.screens

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

    val activeSkin = remember { SKINS_LIST.find { it.id == prefs.activeSkin } ?: SKINS_LIST[0] }

    LaunchedEffect(Unit) {
        playGamesManager.unlockAchievement("CgkIua-BqqENEAIQBw")
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
                        text = "PLAYER 2",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "SCORE: $p2Score",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameFinished && p2Move != null) {
                        HandImage(move = p2Move!!, skin = activeSkin, modifier = Modifier.size(60.dp))
                    } else if (p2Move != null) {
                        Text(text = "✓", color = Color.Green, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "?", color = Color.White.copy(alpha = 0.2f), fontSize = 36.sp)
                    }
                }

                if (!gameFinished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                            MoveCard(move = move, skin = activeSkin, isSelected = p2Move == move) {
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
                    Spacer(modifier = Modifier.height(50.dp))
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

            if (gameFinished) {
                Text(
                    text = resultText.uppercase(),
                    color = resultColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = {
                        soundManager.playClick()
                        p1Move = null
                        p2Move = null
                        gameFinished = false
                        resultText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "NEXT",
                        color = Color(0xFF0F1112),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            } else {
                Text(
                    text = "MAKE YOUR CHOICES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.size(36.dp))
            }
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
                if (!gameFinished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                            MoveCard(move = move, skin = activeSkin, isSelected = p1Move == move) {
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
                    Spacer(modifier = Modifier.height(50.dp))
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameFinished && p1Move != null) {
                        HandImage(move = p1Move!!, skin = activeSkin, modifier = Modifier.size(60.dp))
                    } else if (p1Move != null) {
                        Text(text = "✓", color = Color.Green, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "?", color = Color.White.copy(alpha = 0.2f), fontSize = 36.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLAYER 1",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "SCORE: $p1Score",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
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

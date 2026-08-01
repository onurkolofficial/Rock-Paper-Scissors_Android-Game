package com.onurkolofficial.spsgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.model.GameEngine
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.ui.components.Skin
import com.onurkolofficial.spsgame.ui.components.SkinIcon
import com.onurkolofficial.spsgame.ui.components.SKINS_LIST
import com.onurkolofficial.spsgame.utils.AdManager
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SinglePlayerScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    adManager: AdManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showConfirmExit by remember { mutableStateOf(false) }

    var playerScore by remember { mutableIntStateOf(0) }
    var computerScore by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var roundNum by remember { mutableIntStateOf(1) }

    var playerMove by remember { mutableStateOf<Move?>(null) }
    var computerMove by remember { mutableStateOf<Move?>(null) }
    var gameResult by remember { mutableStateOf<GameResult?>(null) }
    var isPlayingAnimation by remember { mutableStateOf(false) }
    var animationFrame by remember { mutableIntStateOf(0) }

    var ironCount by remember { mutableIntStateOf(prefs.ironCount) }
    var iceCount by remember { mutableIntStateOf(prefs.iceCount) }
    var steelCount by remember { mutableIntStateOf(prefs.steelCount) }

    val activeSkin = remember { SKINS_LIST.find { it.id == prefs.activeSkin } ?: SKINS_LIST[0] }

    val handleMoveSelected: (Move) -> Unit = { move ->
        if (!isPlayingAnimation) {
            vibrationManager.vibrateClick()
            soundManager.playClick()
            
            var canPlay = true
        when (move) {
            Move.IRON -> {
                if (ironCount > 0) {
                    ironCount--
                    prefs.ironCount = ironCount
                } else canPlay = false
            }
            Move.ICE -> {
                if (iceCount > 0) {
                    iceCount--
                    prefs.iceCount = iceCount
                } else canPlay = false
            }
            Move.STEEL -> {
                if (steelCount > 0) {
                    steelCount--
                    prefs.steelCount = steelCount
                } else canPlay = false
            }
            else -> {}
        }

        if (canPlay) {
            coroutineScope.launch {
                isPlayingAnimation = true
                playerMove = move
                computerMove = null
                gameResult = null
                
                for (i in 1..4) {
                    animationFrame = i
                    vibrationManager.vibrate(30)
                    delay(250)
                }
                
                val cpuMove = GameEngine.getRandomMove()
                computerMove = cpuMove
                val res = GameEngine.determineWinner(move, cpuMove)
                gameResult = res
                isPlayingAnimation = false
                
                when (res) {
                    GameResult.WIN -> {
                        playerScore++
                        streak++
                        prefs.statsWins++
                        prefs.statsCash += 100
                        soundManager.playWin()
                        vibrationManager.vibrateSuccess()
                        
                        playGamesManager.submitScore("CgkIua-BqqENEAIQCw", playerScore.toLong() * 100)
                    }
                    GameResult.LOSE -> {
                        computerScore++
                        streak = 0
                        prefs.statsLosses++
                        soundManager.playLose()
                        vibrationManager.vibrateFailure()
                    }
                    GameResult.DRAW -> {
                        streak = 0
                        prefs.statsDraws++
                        soundManager.playDraw()
                        vibrationManager.vibrate(100)
                    }
                }

                checkAchievements(prefs, playGamesManager, streak)

                if (roundNum % 4 == 0) {
                    adManager.showInterstitialAd()
                }
                
                roundNum++
            }
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1112))
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playClick()
                        showConfirmExit = true
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "${stringResource(id = R.string.game_score)}: $playerScore - $computerScore",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${stringResource(id = R.string.game_streak)}: $streak",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Computer Hand
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.game_computer).uppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayingAnimation) {
                                Text(text = "✊", fontSize = 60.sp)
                            } else if (computerMove != null) {
                                HandImage(move = computerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp))
                            } else {
                                Text(text = "❓", fontSize = 48.sp, color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                    }

                    // Result Text
                    Box(
                        modifier = Modifier.height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isPlayingAnimation && gameResult != null) {
                            val resColor = when (gameResult) {
                                GameResult.WIN -> Color.Green
                                GameResult.LOSE -> Color.Red
                                else -> Color.White
                            }
                            val resText = when (gameResult) {
                                GameResult.WIN -> stringResource(id = R.string.game_win)
                                GameResult.LOSE -> stringResource(id = R.string.game_lose)
                                else -> stringResource(id = R.string.game_draw)
                            }
                            Text(
                                text = resText.uppercase(),
                                color = resColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // Player Hand
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayingAnimation) {
                                Text(text = "✊", fontSize = 60.sp)
                            } else if (playerMove != null) {
                                HandImage(move = playerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp))
                            } else {
                                Text(text = "❓", fontSize = 48.sp, color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.game_player).uppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions
            Text(
                text = stringResource(id = R.string.tap_to_play).uppercase(),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Selectable moves
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoveSelectionCard(move = Move.ROCK, skin = activeSkin) { handleMoveSelected(Move.ROCK) }
                MoveSelectionCard(move = Move.PAPER, skin = activeSkin) { handleMoveSelected(Move.PAPER) }
                MoveSelectionCard(move = Move.SCISSORS, skin = activeSkin) { handleMoveSelected(Move.SCISSORS) }

                if (ironCount > 0) {
                    MoveSelectionCard(move = Move.IRON, skin = activeSkin, qty = ironCount) { handleMoveSelected(Move.IRON) }
                }
                if (iceCount > 0) {
                    MoveSelectionCard(move = Move.ICE, skin = activeSkin, qty = iceCount) { handleMoveSelected(Move.ICE) }
                }
                if (steelCount > 0) {
                    MoveSelectionCard(move = Move.STEEL, skin = activeSkin, qty = steelCount) { handleMoveSelected(Move.STEEL) }
                }
            }
        }
    }
}

@Composable
fun MoveSelectionCard(
    move: Move,
    skin: Skin,
    qty: Int? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        HandImage(move = move, skin = skin, modifier = Modifier.size(36.dp))
        
        if (qty != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color(0xFFFFD700), CircleShape)
                    .size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = qty.toString(),
                    color = Color(0xFF0F1112),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun HandImage(move: Move, skin: Skin, modifier: Modifier = Modifier) {
    if (move == Move.IRON || move == Move.ICE || move == Move.STEEL) {
        val drawableId = when (move) {
            Move.IRON -> R.drawable.gfx_iron
            Move.STEEL -> R.drawable.gfx_steel
            else -> null
        }
        if (drawableId != null) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = move.name,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        } else {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(text = "🧊", fontSize = 28.sp)
            }
        }
    } else {
        SkinIcon(skin = skin, type = move.name.lowercase(), modifier = modifier)
    }
}

private fun checkAchievements(prefs: GamePreferences, playGamesManager: PlayGamesManager, streak: Int) {
    val totalWins = prefs.statsWins
    val totalDraws = prefs.statsDraws
    
    if (streak > 5) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQBQ") // t1
    if (streak >= 5) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQBA") // t2
    if (streak >= 3) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQAw") // t3
    if (totalDraws >= 10) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQCg") // t4
    if (totalWins >= 5) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQBg") // t5
    if (totalWins >= 10) playGamesManager.unlockAchievement("CgkIua-BqqENEAIQAg") // t6
}

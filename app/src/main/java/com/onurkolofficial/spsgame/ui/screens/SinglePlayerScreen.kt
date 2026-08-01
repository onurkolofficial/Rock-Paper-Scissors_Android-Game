package com.onurkolofficial.spsgame.ui.screens
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
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
import com.onurkolofficial.spsgame.ui.components.StoreModal
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
    var showStore by remember { mutableStateOf(false) }

    var playerScore by remember { mutableIntStateOf(0) }
    var computerScore by remember { mutableIntStateOf(0) }
    var sessionDraws by remember { mutableIntStateOf(0) }
    var currentCash by remember { mutableIntStateOf(prefs.statsCash) }
    var isButtonsEnabled by remember { mutableStateOf(true) }
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
    
    var activeSkinId by remember { mutableStateOf(prefs.activeSkin) }
    val activeSkin = remember(activeSkinId) { SKINS_LIST.find { it.id == activeSkinId } ?: SKINS_LIST[0] }

    val handleMoveSelected: (Move) -> Unit = { move ->
        if (isButtonsEnabled && !isPlayingAnimation) {
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
                    isButtonsEnabled = false
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
                        currentCash = prefs.statsCash
                        soundManager.playWin()
                        vibrationManager.vibrateSuccess()
                        
                        playGamesManager.submitScore(com.onurkolofficial.spsgame.utils.PlayGamesConstants.LEADERBOARD_WINS, playerScore.toLong() * 100)
                    }
                    GameResult.LOSE -> {
                        computerScore++
                        streak = 0
                        prefs.statsLosses++
                        soundManager.playLose()
                        vibrationManager.vibrateFailure()
                    }
                    GameResult.DRAW -> {
                        sessionDraws++
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
                
                delay(500)
                isButtonsEnabled = true
            }
        }
        }
    }

    if (showConfirmExit) {
        ConfirmModal(
            message = stringResource(id = R.string.game_back_confirm),
            onConfirm = {
                showConfirmExit = false
                playGamesManager.saveGame()
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

                // Store Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable {
                            vibrationManager.vibrateClick()
                            soundManager.playClick()
                            showStore = true
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛒",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score and Stats Panels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Cash Panel (Left)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF141517), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("SCORE", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${playerScore * 100}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Text("$$currentCash", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Wins / Draws / Losses Panel (Right)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(Color(0xFF141517), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WINS", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$playerScore", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DRAWS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$sessionDraws", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LOSSES", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$computerScore", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            text = stringResource(id = R.string.game_computer).toAppUppercase(),
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
                                CircularProgressIndicator(
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                            } else if (computerMove != null) {
                                HandImage(move = computerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
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
                                text = resText.toAppUppercase(),
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
                            if (playerMove != null) {
                                HandImage(move = playerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
                            } else {
                                Text(text = "❓", fontSize = 48.sp, color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.game_player).toAppUppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (streak >= 2) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔥", fontSize = 10.sp)
                                    Text(
                                        text = "${stringResource(id = R.string.game_streak).toAppUppercase()} X$streak",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions
            Text(
                text = stringResource(id = R.string.tap_to_play).toAppUppercase(),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Selectable moves (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoveSelectionCard(move = Move.ROCK, skin = activeSkin, enabled = isButtonsEnabled) { handleMoveSelected(Move.ROCK) }
                MoveSelectionCard(move = Move.PAPER, skin = activeSkin, enabled = isButtonsEnabled) { handleMoveSelected(Move.PAPER) }
                MoveSelectionCard(move = Move.SCISSORS, skin = activeSkin, enabled = isButtonsEnabled) { handleMoveSelected(Move.SCISSORS) }

                if (ironCount > 0) {
                    MoveSelectionCard(move = Move.IRON, skin = activeSkin, qty = ironCount, enabled = isButtonsEnabled) { handleMoveSelected(Move.IRON) }
                }
                if (iceCount > 0) {
                    MoveSelectionCard(move = Move.ICE, skin = activeSkin, qty = iceCount, enabled = isButtonsEnabled) { handleMoveSelected(Move.ICE) }
                }
                if (steelCount > 0) {
                    MoveSelectionCard(move = Move.STEEL, skin = activeSkin, qty = steelCount, enabled = isButtonsEnabled) { handleMoveSelected(Move.STEEL) }
                }
            }
        }

        if (showStore) {
            StoreModal(
                prefs = prefs,
                soundManager = soundManager,
                playGamesManager = playGamesManager,
                onClose = { 
                    showStore = false
                    currentCash = prefs.statsCash
                    ironCount = prefs.ironCount
                    iceCount = prefs.iceCount
                    steelCount = prefs.steelCount
                    activeSkinId = prefs.activeSkin
                },
                onRefreshCash = {
                    currentCash = prefs.statsCash
                    ironCount = prefs.ironCount
                    iceCount = prefs.iceCount
                    steelCount = prefs.steelCount
                    activeSkinId = prefs.activeSkin
                }
            )
        }
    }
}

@Composable
fun MoveSelectionCard(
    move: Move,
    skin: Skin,
    qty: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(94.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.05f else 0.02f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.1f else 0.03f), RoundedCornerShape(24.dp))
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
                text = move.name.toAppUppercase(),
                color = Color.White.copy(alpha = if (enabled) 1f else 0.3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        if (qty != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color(0xFFFFD700), CircleShape)
                    .size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = qty.toString(),
                    color = Color(0xFF0F1112),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun HandImage(
    move: Move, 
    skin: Skin, 
    modifier: Modifier = Modifier, 
    fontSize: androidx.compose.ui.unit.TextUnit? = null
) {
    if (move == Move.IRON || move == Move.ICE || move == Move.STEEL) {
        val drawableId = when (move) {
            Move.IRON -> R.drawable.gfx_iron
            Move.STEEL -> R.drawable.gfx_steel
            else -> null
        }
        
        // Consumables are zero-margin drawables, so they bleed to the edge.
        // We apply padding to shrink them visually to match standard moves.
        val drawableModifier = modifier.padding(8.dp)
        
        if (drawableId != null) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = move.name,
                modifier = drawableModifier,
                contentScale = ContentScale.Fit
            )
        } else {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(text = "🧊", fontSize = fontSize ?: 28.sp)
            }
        }
    } else {
        SkinIcon(skin = skin, type = move.name.lowercase(), modifier = modifier, fontSize = fontSize)
    }
}

private fun checkAchievements(prefs: GamePreferences, playGamesManager: PlayGamesManager, streak: Int) {
    val totalWins = prefs.statsWins
    val totalDraws = prefs.statsDraws
    
    if (streak > 5) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T1_WINNER)
    if (streak >= 5) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T2_STREAK_5)
    if (streak >= 3) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T3_STREAK_3)
    if (totalDraws >= 10) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T4_DRAW_MASTER)
    if (totalWins >= 5) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T5_APPRENTICE)
    if (totalWins >= 10) playGamesManager.unlockAchievement(com.onurkolofficial.spsgame.utils.PlayGamesConstants.ACH_T6_RICH)
}


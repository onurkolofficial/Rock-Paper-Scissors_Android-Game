package com.onurkolofficial.spsgame.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.di.LocalAppContainer
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.components.AlertModal
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.ui.components.SKINS_LIST
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase
import com.onurkolofficial.spsgame.ui.viewmodels.OnlineMultiplayerViewModel
import kotlinx.coroutines.delay

enum class MultiplayerMode { SELECTION, MATCHMAKING, CREATE_ROOM, JOIN_ROOM, CONNECTING, IN_GAME }
enum class MatchStatus { CONNECTING, WAITING, STARTING, LOADING, PLAYING, RESULT, GAME_OVER, OPPONENT_DISCONNECTED }

@Composable
fun OnlineMultiplayerScreen(
    viewModel: OnlineMultiplayerViewModel,
    onNavigateBack: () -> Unit
) {
    val appContainer = LocalAppContainer.current
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmExit by remember { mutableStateOf(false) }

    val activeSkin = remember(uiState.activeSkinId) {
        SKINS_LIST.find { it.id == uiState.activeSkinId } ?: SKINS_LIST[0]
    }
    val opponentSkin = remember(uiState.opponentSkinId) {
        SKINS_LIST.find { it.id == uiState.opponentSkinId } ?: SKINS_LIST[0]
    }

    var loadingProgress by remember { mutableStateOf(0f) }
    val animatedLoadingProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 2000),
        label = "LoadingProgress"
    )

    val msgVisuals = stringResource(id = R.string.loading_visuals)
    val msgSounds = stringResource(id = R.string.loading_sounds)
    val msgEffects = stringResource(id = R.string.loading_effects)
    val msgResources = stringResource(id = R.string.loading_resources)
    val msgServer = stringResource(id = R.string.loading_server)
    val msgRoom = stringResource(id = R.string.loading_rooms)
    val msgGameLoading = stringResource(id = R.string.loading_game)
    val msgGameStarting = stringResource(id = R.string.loading_start)

    val loadingMessages = remember(msgVisuals, msgSounds, msgEffects, msgResources, msgServer, msgRoom, msgGameStarting) {
        listOf(msgVisuals, msgSounds, msgEffects, msgResources, msgServer, msgRoom, msgGameStarting)
    }

    LaunchedEffect(uiState.matchStatus) {
        if (uiState.matchStatus == MatchStatus.LOADING) {
            loadingProgress = 1f
            delay(2000)
            viewModel.onLoadingAnimationFinished()
        } else {
            loadingProgress = 0f
        }
    }

    val handleBackPress = {
        if (uiState.matchStatus == MatchStatus.STARTING || uiState.matchStatus == MatchStatus.LOADING) {
            // Do nothing during GameLoadingScreen
        } else if (uiState.gameMode == MultiplayerMode.IN_GAME && uiState.matchStatus != MatchStatus.GAME_OVER) {
            showConfirmExit = true
        } else if (uiState.gameMode != MultiplayerMode.SELECTION && uiState.gameMode != MultiplayerMode.IN_GAME) {
            appContainer.soundManager.playClick()
            viewModel.leaveMatchmakingOrGame()
            viewModel.setGameMode(MultiplayerMode.SELECTION)
        } else {
            appContainer.soundManager.playClick()
            viewModel.leaveMatchmakingOrGame()
            appContainer.playGamesManager.saveGame()
            onNavigateBack()
        }
    }

    BackHandler {
        handleBackPress()
    }

    if (showConfirmExit) {
        ConfirmModal(
            message = stringResource(id = R.string.online_exit_confirm),
            onConfirm = {
                showConfirmExit = false
                viewModel.forfeitGame()
                onNavigateBack()
            },
            onCancel = { showConfirmExit = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1112))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Central Control Panel (divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        appContainer.soundManager.playClick()
                        if (uiState.gameMode == MultiplayerMode.IN_GAME) {
                            showConfirmExit = true
                        } else {
                            viewModel.leaveMatchmakingOrGame()
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit",
                        tint = Color.Red
                    )
                }

                if (uiState.matchStatus == MatchStatus.PLAYING || uiState.matchStatus == MatchStatus.RESULT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.opponentScore.toString(),
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            modifier = Modifier.rotate(180f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.game_round, uiState.currentRound),
                                        color = Color(0xFF3B82F6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (uiState.allowSpecialItems) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (uiState.allowSpecialItems) Color(0xFF8B5CF6).copy(alpha = 0.4f) else Color(0xFF3B82F6).copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.allowSpecialItems) stringResource(id = R.string.online_badge_hard) else stringResource(id = R.string.online_badge_classic),
                                        color = if (uiState.allowSpecialItems) Color(0xFFC084FC) else Color(0xFF60A5FA),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.game_draws_label, uiState.totalDraws),
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 11.sp
                                )
                                uiState.timerVal?.let { t ->
                                    Text(
                                        text = "⏳ ${t}s",
                                        color = Color.Red.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = uiState.myScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }
                } else {
                    Text(
                        text = "WIFI CONNECTED",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            // Top Half - Opponent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hand Card
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.matchStatus == MatchStatus.RESULT && uiState.opponentMove != null) {
                            HandImage(move = uiState.opponentMove!!, skin = opponentSkin, modifier = Modifier.size(68.dp), fontSize = 40.sp)
                        } else if (uiState.matchStatus == MatchStatus.PLAYING && uiState.opponentHasMoved) {
                            Text(text = "✓", color = Color.Green, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "?", color = Color.White.copy(alpha = 0.1f), fontSize = 40.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.opponentName.toAppUppercase(),
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.rotate(180f)
                    )
                }
            }

            // Bottom Half - Player
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hand Card
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.myMove != null) {
                            HandImage(move = uiState.myMove!!, skin = activeSkin, modifier = Modifier.size(68.dp), fontSize = 40.sp)
                        } else {
                            Text(text = "?", color = Color.White.copy(alpha = 0.1f), fontSize = 40.sp)
                        }
                    }

                    // Move Select buttons
                    if (uiState.matchStatus == MatchStatus.PLAYING && uiState.myMove == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                                MoveSelectionCard(move = move, skin = activeSkin, enabled = true) {
                                    viewModel.sendMove(move)
                                }
                            }
                            if (uiState.allowSpecialItems) {
                                if (uiState.ironCount > 0) {
                                    MoveSelectionCard(move = Move.IRON, skin = activeSkin, qty = uiState.ironCount, enabled = true) {
                                        viewModel.sendMove(Move.IRON)
                                    }
                                }
                                if (uiState.iceCount > 0) {
                                    MoveSelectionCard(move = Move.ICE, skin = activeSkin, qty = uiState.iceCount, enabled = true) {
                                        viewModel.sendMove(Move.ICE)
                                    }
                                }
                                if (uiState.steelCount > 0) {
                                    MoveSelectionCard(move = Move.STEEL, skin = activeSkin, qty = uiState.steelCount, enabled = true) {
                                        viewModel.sendMove(Move.STEEL)
                                    }
                                }
                                if (uiState.fireCount > 0) {
                                    MoveSelectionCard(move = Move.FIRE, skin = activeSkin, qty = uiState.fireCount, enabled = true) {
                                        viewModel.sendMove(Move.FIRE)
                                    }
                                }
                                if (uiState.lightningCount > 0) {
                                    MoveSelectionCard(move = Move.LIGHTNING, skin = activeSkin, qty = uiState.lightningCount, enabled = true) {
                                        viewModel.sendMove(Move.LIGHTNING)
                                    }
                                }
                                if (uiState.bombCount > 0) {
                                    MoveSelectionCard(move = Move.BOMB, skin = activeSkin, qty = uiState.bombCount, enabled = true) {
                                        viewModel.sendMove(Move.BOMB)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = appContainer.prefs.userName.toAppUppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Overlay 1: Matchmaking & Room Selection Menu
        AnimatedVisibility(
            visible = uiState.gameMode != MultiplayerMode.IN_GAME,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { handleBackPress() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                when (uiState.gameMode) {
                    MultiplayerMode.SELECTION -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.online_title),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Classic Mode Matchmaking
                            Button(
                                onClick = {
                                    appContainer.soundManager.playClick()
                                    viewModel.joinMatchmaking(appContainer.prefs.userName, appContainer.prefs.activeSkin, "classic")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(68.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "⚔️", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = stringResource(id = R.string.online_mode_classic).toAppUppercase(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = stringResource(id = R.string.online_mode_classic_desc),
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Hard Mode Matchmaking
                            Button(
                                onClick = {
                                    appContainer.soundManager.playClick()
                                    viewModel.joinMatchmaking(appContainer.prefs.userName, appContainer.prefs.activeSkin, "hard")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(68.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "⚡", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = stringResource(id = R.string.online_mode_hard).toAppUppercase(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = stringResource(id = R.string.online_mode_hard_desc),
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Private Room: Create Room
                            Button(
                                onClick = {
                                    appContainer.soundManager.playClick()
                                    viewModel.setGameMode(MultiplayerMode.CREATE_ROOM)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🔒", fontSize = 16.sp)
                                    Text(
                                        text = stringResource(id = R.string.online_create_room),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Private Room: Join Room
                            Button(
                                onClick = {
                                    appContainer.soundManager.playClick()
                                    viewModel.setGameMode(MultiplayerMode.JOIN_ROOM)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🔑", fontSize = 16.sp)
                                    Text(
                                        text = stringResource(id = R.string.online_join_room),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    MultiplayerMode.CREATE_ROOM -> {
                        if (uiState.roomId == null) {
                            // Room Configuration Screen before creating
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_create_room_config),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )

                                // Item toggle card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(id = R.string.online_allow_items_toggle),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.online_allow_items_desc),
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    Switch(
                                        checked = uiState.createRoomAllowItems,
                                        onCheckedChange = {
                                            appContainer.soundManager.playClick()
                                            viewModel.setCreateRoomAllowItems(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981),
                                            uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        appContainer.soundManager.playClick()
                                        viewModel.createPrivateRoom(
                                            appContainer.prefs.userName,
                                            appContainer.prefs.activeSkin,
                                            uiState.createRoomAllowItems
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.online_create_room_btn).toAppUppercase(),
                                        color = Color(0xFF0F1112),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Text(
                                    text = stringResource(id = R.string.online_cancel),
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            appContainer.soundManager.playClick()
                                            viewModel.setGameMode(MultiplayerMode.SELECTION)
                                        }
                                        .padding(8.dp)
                                )
                            }
                        } else {
                            // Waiting for opponent in created room
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF3B82F6))

                                Text(
                                    text = stringResource(id = R.string.online_waiting),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.online_room_code_label).toAppUppercase(),
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = uiState.roomId!!,
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (uiState.allowSpecialItems) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (uiState.allowSpecialItems) Color(0xFF8B5CF6).copy(alpha = 0.5f) else Color(0xFF3B82F6).copy(alpha = 0.5f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (uiState.allowSpecialItems) stringResource(id = R.string.online_mode_hard).toAppUppercase() else stringResource(id = R.string.online_mode_classic).toAppUppercase(),
                                            color = if (uiState.allowSpecialItems) Color(0xFFC084FC) else Color(0xFF60A5FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(id = R.string.online_room_share_msg),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    MultiplayerMode.JOIN_ROOM -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.online_join_room_title),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = uiState.roomCodeInput,
                                onValueChange = { viewModel.setRoomCodeInput(it.take(6).toAppUppercase()) },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.online_room_code_placeholder),
                                        color = Color.White.copy(alpha = 0.3f)
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    viewModel.joinPrivateRoom(
                                        uiState.roomCodeInput,
                                        appContainer.prefs.userName,
                                        appContainer.prefs.activeSkin
                                    )
                                },
                                enabled = uiState.roomCodeInput.length >= 3,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_join_button).toAppUppercase(),
                                    color = Color(0xFF0F1112),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Text(
                                text = stringResource(id = R.string.online_cancel),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        appContainer.soundManager.playClick()
                                        viewModel.setGameMode(MultiplayerMode.SELECTION)
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                    MultiplayerMode.MATCHMAKING, MultiplayerMode.CONNECTING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6))

                            Text(
                                text = when {
                                    uiState.matchStatus == MatchStatus.WAITING -> stringResource(id = R.string.online_waiting)
                                    else -> stringResource(id = R.string.online_connecting)
                                },
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        // Overlay 2: Player Joined Countdown
        if (uiState.matchStatus == MatchStatus.STARTING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.online_player_joined).toAppUppercase(),
                        color = Color.Green,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = uiState.opponentName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.startingTimerVal?.toString() ?: "",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Overlay 2.5: Game Loading Screen
        if (uiState.matchStatus == MatchStatus.LOADING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1112)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    val currentMessageIndex = (animatedLoadingProgress * loadingMessages.size).toInt().coerceAtMost(loadingMessages.size - 1)
                    val currentMessage = loadingMessages[currentMessageIndex]

                    Text(
                        text = msgGameLoading.toAppUppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentMessage,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedLoadingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF3B82F6),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // Overlay 3: Round Results
        if (uiState.matchStatus == MatchStatus.RESULT && uiState.roundResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val outcomeColor = when (uiState.roundResult) {
                        GameResult.WIN -> Color.Green
                        GameResult.LOSE -> Color.Red
                        else -> Color.White
                    }
                    val outcomeText = when (uiState.roundResult) {
                        GameResult.WIN -> stringResource(id = R.string.game_win)
                        GameResult.LOSE -> stringResource(id = R.string.game_lose)
                        else -> stringResource(id = R.string.game_draw)
                    }

                    Text(
                        text = outcomeText.toAppUppercase(),
                        color = outcomeColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    uiState.nextRoundTimerVal?.let { t ->
                        Text(
                            text = stringResource(id = R.string.online_next_round, t),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Overlay 4: Match Finished or Opponent Disconnected
        if (uiState.matchStatus == MatchStatus.GAME_OVER || uiState.matchStatus == MatchStatus.OPPONENT_DISCONNECTED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    if (uiState.matchStatus == MatchStatus.OPPONENT_DISCONNECTED) {
                        Text(
                            text = stringResource(id = R.string.online_opponent_disconnected).toAppUppercase(),
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        if (uiState.isAbandonedWon || uiState.finalResult == GameResult.WIN) {
                            Text(
                                text = stringResource(id = R.string.online_you_won).toAppUppercase(),
                                color = Color.Green,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = stringResource(id = R.string.online_opponent_abandoned_won),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.online_match_void).toAppUppercase(),
                                color = Color(0xFFFFD700),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = stringResource(id = R.string.online_opponent_abandoned_void),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        val overText = when (uiState.finalResult) {
                            GameResult.WIN -> stringResource(id = R.string.game_win)
                            GameResult.LOSE -> stringResource(id = R.string.game_lose)
                            else -> stringResource(id = R.string.game_draw)
                        }
                        val overColor = when (uiState.finalResult) {
                            GameResult.WIN -> Color.Green
                            GameResult.LOSE -> Color.Red
                            else -> Color.White
                        }

                        Text(
                            text = stringResource(id = R.string.online_match_completed).toAppUppercase(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = overText.toAppUppercase(),
                            color = overColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            appContainer.soundManager.playClick()
                            viewModel.leaveMatchmakingOrGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rematch", tint = Color.White)
                            Text(
                                text = stringResource(id = R.string.online_rematch).toAppUppercase(),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Button(
                        onClick = {
                            appContainer.soundManager.playClick()
                            viewModel.leaveMatchmakingOrGame()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.online_back_to_menu).toAppUppercase(),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Alert message Dialog
        uiState.errorMsg?.let { msg ->
            AlertModal(
                title = stringResource(id = R.string.online_error_title).toAppUppercase(),
                message = msg,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

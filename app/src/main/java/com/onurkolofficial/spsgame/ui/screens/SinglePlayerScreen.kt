package com.onurkolofficial.spsgame.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.di.LocalAppContainer
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.ui.components.SKINS_LIST
import com.onurkolofficial.spsgame.ui.components.Skin
import com.onurkolofficial.spsgame.ui.components.SkinIcon
import com.onurkolofficial.spsgame.ui.components.StoreModal
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase
import com.onurkolofficial.spsgame.ui.viewmodels.SinglePlayerViewModel

@Composable
fun SinglePlayerScreen(
    viewModel: SinglePlayerViewModel,
    onNavigateBack: () -> Unit
) {
    val appContainer = LocalAppContainer.current
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmExit by remember { mutableStateOf(false) }
    var showStore by remember { mutableStateOf(false) }

    val activeSkin = remember(uiState.activeSkinId) {
        SKINS_LIST.find { it.id == uiState.activeSkinId } ?: SKINS_LIST[0]
    }

    BackHandler {
        appContainer.soundManager.playClick()
        showConfirmExit = true
    }

    if (showConfirmExit) {
        ConfirmModal(
            message = stringResource(id = R.string.game_back_confirm),
            onConfirm = {
                showConfirmExit = false
                appContainer.playGamesManager.saveGame()
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
                        appContainer.soundManager.playClick()
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
                            appContainer.vibrationManager.vibrateClick()
                            appContainer.soundManager.playClick()
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
                            Text(stringResource(id = R.string.score_label), color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${uiState.playerScore * 100}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Text("$${uiState.currentCash}", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Wins / Draws / Losses Panel (Right)
                Box(
                    modifier = Modifier
                        .weight(1.4f)
                        .background(Color(0xFF141517), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.game_wins).toAppUppercase(),
                                color = Color(0xFF10B981),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text("${uiState.playerScore}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.game_draws).toAppUppercase(),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text("${uiState.sessionDraws}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.game_losses).toAppUppercase(),
                                color = Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text("${uiState.computerScore}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
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
                            if (uiState.isPlayingAnimation) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                            } else if (uiState.computerMove != null) {
                                HandImage(move = uiState.computerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
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
                        if (!uiState.isPlayingAnimation && uiState.gameResult != null) {
                            val resColor = when (uiState.gameResult) {
                                GameResult.WIN -> Color.Green
                                GameResult.LOSE -> Color.Red
                                else -> Color.White
                            }
                            val resText = when (uiState.gameResult) {
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
                            if (uiState.playerMove != null) {
                                HandImage(move = uiState.playerMove!!, skin = activeSkin, modifier = Modifier.size(80.dp), fontSize = 48.sp)
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

                        if (uiState.streak >= 2) {
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
                                        text = "${stringResource(id = R.string.game_streak).toAppUppercase()} X${uiState.streak}",
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
                MoveSelectionCard(move = Move.ROCK, skin = activeSkin, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.ROCK) }
                MoveSelectionCard(move = Move.PAPER, skin = activeSkin, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.PAPER) }
                MoveSelectionCard(move = Move.SCISSORS, skin = activeSkin, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.SCISSORS) }

                if (uiState.ironCount > 0) {
                    MoveSelectionCard(move = Move.IRON, skin = activeSkin, qty = uiState.ironCount, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.IRON) }
                }
                if (uiState.iceCount > 0) {
                    MoveSelectionCard(move = Move.ICE, skin = activeSkin, qty = uiState.iceCount, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.ICE) }
                }
                if (uiState.steelCount > 0) {
                    MoveSelectionCard(move = Move.STEEL, skin = activeSkin, qty = uiState.steelCount, enabled = uiState.isButtonsEnabled) { viewModel.onMoveSelected(Move.STEEL) }
                }
            }
        }

        if (showStore) {
            StoreModal(
                prefs = appContainer.prefs,
                soundManager = appContainer.soundManager,
                playGamesManager = appContainer.playGamesManager,
                onClose = {
                    showStore = false
                    viewModel.refreshInventory()
                },
                onRefreshCash = {
                    viewModel.refreshInventory()
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
                modifier = Modifier.size(44.dp),
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = move.getNameRes()).toAppUppercase(),
                color = if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (qty != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFFEF4444), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$qty",
                    color = Color.White,
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
    fontSize: androidx.compose.ui.unit.TextUnit = 32.sp
) {
    if (move == Move.IRON) {
        Image(
            painter = painterResource(id = R.drawable.gfx_iron),
            contentDescription = "Iron",
            modifier = modifier
        )
    } else if (move == Move.ICE) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "🧊", fontSize = fontSize)
        }
    } else if (move == Move.STEEL) {
        Image(
            painter = painterResource(id = R.drawable.gfx_steel),
            contentDescription = "Steel",
            modifier = modifier
        )
    } else if (skin.id == "default" || skin.id == "biker") {
        val res = when (move) {
            Move.ROCK -> skin.rockResId
            Move.PAPER -> skin.paperResId
            Move.SCISSORS -> skin.scissorsResId
            else -> null
        }
        if (res != null) {
            Image(
                painter = painterResource(id = res),
                contentDescription = move.name,
                modifier = modifier
            )
        } else {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(text = "❓", fontSize = fontSize)
            }
        }
    } else {
        SkinIcon(skin = skin, type = move.name.lowercase(), modifier = modifier, fontSize = fontSize)
    }
}

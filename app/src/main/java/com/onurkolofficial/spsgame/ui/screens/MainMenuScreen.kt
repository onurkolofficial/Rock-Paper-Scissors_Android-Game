package com.onurkolofficial.spsgame.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.di.LocalAppContainer
import com.onurkolofficial.spsgame.ui.components.StoreModal
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase
import com.onurkolofficial.spsgame.ui.viewmodels.MainMenuViewModel
import com.startapp.sdk.adsbase.model.AdPreferences

@Composable
fun MainMenuScreen(
    viewModel: MainMenuViewModel,
    onNavigateToSinglePlayer: () -> Unit,
    onNavigateToTwoPlayer: () -> Unit,
    onNavigateToOnlineMultiplayer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val appContainer = LocalAppContainer.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        appContainer.soundManager.startBgm()
        viewModel.ensureOnlineConnected()
        viewModel.refreshProfile()
    }

    if (uiState.showStore) {
        StoreModal(
            prefs = appContainer.prefs,
            soundManager = appContainer.soundManager,
            playGamesManager = appContainer.playGamesManager,
            onClose = { viewModel.setShowStore(false) },
            onRefreshCash = { viewModel.refreshProfile() }
        )
        return
    }

    if (uiState.showUpdateDialog) {
        Dialog(onDismissRequest = {
            viewModel.dismissUpdateDialog()
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E2124),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.update_dialog_title, com.onurkolofficial.spsgame.BuildConfig.VERSION_NAME),
                        color = Color(0xFF10B981),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.update_dialog_features),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            appContainer.soundManager.playClick()
                            viewModel.dismissUpdateDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.update_dialog_close).toAppUppercase(),
                            color = Color(0xFF0F1112),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
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
            // Top Start.io Banner Ad placement
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        val adPrefs = AdPreferences().apply {
                            adTag = "RPSGame_MainBanner"
                            minCpm = 0.05
                        }
                        com.startapp.sdk.ads.banner.Banner(context, adPrefs)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info Header
            val isGuest = uiState.userName == "Guest"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .clickable {
                        appContainer.vibrationManager.vibrateClick()
                        appContainer.soundManager.playClick()
                        if (isGuest) {
                            appContainer.playGamesManager.signIn { success, name, _ ->
                                if (success && name != null) {
                                    appContainer.prefs.userName = name
                                    viewModel.refreshProfile()
                                }
                            }
                        } else {
                            onNavigateToProfile()
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👤", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = uiState.userName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isGuest) stringResource(id = R.string.tap_to_sign_in) else "Connected",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Game Logo / Title
            Text(
                text = stringResource(id = R.string.app_name).toAppUppercase(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "NEW NATIVE EDITION",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Store / Shop button above Single Player button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable {
                            appContainer.vibrationManager.vibrateClick()
                            appContainer.soundManager.playClick()
                            viewModel.setShowStore(true)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🛒", fontSize = 15.sp)
                    Text(
                        text = stringResource(id = R.string.game_shop).toAppUppercase(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Action Play Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuButton(
                    text = stringResource(id = R.string.menu_single_player),
                    icon = "▷",
                    containerColor = Color(0xFF2C2E30),
                    iconContainerColor = Color(0xFF434548)
                ) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToSinglePlayer()
                }

                MenuButton(
                    text = stringResource(id = R.string.menu_two_player),
                    icon = "👥",
                    containerColor = Color(0xFF141416),
                    iconContainerColor = Color(0xFF222325)
                ) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToTwoPlayer()
                }

                MenuButton(
                    text = stringResource(id = R.string.menu_online_multiplayer),
                    icon = "🌐",
                    containerColor = Color(0xFF1B2F52),
                    iconContainerColor = Color(0xFF2D4B7C),
                    subTextContent = {
                        val onlineCount = uiState.onlinePlayersCount
                        if (onlineCount != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$onlineCount ${stringResource(id = R.string.online_players_count).toAppUppercase()}",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = stringResource(id = R.string.online_connecting_short),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToOnlineMultiplayer()
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Utility buttons Row at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomIconButton(iconText = "🥇", label = stringResource(id = R.string.leaderboard)) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToLeaderboard()
                }
                BottomIconButton(iconText = "🏆", label = stringResource(id = R.string.achievements)) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToAchievements()
                }
                BottomIconButton(iconText = "📊", label = stringResource(id = R.string.menu_stats)) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToStats()
                }
                BottomIconButton(iconText = "⚙️", label = stringResource(id = R.string.menu_settings)) {
                    appContainer.vibrationManager.vibrateClick()
                    appContainer.soundManager.playClick()
                    onNavigateToSettings()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    subText: String? = null,
    subTextContent: (@Composable () -> Unit)? = null,
    icon: String,
    containerColor: Color,
    iconContainerColor: Color,
    textColor: Color = Color.White,
    iconColor: Color = Color.White,
    border: BorderStroke? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        border = border,
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = text.toAppUppercase(),
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                if (subTextContent != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    subTextContent()
                } else if (subText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subText,
                        color = if (iconColor != Color.White) iconColor else Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BottomIconButton(
    iconText: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White.copy(alpha = 0.03f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconText, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

package com.onurkolofficial.spsgame.ui.screens
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import android.util.Log
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
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.startapp.sdk.adsbase.model.AdPreferences
import com.onurkolofficial.spsgame.ui.components.StoreModal
import com.onurkolofficial.spsgame.utils.GameAppConfig
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

@Composable
fun MainMenuScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    onNavigateToSinglePlayer: () -> Unit,
    onNavigateToTwoPlayer: () -> Unit,
    onNavigateToOnlineMultiplayer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var showStore by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(prefs.userName) }
    var statsCash by remember { mutableIntStateOf(prefs.statsCash) }
    var onlinePlayers by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        soundManager.startBgm()
        playGamesManager.checkSilentSignIn { success, name, _ ->
            if (success && name != null) {
                prefs.userName = name
                userName = name
            }
        }
    }

    DisposableEffect(Unit) {
        val socketUrl = GameAppConfig.SOCKET_URL
        var socket: Socket? = null
        try {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            socket = IO.socket(socketUrl, opts)
            socket.on(Socket.EVENT_CONNECT) {
                socket?.emit("request_player_count")
            }
            socket.on("player_count") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject
                    val count = data?.optInt("count", 2) ?: 2
                    onlinePlayers = count
                }
            }
            socket.connect()
        } catch (e: Exception) {
            Log.e("MainMenuScreen", "Error connecting socket for player count", e)
        }

        onDispose {
            try {
                socket?.disconnect()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    if (showStore) {
        StoreModal(
            prefs = prefs,
            soundManager = soundManager,
            playGamesManager = playGamesManager,
            onClose = { showStore = false },
            onRefreshCash = { statsCash = prefs.statsCash }
        )
        return
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
            // Top Start.io Banner Ad placement using AndroidView
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
            val isGuest = userName == "Guest"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .clickable {
                        vibrationManager.vibrateClick()
                        soundManager.playClick()
                        if (isGuest) {
                            playGamesManager.signIn { success, name, _ ->
                                if (success && name != null) {
                                    prefs.userName = name
                                    userName = name
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
                            text = userName,
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

                // Removed Balance/Logout Display
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

            // Store / Shop button above Single Player button (aligned to the right)
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
                            vibrationManager.vibrateClick()
                            soundManager.playClick()
                            showStore = true
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
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
                    onNavigateToSinglePlayer()
                }

                MenuButton(
                    text = stringResource(id = R.string.menu_two_player),
                    icon = "👥",
                    containerColor = Color(0xFF141416),
                    iconContainerColor = Color(0xFF222325)
                ) {
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
                    onNavigateToTwoPlayer()
                }

                val onlinePlayersText = if (onlinePlayers != null) {
                    "● $onlinePlayers ${stringResource(id = R.string.online_players_count).toAppUppercase()}"
                } else {
                    "● ... ${stringResource(id = R.string.online_players_count).toAppUppercase()}"
                }

                MenuButton(
                    text = stringResource(id = R.string.menu_online_multiplayer),
                    subText = onlinePlayersText,
                    icon = "🌐",
                    containerColor = Color(0xFF1B2F52),
                    iconContainerColor = Color(0xFF2D4B7C)
                ) {
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
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
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
                    onNavigateToLeaderboard()
                }
                BottomIconButton(iconText = "🏆", label = stringResource(id = R.string.achievements)) {
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
                    onNavigateToAchievements()
                }
                BottomIconButton(iconText = "📊", label = stringResource(id = R.string.menu_stats)) {
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
                    onNavigateToStats()
                }
                BottomIconButton(iconText = "⚙️", label = stringResource(id = R.string.menu_settings)) {
                    vibrationManager.vibrateClick()
                    soundManager.playClick()
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
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
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
            // Left Icon Box
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
            
            // Texts (Title & Subtitle)
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text.toAppUppercase(),
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                if (subText != null) {
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


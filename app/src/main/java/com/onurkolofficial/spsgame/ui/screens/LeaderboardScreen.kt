package com.onurkolofficial.spsgame.ui.screens
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.LeaderboardEntry
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

@Composable
fun LeaderboardScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    onNavigateBack: () -> Unit
) {
    val leaderboardId = com.onurkolofficial.spsgame.utils.PlayGamesConstants.LEADERBOARD_WINS
    
    var scores by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        playGamesManager.loadLeaderboardScores(leaderboardId) { success, result ->
            scores = result
            isLoading = false
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playClick()
                        vibrationManager.vibrateClick()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.leaderboard).toAppUppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = {
                        vibrationManager.vibrateClick()
                        soundManager.playClick()
                        playGamesManager.showLeaderboard(leaderboardId)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Play Games",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                if (scores.isNullOrEmpty()) {
                    // Empty or Error Fallback
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LİDERLİK TABLOSU YÜKLENEMEDİ",
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    vibrationManager.vibrateClick()
                                    soundManager.playClick()
                                    isLoading = true
                                    playGamesManager.loadLeaderboardScores(leaderboardId) { success, result ->
                                        scores = result
                                        isLoading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("TEKRAR DENE", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Ranking List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(scores!!) { entry ->
                            val isMe = entry.name.equals(prefs.userName, ignoreCase = true)
                            LeaderboardRow(entry = entry, isMe = isMe) {
                                if (entry.playerId != null) {
                                    soundManager.playClick()
                                    playGamesManager.showCompareProfile(entry.playerId)
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry, isMe: Boolean = false, onClick: () -> Unit = {}) {
    val rankInt = entry.rank.replace(".", "").toIntOrNull() ?: 99
    val badgeColor = when (rankInt) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White.copy(alpha = 0.05f)
    }
    val badgeTextColor = when (rankInt) {
        1, 2, 3 -> Color(0xFF0F1112)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isMe) Color(0xFFFFD700).copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.02f)
            )
            .border(
                1.dp,
                if (isMe) Color(0xFFFFD700).copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.rank.replace(".", ""),
                color = badgeTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = entry.name,
            color = if (isMe) Color(0xFFFFD700) else Color.White,
            fontWeight = if (isMe) FontWeight.Black else FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = entry.score,
            color = Color(0xFFFFD700),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}


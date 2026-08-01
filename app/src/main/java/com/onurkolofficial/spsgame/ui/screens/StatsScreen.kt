package com.onurkolofficial.spsgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

@Composable
fun StatsScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    onNavigateBack: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    var wins by remember { mutableStateOf(prefs.statsWins) }
    var losses by remember { mutableStateOf(prefs.statsLosses) }
    var draws by remember { mutableStateOf(prefs.statsDraws) }

    var oWins by remember { mutableStateOf(prefs.statsOnlineWins) }
    var oLosses by remember { mutableStateOf(prefs.statsOnlineLosses) }
    var oDraws by remember { mutableStateOf(prefs.statsOnlineDraws) }
    var oHistory by remember { mutableStateOf(prefs.onlineHistory) }

    val calculateWinRate = { w: Int, l: Int, d: Int ->
        val total = w + l + d
        if (total > 0) {
            val rate = (w.toFloat() / total.toFloat()) * 100f
            String.format(java.util.Locale.US, "%.1f%%", rate)
        } else {
            "0.0%"
        }
    }

    if (showConfirmReset) {
        ConfirmModal(
            message = "Tüm istatistiklerinizi ve ilerlemenizi sıfırlamak istediğinize emin misiniz?",
            onConfirm = {
                vibrationManager.vibrateClick()
                soundManager.playClick()
                prefs.clearStats()
                
                wins = prefs.statsWins
                losses = prefs.statsLosses
                draws = prefs.statsDraws
                oWins = prefs.statsOnlineWins
                oLosses = prefs.statsOnlineLosses
                oDraws = prefs.statsOnlineDraws
                oHistory = prefs.onlineHistory
                
                showConfirmReset = false
            },
            onCancel = { showConfirmReset = false }
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
                        onNavigateBack()
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

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.stats_title).uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Single Player Stats Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.stats_tab_single).uppercase(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = stringResource(id = R.string.stats_wins), value = wins.toString(), Color.Green)
                        StatItem(label = stringResource(id = R.string.stats_draws), value = draws.toString(), Color.White)
                        StatItem(label = stringResource(id = R.string.stats_losses), value = losses.toString(), Color.Red)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(id = R.string.stats_win_rate), color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = calculateWinRate(wins, losses, draws), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Online Multiplayer Stats Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.stats_tab_online).uppercase(),
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = stringResource(id = R.string.stats_wins), value = oWins.toString(), Color.Green)
                        StatItem(label = stringResource(id = R.string.stats_draws), value = oDraws.toString(), Color.White)
                        StatItem(label = stringResource(id = R.string.stats_losses), value = oLosses.toString(), Color.Red)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(id = R.string.stats_win_rate), color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = calculateWinRate(oWins, oLosses, oDraws), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Recent Matches Card
                if (oHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.stats_recent_matches).uppercase(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            oHistory.forEach { outcome ->
                                val badgeColor = when (outcome) {
                                    "win" -> Color.Green
                                    "lose" -> Color.Red
                                    else -> Color.White.copy(alpha = 0.6f)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = outcome.uppercase(),
                                        color = badgeColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Reset Stats Button
                Button(
                    onClick = { showConfirmReset = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "İSTATİSTİKLERİ SIFIRLA",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label.uppercase(), color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

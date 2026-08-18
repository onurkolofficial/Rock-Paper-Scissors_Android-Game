package com.onurkolofficial.spsgame.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
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
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase
import com.onurkolofficial.spsgame.ui.navigation.ScreenRoute
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

@Composable
fun ProfileScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    profileArgs: ScreenRoute.Profile? = null,
    onNavigateBack: () -> Unit
) {
    val isOtherUser = profileArgs?.isOtherUser == true &&
            !profileArgs.userName.isNullOrBlank() &&
            !profileArgs.userName.equals(prefs.userName, ignoreCase = true)

    val displayName = if (isOtherUser) (profileArgs?.userName ?: "Player") else prefs.userName
    val otherWins = profileArgs?.wins ?: 0
    val otherRank = profileArgs?.rank ?: "-"

    val calculateWinRate = { w: Int, l: Int, d: Int ->
        val total = w + l + d
        if (total > 0) {
            val rate = (w.toFloat() / total.toFloat()) * 100f
            String.format(java.util.Locale.US, "%.1f%%", rate)
        } else {
            "0.0%"
        }
    }

    val profileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Refresh profile on return
        playGamesManager.checkSilentSignIn { success, name, _ ->
            if (success && name != null) {
                prefs.userName = name
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = (if (isOtherUser) stringResource(id = R.string.profile_player_title) else stringResource(id = R.string.profile_title)).toAppUppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Switch Profile Button (Only for own profile)
                if (!isOtherUser) {
                    TextButton(
                        onClick = {
                            soundManager.playClick()
                            vibrationManager.vibrateClick()
                            playGamesManager.getCompareProfileIntent { intent ->
                                profileLauncher.launch(intent)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD700))
                    ) {
                        Text(
                            text = stringResource(id = R.string.profile_switch).toAppUppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(
                        2.dp,
                        if (isOtherUser) Color(0xFF3B82F6) else Color(0xFFFFD700),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = if (isOtherUser) Color(0xFF3B82F6) else Color(0xFFFFD700),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = displayName.toAppUppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isOtherUser) {
                    val cleanRank = otherRank.replace(Regex("[^0-9]"), "")
                    val rankText = if (cleanRank.isNotEmpty()) "$cleanRank." else otherRank
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${stringResource(id = R.string.profile_leaderboard_player)} • $rankText",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Google Play Games",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Stats Card (Identical aesthetic to StatsScreen)
            if (!isOtherUser) {
                val oWins = prefs.statsOnlineWins
                val oDraws = prefs.statsOnlineDraws
                val oLosses = prefs.statsOnlineLosses
                val oAbandons = prefs.statsOnlineAbandons

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.stats_tab_online).toAppUppercase(),
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(label = stringResource(id = R.string.stats_wins), value = oWins.toString(), Color.Green)
                        ProfileStatItem(label = stringResource(id = R.string.stats_draws), value = oDraws.toString(), Color.White)
                        ProfileStatItem(label = stringResource(id = R.string.stats_losses), value = oLosses.toString(), Color.Red)
                        ProfileStatItem(label = stringResource(id = R.string.stats_online_abandons), value = oAbandons.toString(), Color(0xFFF97316))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.stats_win_rate),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = calculateWinRate(oWins, oLosses, oDraws),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Other player's leaderboard stats
                val cleanRankDigits = otherRank.replace(Regex("[^0-9]"), "")
                val rankBadgeValue = if (cleanRankDigits.isNotEmpty()) "$cleanRankDigits." else otherRank

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_leaderboard_player).toAppUppercase(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(label = stringResource(id = R.string.profile_rank_label), value = rankBadgeValue, Color(0xFFFFD700))
                        ProfileStatItem(label = stringResource(id = R.string.profile_score), value = otherWins.toString(), Color.Green)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.profile_verified_player),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Play Games",
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Matches Card (Last 5 matches)
            if (!isOtherUser) {
                val oHistory = prefs.onlineHistory.takeLast(5).reversed()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.stats_recent_matches).toAppUppercase(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "5 / 5",
                            color = Color.White.copy(alpha = 0.25f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (oHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.profile_no_recent_matches),
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            oHistory.forEach { outcome ->
                                val (badgeColor, outcomeText) = when (outcome) {
                                    "win" -> Pair(Color.Green, stringResource(id = R.string.game_win))
                                    "lose" -> Pair(Color.Red, stringResource(id = R.string.game_lose))
                                    else -> Pair(Color.White.copy(alpha = 0.6f), stringResource(id = R.string.game_draw))
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = outcomeText.toAppUppercase(),
                                        color = badgeColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.toAppUppercase(),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

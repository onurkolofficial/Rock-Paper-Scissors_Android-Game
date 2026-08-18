package com.onurkolofficial.spsgame.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
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
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

data class LocalAchievement(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val icon: String
)

val ACHIEVEMENTS_LIST = listOf(
    LocalAchievement("t1", R.string.ach_t1_title, R.string.ach_t1_desc, "🏆"),
    LocalAchievement("t2", R.string.ach_t2_title, R.string.ach_t2_desc, "🔥"),
    LocalAchievement("t3", R.string.ach_t3_title, R.string.ach_t3_desc, "⚡"),
    LocalAchievement("t4", R.string.ach_t4_title, R.string.ach_t4_desc, "🤝"),
    LocalAchievement("t5", R.string.ach_t5_title, R.string.ach_t5_desc, "🎯"),
    LocalAchievement("t6", R.string.ach_t6_title, R.string.ach_t6_desc, "💰"),
    LocalAchievement("t7", R.string.ach_t7_title, R.string.ach_t7_desc, "👥"),
    LocalAchievement("t8", R.string.ach_t8_title, R.string.ach_t8_desc, "🛒"),
    LocalAchievement("t9", R.string.ach_t9_title, R.string.ach_t9_desc, "🎒")
)

enum class AchievementFilter { ALL, REMAINING, COMPLETED }

fun isAchievementUnlocked(id: String, prefs: GamePreferences): Boolean {
    return when (id) {
        "t1" -> prefs.statsWins >= 6
        "t2" -> prefs.statsWins >= 5
        "t3" -> prefs.statsWins >= 3
        "t4" -> prefs.statsDraws >= 10
        "t5" -> prefs.statsWins >= 5
        "t6" -> prefs.statsWins >= 10
        "t7" -> true
        "t8" -> prefs.ownedSkins.size > 1 || prefs.ironCount > 0 || prefs.iceCount > 0 || prefs.steelCount > 0
        "t9" -> prefs.ironCount < 5 && prefs.ownedSkins.size > 1
        else -> false
    }
}

@Composable
fun AchievementsScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    onNavigateBack: () -> Unit
) {
    var currentFilter by remember { mutableStateOf(AchievementFilter.ALL) }
    val filteredList = ACHIEVEMENTS_LIST.filter { ach ->
        val unlocked = isAchievementUnlocked(ach.id, prefs)
        when (currentFilter) {
            AchievementFilter.ALL -> true
            AchievementFilter.COMPLETED -> unlocked
            AchievementFilter.REMAINING -> !unlocked
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
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.achievements).toAppUppercase(),
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
                        playGamesManager.showAchievements()
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

            Spacer(modifier = Modifier.height(16.dp))
            
            // Unified Filter Tabs (Same aesthetic as RulesModal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AchTabButton(
                    title = stringResource(id = R.string.ach_filter_all),
                    isSelected = currentFilter == AchievementFilter.ALL,
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    currentFilter = AchievementFilter.ALL
                }

                AchTabButton(
                    title = stringResource(id = R.string.ach_filter_remaining),
                    isSelected = currentFilter == AchievementFilter.REMAINING,
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    currentFilter = AchievementFilter.REMAINING
                }

                AchTabButton(
                    title = stringResource(id = R.string.ach_filter_completed),
                    isSelected = currentFilter == AchievementFilter.COMPLETED,
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    currentFilter = AchievementFilter.COMPLETED
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentFilter,
                label = "AchievementsFilterAnim",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    )) togetherWith (fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    ))
                }
            ) { filter ->
                val listForFilter = ACHIEVEMENTS_LIST.filter { ach ->
                    val unlocked = isAchievementUnlocked(ach.id, prefs)
                    when (filter) {
                        AchievementFilter.ALL -> true
                        AchievementFilter.COMPLETED -> unlocked
                        AchievementFilter.REMAINING -> !unlocked
                    }
                }

                if (listForFilter.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.ach_no_items).toAppUppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(listForFilter, key = { it.id }) { ach ->
                            val isUnlocked = isAchievementUnlocked(ach.id, prefs)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.02f) else Color.White.copy(alpha = 0.01f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = ach.icon, fontSize = 24.sp)
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(id = ach.titleResId),
                                            color = if (isUnlocked) Color(0xFFFFD700) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = stringResource(id = ach.descResId),
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                if (isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Unlocked",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "🔒",
                                        fontSize = 16.sp
                                    )
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
private fun AchTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Color(0xFFFFD700)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.toAppUppercase(),
            color = if (isSelected) Color(0xFF0F1112) else Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

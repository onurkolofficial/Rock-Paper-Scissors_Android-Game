package com.onurkolofficial.spsgame.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager

@Composable
fun formatRelativeTime(timestampMillis: Long): String {
    val now = remember { System.currentTimeMillis() }
    val diff = (now - timestampMillis).coerceAtLeast(0L)
    val seconds = diff / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L
    val months = days / 30L
    val years = days / 365L

    return when {
        years > 0 -> stringResource(id = R.string.time_years_ago, years)
        months > 0 -> stringResource(id = R.string.time_months_ago, months)
        days > 0 -> stringResource(id = R.string.time_days_ago, days)
        hours > 0 -> stringResource(id = R.string.time_hours_ago, hours)
        minutes > 0 -> stringResource(id = R.string.time_minutes_ago, minutes)
        seconds > 5 -> stringResource(id = R.string.time_seconds_ago, seconds)
        else -> stringResource(id = R.string.time_just_now)
    }
}

@Composable
fun NotificationsModal(
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    onOpenUpdater: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf("system") }
    val currentTime = remember { System.currentTimeMillis() }

    BackHandler {
        onClose()
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
                        onClose()
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
                    text = stringResource(id = R.string.notifications_title).toAppUppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unified Tabs: Sistem (Left) | Güncellemeler (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NotificationTabButton(
                    title = stringResource(id = R.string.notifications_tab_system),
                    isSelected = activeTab == "system",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    activeTab = "system"
                }

                NotificationTabButton(
                    title = stringResource(id = R.string.notifications_tab_updates),
                    isSelected = activeTab == "updates",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    activeTab = "updates"
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Tab Content
            AnimatedContent(
                targetState = activeTab,
                label = "NotificationsTabAnim",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                        initialOffsetX = { if (targetState == "updates") it / 4 else -it / 4 },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    )) togetherWith (fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                        targetOffsetX = { if (targetState == "updates") -it / 4 else it / 4 },
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    ))
                }
            ) { tab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (tab == "system") {
                        // System Announcements (Newest on top with relative timestamps)
                        SystemAnnouncementCard(
                            icon = "💡",
                            title = stringResource(id = R.string.notifications_system_tip_title),
                            desc = stringResource(id = R.string.notifications_system_tip_desc),
                            accentColor = Color(0xFFFFD700),
                            tagText = stringResource(id = R.string.notifications_tag_tip),
                            timestampMillis = currentTime - (15 * 60 * 1000L) // 15 Minutes Ago
                        )

                        SystemAnnouncementCard(
                            icon = "🌐",
                            title = stringResource(id = R.string.notifications_system_server_title),
                            desc = stringResource(id = R.string.notifications_system_server_desc),
                            accentColor = Color(0xFF10B981),
                            tagText = stringResource(id = R.string.notifications_tag_live),
                            timestampMillis = currentTime - (3 * 60 * 1000L)
                        )

                        SystemAnnouncementCard(
                            icon = "👋",
                            title = stringResource(id = R.string.notifications_system_welcome_title),
                            desc = stringResource(id = R.string.notifications_system_welcome_desc),
                            accentColor = Color(0xFF3B82F6),
                            tagText = stringResource(id = R.string.notifications_tag_announcement),
                            timestampMillis = currentTime - (2 * 60 * 1000L)
                        )
                    } else {
                        // Updates & Changelog (v4.1.0.31 on top)
                        if (onOpenUpdater != null) {
                            Button(
                                onClick = {
                                    soundManager.playClick()
                                    vibrationManager.vibrateClick()
                                    onOpenUpdater()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = "🚀", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.updater_title).toAppUppercase(),
                                    color = Color(0xFF0F1112),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        UpdateCard(
                            version = "v4.1.0.31",
                            title = stringResource(id = R.string.notifications_v410_title),
                            isLatest = true,
                            icon = "🚀",
                            features = listOf(
                                stringResource(id = R.string.notifications_v410_f1),
                                stringResource(id = R.string.notifications_v410_f2),
                                stringResource(id = R.string.notifications_v410_f3),
                                stringResource(id = R.string.notifications_v410_f4),
                                stringResource(id = R.string.notifications_v410_f5),
                                stringResource(id = R.string.notifications_v410_f6),
                                stringResource(id = R.string.notifications_v410_f7),
                                stringResource(id = R.string.notifications_v410_f8),
                                stringResource(id = R.string.notifications_v410_f9)
                            )
                        )

                        UpdateCard(
                            version = "v4.0.0.30",
                            title = stringResource(id = R.string.notifications_v400_title),
                            isLatest = false,
                            icon = "📦",
                            features = listOf(
                                stringResource(id = R.string.notifications_v400_f1),
                                stringResource(id = R.string.notifications_v400_f2),
                                stringResource(id = R.string.notifications_v400_f3),
                                stringResource(id = R.string.notifications_v400_f4),
                                stringResource(id = R.string.notifications_v400_f5),
                                stringResource(id = R.string.notifications_v400_f6)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTabButton(
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

@Composable
private fun UpdateCard(
    version: String,
    title: String,
    isLatest: Boolean,
    icon: String,
    features: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isLatest) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = version,
                    color = if (isLatest) Color(0xFFFFD700) else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            if (isLatest) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.notifications_latest_badge).toAppUppercase(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            features.forEach { feature ->
                Text(
                    text = feature,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SystemAnnouncementCard(
    icon: String,
    title: String,
    desc: String,
    accentColor: Color,
    tagText: String,
    timestampMillis: Long
) {
    val relativeTime = formatRelativeTime(timestampMillis = timestampMillis)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = relativeTime,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tagText.toAppUppercase(),
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = desc,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

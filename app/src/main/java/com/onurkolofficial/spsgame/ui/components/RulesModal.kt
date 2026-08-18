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
fun RulesModal(
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf("matrix") }

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
            // Header (StoreModal style: Back button on the left)
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
                    text = stringResource(id = R.string.rules_title).toAppUppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RulesTabButton(
                    title = stringResource(id = R.string.rules_tab_matrix),
                    isSelected = activeTab == "matrix",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    activeTab = "matrix"
                }

                RulesTabButton(
                    title = stringResource(id = R.string.rules_tab_modes),
                    isSelected = activeTab == "modes",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    activeTab = "modes"
                }

                RulesTabButton(
                    title = stringResource(id = R.string.rules_tab_online),
                    isSelected = activeTab == "online",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    vibrationManager.vibrateClick()
                    activeTab = "online"
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Scrollable Content
            AnimatedContent(
                targetState = activeTab,
                label = "RulesTabAnim",
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
            ) { tab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (tab) {
                        "matrix" -> {
                            Text(
                                text = stringResource(id = R.string.rules_matrix_title).toAppUppercase(),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // Basic Moves
                            RuleCard(
                                emoji = "✊",
                                name = stringResource(id = R.string.rules_move_rock),
                                desc = stringResource(id = R.string.rules_rock_desc),
                                tag = stringResource(id = R.string.rules_tag_basic).toAppUppercase(),
                                tagColor = Color(0xFF3B82F6)
                            )
                            RuleCard(
                                emoji = "✋",
                                name = stringResource(id = R.string.rules_move_paper),
                                desc = stringResource(id = R.string.rules_paper_desc),
                                tag = stringResource(id = R.string.rules_tag_basic).toAppUppercase(),
                                tagColor = Color(0xFF3B82F6)
                            )
                            RuleCard(
                                emoji = "✌️",
                                name = stringResource(id = R.string.rules_move_scissors),
                                desc = stringResource(id = R.string.rules_scissors_desc),
                                tag = stringResource(id = R.string.rules_tag_basic).toAppUppercase(),
                                tagColor = Color(0xFF3B82F6)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.rules_special_powers_title).toAppUppercase(),
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            // Special Powers
                            RuleCard(
                                emoji = "🪨",
                                name = stringResource(id = R.string.rules_move_iron),
                                desc = stringResource(id = R.string.rules_iron_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFF8B5CF6)
                            )
                            RuleCard(
                                emoji = "🧊",
                                name = stringResource(id = R.string.rules_move_ice),
                                desc = stringResource(id = R.string.rules_ice_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFF06B6D4)
                            )
                            RuleCard(
                                emoji = "🛡️",
                                name = stringResource(id = R.string.rules_move_steel),
                                desc = stringResource(id = R.string.rules_steel_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFFF59E0B)
                            )
                            RuleCard(
                                emoji = "🔥",
                                name = stringResource(id = R.string.rules_move_fire),
                                desc = stringResource(id = R.string.shop_fire_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFFFF5722)
                            )
                            RuleCard(
                                emoji = "⚡",
                                name = stringResource(id = R.string.rules_move_lightning),
                                desc = stringResource(id = R.string.shop_lightning_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFFFFEB3B)
                            )
                            RuleCard(
                                emoji = "💣",
                                name = stringResource(id = R.string.rules_move_bomb),
                                desc = stringResource(id = R.string.shop_bomb_desc),
                                tag = stringResource(id = R.string.rules_tag_special).toAppUppercase(),
                                tagColor = Color(0xFFEF4444)
                            )
                        }
                        "modes" -> {
                            // Single Player
                            RuleSectionCard(
                                emoji = "▷",
                                title = stringResource(id = R.string.rules_single_player_title),
                                desc = stringResource(id = R.string.rules_single_player_desc),
                                accentColor = Color(0xFF3B82F6)
                            )

                            // Two Player
                            RuleSectionCard(
                                emoji = "👥",
                                title = stringResource(id = R.string.rules_two_player_title),
                                desc = stringResource(id = R.string.rules_two_player_desc),
                                accentColor = Color(0xFF10B981)
                            )
                        }
                        "online" -> {
                            // Online General
                            RuleSectionCard(
                                emoji = "🌐",
                                title = stringResource(id = R.string.rules_online_title),
                                desc = "${stringResource(id = R.string.rules_online_classic_desc)}\n\n${stringResource(id = R.string.rules_online_hard_desc)}\n\n${stringResource(id = R.string.rules_online_rooms_desc)}",
                                accentColor = Color(0xFF8B5CF6)
                            )

                            // Forfeit / Abandon Rule Card
                            RuleSectionCard(
                                emoji = "🚫",
                                title = stringResource(id = R.string.rules_online_forfeit_title),
                                desc = stringResource(id = R.string.rules_online_forfeit_desc),
                                accentColor = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesTabButton(
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
private fun RuleCard(
    emoji: String,
    name: String,
    desc: String,
    tag: String,
    tagColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(tagColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .background(tagColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        color = tagColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RuleSectionCard(
    emoji: String,
    title: String,
    desc: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Text(
                text = title.toAppUppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = desc,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

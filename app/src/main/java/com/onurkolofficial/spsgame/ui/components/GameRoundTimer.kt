package com.onurkolofficial.spsgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameRoundTimer(
    secondsLeft: Int?,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = secondsLeft != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (secondsLeft != null) {
            val timerColor = if (secondsLeft <= 3) Color(0xFFEF4444) else Color(0xFFFFD700)
            
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(timerColor.copy(alpha = 0.15f))
                    .border(1.dp, timerColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (label != null) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "$secondsLeft",
                        color = timerColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

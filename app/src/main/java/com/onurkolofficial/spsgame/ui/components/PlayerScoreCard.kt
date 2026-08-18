package com.onurkolofficial.spsgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.model.Move

@Composable
fun PlayerScoreCard(
    playerName: String,
    score: Int,
    skin: Skin,
    currentMove: Move? = null,
    isOpponent: Boolean = false,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isOpponent) Color(0xFFEF4444) else Color(0xFF10B981)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.15f),
            Color(0xFF1E2124).copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playerName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$score",
                color = accentColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Move representation
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentMove != null) {
                    when (currentMove) {
                        Move.ROCK, Move.PAPER, Move.SCISSORS -> {
                            SkinIcon(
                                skin = skin,
                                type = currentMove.toId(),
                                modifier = Modifier.size(28.dp),
                                fontSize = 20.sp
                            )
                        }
                        Move.IRON -> {
                            Text(text = "🛡️", fontSize = 20.sp)
                        }
                        Move.ICE -> {
                            Text(text = "🧊", fontSize = 20.sp)
                        }
                        Move.STEEL -> {
                            Text(text = "⚔️", fontSize = 20.sp)
                        }
                    }
                } else {
                    Text(
                        text = "?",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

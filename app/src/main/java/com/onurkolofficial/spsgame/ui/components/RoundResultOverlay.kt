package com.onurkolofficial.spsgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.model.GameResult

@Composable
fun RoundResultOverlay(
    result: GameResult?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = result != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut()
    ) {
        if (result != null) {
            val (textRes, color) = when (result) {
                GameResult.WIN -> Pair(R.string.game_win, Color(0xFF10B981))
                GameResult.LOSE -> Pair(R.string.game_lose, Color(0xFFEF4444))
                GameResult.DRAW -> Pair(R.string.game_draw, Color(0xFFFFD700))
            }

            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.2f))
                    .border(2.dp, color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = textRes),
                    color = color,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

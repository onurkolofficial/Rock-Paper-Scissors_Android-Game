package com.onurkolofficial.spsgame.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.model.Move

@Composable
fun MoveSelectorView(
    activeSkin: Skin,
    ironCount: Int = 0,
    iceCount: Int = 0,
    steelCount: Int = 0,
    selectedMove: Move? = null,
    isInputEnabled: Boolean = true,
    showSpecialMoves: Boolean = true,
    onMoveSelected: (Move) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Normal Moves
        MoveButton(
            move = Move.ROCK,
            skin = activeSkin,
            isSelected = selectedMove == Move.ROCK,
            isEnabled = isInputEnabled,
            onClick = { onMoveSelected(Move.ROCK) }
        )
        Spacer(modifier = Modifier.width(10.dp))
        MoveButton(
            move = Move.PAPER,
            skin = activeSkin,
            isSelected = selectedMove == Move.PAPER,
            isEnabled = isInputEnabled,
            onClick = { onMoveSelected(Move.PAPER) }
        )
        Spacer(modifier = Modifier.width(10.dp))
        MoveButton(
            move = Move.SCISSORS,
            skin = activeSkin,
            isSelected = selectedMove == Move.SCISSORS,
            isEnabled = isInputEnabled,
            onClick = { onMoveSelected(Move.SCISSORS) }
        )

        // Special Moves
        if (showSpecialMoves) {
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Iron
            SpecialMoveButton(
                move = Move.IRON,
                drawableResId = R.drawable.gfx_iron,
                count = ironCount,
                isSelected = selectedMove == Move.IRON,
                isEnabled = isInputEnabled && ironCount > 0,
                borderColor = Color(0xFFFFB300),
                onClick = { onMoveSelected(Move.IRON) }
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Ice
            SpecialMoveButton(
                move = Move.ICE,
                emoji = "🧊",
                count = iceCount,
                isSelected = selectedMove == Move.ICE,
                isEnabled = isInputEnabled && iceCount > 0,
                borderColor = Color(0xFF00E5FF),
                onClick = { onMoveSelected(Move.ICE) }
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Steel
            SpecialMoveButton(
                move = Move.STEEL,
                drawableResId = R.drawable.gfx_steel,
                count = steelCount,
                isSelected = selectedMove == Move.STEEL,
                isEnabled = isInputEnabled && steelCount > 0,
                borderColor = Color(0xFFB0BEC5),
                onClick = { onMoveSelected(Move.STEEL) }
            )
        }
    }
}

@Composable
fun MoveButton(
    move: Move,
    skin: Skin,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "MoveButtonScale"
    )

    val borderColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f)
    val bgColor = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .scale(scale)
            .size(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SkinIcon(
            skin = skin,
            type = move.toId(),
            modifier = Modifier.size(38.dp),
            fontSize = 30.sp
        )
    }
}

@Composable
fun SpecialMoveButton(
    move: Move,
    count: Int,
    isSelected: Boolean,
    isEnabled: Boolean,
    borderColor: Color,
    drawableResId: Int? = null,
    emoji: String? = null,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "SpecialMoveButtonScale"
    )

    val border = if (isSelected) 2.dp else 1.dp
    val actualBorderColor = if (isEnabled) {
        if (isSelected) borderColor else borderColor.copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.06f)
    }

    val actualBgColor = if (isEnabled) {
        if (isSelected) borderColor.copy(alpha = 0.2f) else borderColor.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.02f)
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .size(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(actualBgColor)
            .border(border, actualBorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = move.toId(),
                modifier = Modifier
                    .size(34.dp)
                    .then(if (!isEnabled) Modifier.background(Color.Transparent) else Modifier)
            )
        } else if (emoji != null) {
            Text(
                text = emoji,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        }

        // Count Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (count > 0) borderColor else Color.Gray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

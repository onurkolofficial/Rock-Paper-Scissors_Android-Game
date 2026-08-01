package com.onurkolofficial.spsgame.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.onurkolofficial.spsgame.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameLoadingScreen(
    targetMode: String,
    onNavigateNext: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3000)
    )
    
    val msgVisuals = stringResource(id = R.string.loading_visuals)
    val msgSounds = stringResource(id = R.string.loading_sounds)
    val msgEffects = stringResource(id = R.string.loading_effects)
    val msgResources = stringResource(id = R.string.loading_resources)
    val msgServer = stringResource(id = R.string.loading_server)
    val msgRoom = stringResource(id = R.string.loading_rooms)
    val msgGameLoading = stringResource(id = R.string.loading_game)
    val msgGameStarting = stringResource(id = R.string.loading_start)
    
    val messages = remember(targetMode, msgVisuals, msgSounds, msgEffects, msgResources, msgServer) {
        val baseMessages = mutableListOf(msgVisuals, msgSounds, msgEffects, msgResources)
        if (targetMode == "online") {
            baseMessages.addAll(mutableListOf(msgServer, msgRoom))
        }
        baseMessages.add(msgGameStarting)

        baseMessages
    }
    
    val currentMessageIndex = (animatedProgress * messages.size).toInt().coerceAtMost(messages.size - 1)
    val currentMessage = messages[currentMessageIndex]

    LaunchedEffect(Unit) {
        progress = 1f
        delay(2800.milliseconds) // Wait slightly more than animation
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1112)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = msgGameLoading,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = currentMessage,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFFFD700),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "%${(animatedProgress * 100).toInt()}",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

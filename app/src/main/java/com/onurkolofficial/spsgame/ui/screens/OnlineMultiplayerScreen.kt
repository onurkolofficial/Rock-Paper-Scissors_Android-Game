package com.onurkolofficial.spsgame.ui.screens

import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.model.GameResult
import com.onurkolofficial.spsgame.model.Move
import com.onurkolofficial.spsgame.ui.components.AlertModal
import com.onurkolofficial.spsgame.ui.components.ConfirmModal
import com.onurkolofficial.spsgame.ui.components.SKINS_LIST
import com.onurkolofficial.spsgame.utils.GameAppConfig
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import com.onurkolofficial.spsgame.utils.PlayGamesManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class MultiplayerMode { SELECTION, MATCHMAKING, CREATE_ROOM, JOIN_ROOM, CONNECTING, IN_GAME }
enum class MatchStatus { CONNECTING, WAITING, STARTING, PLAYING, RESULT, GAME_OVER, OPPONENT_DISCONNECTED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMultiplayerScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    playGamesManager: PlayGamesManager,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = object {
        fun runOnUiThread(action: () -> Unit) {
            coroutineScope.launch { action() }
        }
    }
    
    val socketUrl = GameAppConfig.SOCKET_URL
    var socket by remember { mutableStateOf<Socket?>(null) }
    var gameMode by remember { mutableStateOf(MultiplayerMode.SELECTION) }
    var matchStatus by remember { mutableStateOf(MatchStatus.CONNECTING) }
    
    var roomCodeInput by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showConfirmExit by remember { mutableStateOf(false) }
    
    var opponentName by remember { mutableStateOf("Opponent") }
    var currentRound by remember { mutableIntStateOf(1) }
    var totalDraws by remember { mutableIntStateOf(0) }
    
    var myMove by remember { mutableStateOf<Move?>(null) }
    var opponentMove by remember { mutableStateOf<Move?>(null) }
    var roundResult by remember { mutableStateOf<GameResult?>(null) }
    var finalResult by remember { mutableStateOf<GameResult?>(null) }
    
    var myScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    
    var timerVal by remember { mutableStateOf<Int?>(null) }
    var nextRoundTimerVal by remember { mutableStateOf<Int?>(null) }
    var startingTimerVal by remember { mutableStateOf<Int?>(null) }
    
    val activeSkin = remember { SKINS_LIST.find { it.id == prefs.activeSkin } ?: SKINS_LIST[0] }
    var opponentSkinId by remember { mutableStateOf("default") }
    val opponentSkin = remember(opponentSkinId) { SKINS_LIST.find { it.id == opponentSkinId } ?: SKINS_LIST[0] }

    val handleDisconnect = {
        socket?.disconnect()
        socket = null
    }

    val handleBackPress = {
        if (matchStatus == MatchStatus.STARTING) {
            // Do nothing during GameLoadingScreen
        } else if (gameMode == MultiplayerMode.IN_GAME && matchStatus != MatchStatus.GAME_OVER) {
            showConfirmExit = true
        } else if (gameMode != MultiplayerMode.SELECTION && gameMode != MultiplayerMode.IN_GAME) {
            soundManager.playClick()
            handleDisconnect()
            gameMode = MultiplayerMode.SELECTION
        } else {
            soundManager.playClick()
            handleDisconnect()
            playGamesManager.saveGame()
            onNavigateBack()
        }
    }

    BackHandler {
        handleBackPress()
    }

    val connectAndEmit = { event: String, data: JSONObject ->
        if (socket == null) {
            try {
                val opts = IO.Options().apply {
                    forceNew = true
                    reconnection = true
                }
                socket = IO.socket(socketUrl, opts)
            } catch (e: Exception) {
                Log.e("OnlineScreen", "Connection error", e)
                errorMsg = "Connection failed to server."
                gameMode = MultiplayerMode.SELECTION
            }
        }
        
        socket?.let { s ->
            // Listeners
            s.on(Socket.EVENT_CONNECT) {
                activity.runOnUiThread {
                    s.emit(event, data)
                }
            }
            
            s.on("waiting_for_opponent") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.WAITING
                    roomId = res.optString("roomId")
                }
            }
            
            s.on("private_room_created") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.WAITING
                    roomId = res.optString("roomId")
                }
            }
            
            s.on("join_error") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    errorMsg = res.optString("message")
                    gameMode = MultiplayerMode.SELECTION
                    matchStatus = MatchStatus.CONNECTING
                }
            }
            
            s.on("match_found") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.STARTING
                    val players = res.optJSONArray("players")
                    if (players != null) {
                        for (i in 0 until players.length()) {
                            val p = players.getJSONObject(i)
                            if (p.optString("id") != s.id()) {
                                opponentName = p.optString("name")
                                opponentSkinId = p.optString("skin", "default")
                            }
                        }
                    }
                    currentRound = res.optInt("round", 1)
                    totalDraws = res.optInt("draws", 0)
                    soundManager.playClick()
                    vibrationManager.vibrate(50)
                    
                    // Start 3 second pre-match starting count down
                    startingTimerVal = 3
                }
            }
            
            s.on("game_starting") {
                activity.runOnUiThread {
                    matchStatus = MatchStatus.PLAYING
                    gameMode = MultiplayerMode.IN_GAME
                    timerVal = 10 // 10s round timer
                }
            }
            
            s.on("round_result") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    val outcome = GameResult.fromId(res.optString("result"))
                    roundResult = outcome
                    opponentMove = Move.fromId(res.optString("opponentMove"))
                    myScore = res.optInt("score")
                    opponentScore = res.optInt("opponentScore")
                    totalDraws = res.optInt("draws")
                    currentRound = res.optInt("round")
                    matchStatus = MatchStatus.RESULT
                    timerVal = null
                    
                    if (currentRound < 10) {
                        nextRoundTimerVal = 3
                    }
                    
                    when (outcome) {
                        GameResult.WIN -> soundManager.playWin()
                        GameResult.LOSE -> soundManager.playLose()
                        else -> soundManager.playDraw()
                    }
                    vibrationManager.vibrate(100)
                }
            }
            
            s.on("next_round") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.PLAYING
                    myMove = null
                    opponentMove = null
                    roundResult = null
                    nextRoundTimerVal = null
                    currentRound = res.optInt("round")
                    timerVal = 10
                }
            }
            
            s.on("game_over") { args ->
                val res = args[0] as JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.GAME_OVER
                    val finalOutcome = GameResult.fromId(res.optString("result"))
                    finalResult = finalOutcome
                    timerVal = null
                    
                    // Update stats
                    when (finalOutcome) {
                        GameResult.WIN -> prefs.statsOnlineWins++
                        GameResult.LOSE -> prefs.statsOnlineLosses++
                        GameResult.DRAW -> prefs.statsOnlineDraws++
                        else -> {}
                    }
                    
                    // Save history
                    val historyList = prefs.onlineHistory.toMutableList()
                    historyList.add(0, finalOutcome?.toId() ?: "draw")
                    prefs.onlineHistory = historyList.take(5)
                    
                    playGamesManager.saveGame()
                }
            }
            
            s.on("opponent_disconnected") { args ->
                val res = args.getOrNull(0) as? JSONObject
                activity.runOnUiThread {
                    matchStatus = MatchStatus.OPPONENT_DISCONNECTED
                    if (res?.optBoolean("wasPlaying") == true) {
                        finalResult = GameResult.WIN
                        prefs.statsOnlineWins++
                        
                        val historyList = prefs.onlineHistory.toMutableList()
                        historyList.add(0, "win")
                        prefs.onlineHistory = historyList.take(5)
                        
                        playGamesManager.saveGame()
                    }
                }
            }
            
            if (!s.connected()) {
                s.connect()
            } else {
                s.emit(event, data)
            }
        }
    }

    val startMatchmaking = {
        val data = JSONObject().put("name", prefs.userName).put("skin", prefs.activeSkin)
        gameMode = MultiplayerMode.MATCHMAKING
        matchStatus = MatchStatus.CONNECTING
        connectAndEmit("join_matchmaking", data)
    }

    val createRoom = {
        val data = JSONObject().put("name", prefs.userName).put("skin", prefs.activeSkin)
        gameMode = MultiplayerMode.CREATE_ROOM
        matchStatus = MatchStatus.CONNECTING
        connectAndEmit("create_private_room", data)
    }

    val joinRoom = { code: String ->
        val data = JSONObject().put("name", prefs.userName).put("roomId", code).put("skin", prefs.activeSkin)
        gameMode = MultiplayerMode.CONNECTING
        matchStatus = MatchStatus.CONNECTING
        connectAndEmit("join_private_room", data)
    }

    val handleMoveSelection: (Move) -> Unit = { move ->
        if (socket != null && matchStatus == MatchStatus.PLAYING && myMove == null) {
            soundManager.playClick()
            vibrationManager.vibrate(50)
            myMove = move
            socket?.emit("send_move", JSONObject().put("move", move.toId()))
        }
    }

    // Handlers for starting timers
    LaunchedEffect(timerVal) {
        if (timerVal != null && timerVal!! > 0 && matchStatus == MatchStatus.PLAYING) {
            delay(1000)
            timerVal = timerVal!! - 1
        } else if (timerVal == 0) {
            socket?.emit("timeout_from_client", JSONObject())
        }
    }

    LaunchedEffect(nextRoundTimerVal) {
        if (nextRoundTimerVal != null && nextRoundTimerVal!! > 0) {
            delay(1000)
            nextRoundTimerVal = nextRoundTimerVal!! - 1
        }
    }

    LaunchedEffect(startingTimerVal) {
        if (startingTimerVal != null && startingTimerVal!! > 0) {
            delay(1000)
            startingTimerVal = startingTimerVal!! - 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Register a loss if client disconnects early in game
            if (matchStatus == MatchStatus.PLAYING || matchStatus == MatchStatus.RESULT) {
                prefs.statsOnlineLosses++
                val historyList = prefs.onlineHistory.toMutableList()
                historyList.add(0, "lose")
                prefs.onlineHistory = historyList.take(5)
            }
            handleDisconnect()
        }
    }

    if (showConfirmExit) {
        ConfirmModal(
            message = "Oyundan çıkmak istediğinize emin misiniz? Devam eden maçınız kayıp olarak sayılacaktır.",
            onConfirm = {
                showConfirmExit = false
                handleDisconnect()
                onNavigateBack()
            },
            onCancel = { showConfirmExit = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1112))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Central Control Panel (divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playClick()
                        if (gameMode == MultiplayerMode.IN_GAME) {
                            showConfirmExit = true
                        } else {
                            handleDisconnect()
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit",
                        tint = Color.Red
                    )
                }

                if (matchStatus == MatchStatus.PLAYING || matchStatus == MatchStatus.RESULT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = opponentScore.toString(),
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            modifier = Modifier.rotate(180f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Round $currentRound/10",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Draws: $totalDraws",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 11.sp
                                )
                                timerVal?.let { t ->
                                    Text(
                                        text = "⏳ ${t}s",
                                        color = Color.Red.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = myScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }
                } else {
                    Text(
                        text = "WIFI CONNECTED",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            // Top Half - Opponent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hand Card
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (matchStatus == MatchStatus.RESULT && opponentMove != null) {
                            HandImage(move = opponentMove!!, skin = opponentSkin, modifier = Modifier.size(60.dp))
                        } else if (matchStatus == MatchStatus.PLAYING && opponentMove != null) {
                            Text(text = "✓", color = Color.Green, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "?", color = Color.White.copy(alpha = 0.1f), fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Opponent name (rendered straight and above the selection Box)
                    Text(
                        text = opponentName.toAppUppercase(),
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.rotate(180f)
                    )
                }
            }

            // Bottom Half - Player
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hand Card
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (myMove != null) {
                            HandImage(move = myMove!!, skin = activeSkin, modifier = Modifier.size(60.dp))
                        } else {
                            Text(text = "?", color = Color.White.copy(alpha = 0.1f), fontSize = 36.sp)
                        }
                    }

                    // Move Select buttons
                    if (matchStatus == MatchStatus.PLAYING && myMove == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(Move.ROCK, Move.PAPER, Move.SCISSORS).forEach { move ->
                                MoveSelectionCard(move = move, skin = activeSkin, enabled = true) {
                                    handleMoveSelection(move)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = prefs.userName.toAppUppercase(),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Overlay 1: Matchmaking Menu
        AnimatedVisibility(
            visible = gameMode != MultiplayerMode.IN_GAME,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Exit Back
                IconButton(
                    onClick = { handleBackPress() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                when (gameMode) {
                    MultiplayerMode.SELECTION -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.online_title),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            Button(
                                onClick = { startMatchmaking() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_matchmaking),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { createRoom() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_create_room),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    soundManager.playClick()
                                    gameMode = MultiplayerMode.JOIN_ROOM
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_join_room),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    MultiplayerMode.JOIN_ROOM -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.online_join_room_title),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = roomCodeInput,
                                onValueChange = { roomCodeInput = it.take(6).toAppUppercase() },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.online_room_code_placeholder),
                                        color = Color.White.copy(alpha = 0.3f)
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = { joinRoom(roomCodeInput) },
                                enabled = roomCodeInput.length >= 3,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.online_join_button).toAppUppercase(),
                                    color = Color(0xFF0F1112),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Text(
                                text = stringResource(id = R.string.online_cancel),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        soundManager.playClick()
                                        gameMode = MultiplayerMode.SELECTION
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                    MultiplayerMode.MATCHMAKING, MultiplayerMode.CREATE_ROOM, MultiplayerMode.CONNECTING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6))
                            
                            Text(
                                text = when {
                                    gameMode == MultiplayerMode.CREATE_ROOM && roomId != null -> stringResource(id = R.string.online_room_created)
                                    matchStatus == MatchStatus.WAITING -> stringResource(id = R.string.online_waiting)
                                    else -> stringResource(id = R.string.online_connecting)
                                },
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (gameMode == MultiplayerMode.CREATE_ROOM && roomId != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.online_room_code_label).toAppUppercase(),
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = roomId!!,
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(id = R.string.online_room_share_msg),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // Overlay 2: Player Joined Countdown
        if (matchStatus == MatchStatus.STARTING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.online_player_joined).toAppUppercase(),
                        color = Color.Green,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = opponentName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = startingTimerVal?.toString() ?: "",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Overlay 3: Round Results
        if (matchStatus == MatchStatus.RESULT && roundResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val outcomeColor = when (roundResult) {
                        GameResult.WIN -> Color.Green
                        GameResult.LOSE -> Color.Red
                        else -> Color.White
                    }
                    val outcomeText = when (roundResult) {
                        GameResult.WIN -> stringResource(id = R.string.game_win)
                        GameResult.LOSE -> stringResource(id = R.string.game_lose)
                        else -> stringResource(id = R.string.game_draw)
                    }

                    Text(
                        text = outcomeText.toAppUppercase(),
                        color = outcomeColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    nextRoundTimerVal?.let { t ->
                        Text(
                            text = "Next Round in $t...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Overlay 4: Match Finished or Opponent Disconnected
        if (matchStatus == MatchStatus.GAME_OVER || matchStatus == MatchStatus.OPPONENT_DISCONNECTED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    if (matchStatus == MatchStatus.OPPONENT_DISCONNECTED) {
                        Text(
                            text = "RAKİP OYUNDAN AYRILDI",
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        if (finalResult == GameResult.WIN) {
                            Text(
                                text = "KAZANDINIZ!",
                                color = Color.Green,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val overText = when (finalResult) {
                            GameResult.WIN -> stringResource(id = R.string.game_win)
                            GameResult.LOSE -> stringResource(id = R.string.game_lose)
                            else -> stringResource(id = R.string.game_draw)
                        }
                        val overColor = when (finalResult) {
                            GameResult.WIN -> Color.Green
                            GameResult.LOSE -> Color.Red
                            else -> Color.White
                        }
                        
                        Text(
                            text = "MAÇ TAMAMLANDI",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = overText.toAppUppercase(),
                            color = overColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            soundManager.playClick()
                            matchStatus = MatchStatus.CONNECTING
                            gameMode = MultiplayerMode.SELECTION
                            roomId = null
                            myMove = null
                            opponentMove = null
                            roundResult = null
                            finalResult = null
                            myScore = 0
                            opponentScore = 0
                            handleDisconnect()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rematch", tint = Color.White)
                            Text(
                                text = "YENİ EŞLEŞME",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Button(
                        onClick = {
                            soundManager.playClick()
                            handleDisconnect()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            text = "MENÜYE DÖN",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Alert message Dialog
        errorMsg?.let { msg ->
            AlertModal(
                title = "HATA",
                message = msg,
                onDismiss = { errorMsg = null }
            )
        }
    }
}


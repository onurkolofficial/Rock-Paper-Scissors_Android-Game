package com.onurkolofficial.spsgame.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.SoundManager

data class Skin(
    val id: String,
    val nameResId: Int,
    val cost: Int,
    val rockResId: Int? = null,
    val paperResId: Int? = null,
    val scissorsResId: Int? = null,
    val rockEmoji: String? = null,
    val paperEmoji: String? = null,
    val scissorsEmoji: String? = null
)

data class ConsumableItem(
    val id: String,
    val nameResId: Int,
    val descResId: Int,
    val baseCost: Int,
    val drawableResId: Int? = null,
    val emoji: String? = null
)

val SKINS_LIST = listOf(
    Skin("default", R.string.skin_default, 0, R.drawable.gfx_stone, R.drawable.gfx_paper, R.drawable.gfx_scissors),
    Skin("modern", R.string.skin_modern, 300, rockEmoji = "🪨", paperEmoji = "📄", scissorsEmoji = "✂️"),
    Skin("neon", R.string.skin_neon, 400, rockEmoji = "💎", paperEmoji = "📜", scissorsEmoji = "⚡"),
    Skin("fancy", R.string.skin_fancy, 500, rockEmoji = "✊", paperEmoji = "✋", scissorsEmoji = "✌️"),
    Skin("gloved", R.string.skin_gloved, 750, rockEmoji = "🥊", paperEmoji = "🧤", scissorsEmoji = "✌🏼"),
    Skin("biker", R.string.skin_biker, 1000, R.drawable.gfx_skin_bike_rock, R.drawable.gfx_skin_bike_paper, R.drawable.gfx_skin_bike_scissors)
)

val CONSUMABLES_LIST = listOf(
    ConsumableItem("iron", R.string.shop_iron, R.string.shop_iron_desc, 45, drawableResId = R.drawable.gfx_iron),
    ConsumableItem("ice", R.string.shop_ice, R.string.shop_ice_desc, 95, emoji = "🧊"),
    ConsumableItem("steel", R.string.shop_steel, R.string.shop_steel_desc, 100, drawableResId = R.drawable.gfx_steel)
)

@Composable
fun StoreModal(
    prefs: GamePreferences,
    soundManager: SoundManager,
    onClose: () -> Unit,
    onRefreshCash: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("skins") }
    var selectedItem by remember { mutableStateOf<ConsumableItem?>(null) }
    var quantity by remember { mutableIntStateOf(5) }
    var ownedSkins by remember { mutableStateOf(prefs.ownedSkins) }
    var activeSkinId by remember { mutableStateOf(prefs.activeSkin) }
    var alertMessage by remember { mutableStateOf<String?>(null) }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            soundManager.playClick()
                            onClose()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.shop_title),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = prefs.statsCash.toString(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                TabButton(
                    text = stringResource(id = R.string.shop_tab_skins),
                    isSelected = activeTab == "skins",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    activeTab = "skins"
                }
                TabButton(
                    text = stringResource(id = R.string.shop_tab_items),
                    isSelected = activeTab == "consumables",
                    modifier = Modifier.weight(1f)
                ) {
                    soundManager.playClick()
                    activeTab = "consumables"
                }
            }

            // List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeTab == "skins") {
                    items(SKINS_LIST) { skin ->
                        val isOwned = ownedSkins.contains(skin.id)
                        val isActive = activeSkinId == skin.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isActive) Color(0xFFFFD700).copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isActive) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Mini icon preview
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SkinIcon(skin = skin, type = "rock", modifier = Modifier.size(32.dp))
                                }

                                Column {
                                    Text(
                                        text = stringResource(id = skin.nameResId),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        SkinIcon(skin = skin, type = "rock", modifier = Modifier.size(16.dp))
                                        SkinIcon(skin = skin, type = "paper", modifier = Modifier.size(16.dp))
                                        SkinIcon(skin = skin, type = "scissors", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Buy / Equip Button
                            when {
                                isActive -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color.Green.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Equipped",
                                            tint = Color.Green,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = stringResource(id = R.string.shop_equipped),
                                            color = Color.Green,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                isOwned -> {
                                    Button(
                                        onClick = {
                                            soundManager.playClick()
                                            prefs.activeSkin = skin.id
                                            activeSkinId = skin.id
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.shop_equip),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            soundManager.playClick()
                                            if (prefs.statsCash >= skin.cost) {
                                                prefs.statsCash -= skin.cost
                                                val newOwned = prefs.ownedSkins + skin.id
                                                prefs.ownedSkins = newOwned
                                                ownedSkins = newOwned
                                                onRefreshCash()
                                            } else {
                                                alertMessage = context.getString(R.string.shop_insufficient_cash)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Lock",
                                                tint = Color(0xFF0F1112),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "${stringResource(id = R.string.shop_buy)} $${skin.cost}",
                                                color = Color(0xFF0F1112),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(CONSUMABLES_LIST) { item ->
                        val currentQty = when (item.id) {
                            "iron" -> prefs.ironCount
                            "ice" -> prefs.iceCount
                            "steel" -> prefs.steelCount
                            else -> 0
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.drawableResId != null) {
                                        Image(
                                            painter = painterResource(id = item.drawableResId),
                                            contentDescription = stringResource(id = item.nameResId),
                                            modifier = Modifier.size(32.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text(
                                            text = item.emoji ?: "",
                                            fontSize = 24.sp
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = stringResource(id = item.nameResId),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = stringResource(id = item.descResId),
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "x$currentQty",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        soundManager.playClick()
                                        selectedItem = item
                                        quantity = 5
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.shop_buy),
                                        color = Color(0xFF0F1112),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Alert Modal
        alertMessage?.let { msg ->
            AlertModal(
                title = stringResource(id = R.string.login_error_title),
                message = msg,
                onDismiss = { alertMessage = null }
            )
        }

        // Quantity Selection Modal
        selectedItem?.let { item ->
            Dialog(onDismissRequest = { selectedItem = null }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = stringResource(id = item.nameResId).uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Option quantity buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(5, 10, 15).forEach { amt ->
                                val totalCost = amt * item.baseCost
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (quantity == amt) Color(0xFFFFD700).copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.2f)
                                        )
                                        .border(
                                            1.dp,
                                            if (quantity == amt) Color(0xFFFFD700) else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            soundManager.playClick()
                                            quantity = amt
                                        }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$amt ${stringResource(id = R.string.shop_pieces)}",
                                        color = if (quantity == amt) Color(0xFFFFD700) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "$$totalCost",
                                        color = if (quantity == amt) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Purchase Confirm
                        Button(
                            onClick = {
                                soundManager.playClick()
                                val finalCost = quantity * item.baseCost
                                if (prefs.statsCash >= finalCost) {
                                    prefs.statsCash -= finalCost
                                    when (item.id) {
                                        "iron" -> prefs.ironCount += quantity
                                        "ice" -> prefs.iceCount += quantity
                                        "steel" -> prefs.steelCount += quantity
                                    }
                                    selectedItem = null
                                    onRefreshCash()
                                } else {
                                    alertMessage = context.getString(R.string.shop_insufficient_cash)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.shop_confirm_purchase)} ($${quantity * item.baseCost})",
                                color = Color(0xFF0F1112),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkinIcon(skin: Skin, type: String, modifier: Modifier = Modifier) {
    if (skin.id == "default" || skin.id == "biker") {
        val drawableId = when (skin.id) {
            "default" -> when (type) {
                "rock" -> R.drawable.gfx_stone
                "paper" -> R.drawable.gfx_paper
                else -> R.drawable.gfx_scissors
            }
            else -> when (type) {
                "rock" -> R.drawable.gfx_skin_bike_rock
                "paper" -> R.drawable.gfx_skin_bike_paper
                else -> R.drawable.gfx_skin_bike_scissors
            }
        }
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = type,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        val emoji = when (type) {
            "rock" -> skin.rockEmoji
            "paper" -> skin.paperEmoji
            else -> skin.scissorsEmoji
        } ?: ""
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = if (modifier == Modifier.size(16.dp)) 12.sp else 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .background(if (isSelected) Color(0xFFFFD700) else Color.Transparent)
        )
    }
}

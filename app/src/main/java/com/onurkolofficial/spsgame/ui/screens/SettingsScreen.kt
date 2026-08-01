package com.onurkolofficial.spsgame.ui.screens
import com.onurkolofficial.spsgame.ui.localization.toAppUppercase

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurkolofficial.spsgame.R
import com.onurkolofficial.spsgame.data.GamePreferences
import com.onurkolofficial.spsgame.utils.SoundManager
import com.onurkolofficial.spsgame.utils.VibrationManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: GamePreferences,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    onNavigateBack: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current

    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var musicVolume by remember { mutableStateOf(prefs.musicVolume) }
    var sfxVolume by remember { mutableStateOf(prefs.sfxVolume) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }
    var adsEnabled by remember { mutableStateOf(prefs.adsInterstitialEnabled) }
    var lang by remember { mutableStateOf(prefs.language) }

    val changeLanguage = { selectedLang: String ->
        soundManager.playClick()
        vibrationManager.vibrateClick()
        prefs.language = selectedLang
        lang = selectedLang
        
        // The new LocaleHelper in MainActivity will automatically provide the new localized context 
        // throughout the Composition tree when appLanguage state is updated via onLanguageChanged.
        onLanguageChanged(selectedLang)
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
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.settings_title).toAppUppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Toggles & Sliders Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sound Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                soundManager.playClick()
                                vibrationManager.vibrateClick()
                                val newState = !soundEnabled
                                prefs.soundEnabled = newState
                                soundEnabled = newState
                                soundManager.updateBgmVolume()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.settings_sound),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(id = R.string.settings_volume),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                        CustomToggle(checked = soundEnabled)
                    }

                    // Music & SFX Volume Sliders
                    if (soundEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.settings_music_volume),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(musicVolume * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = musicVolume,
                            onValueChange = {
                                prefs.musicVolume = it
                                musicVolume = it
                                soundManager.updateBgmVolume()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFD700),
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.settings_sfx_volume),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(sfxVolume * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = sfxVolume,
                            onValueChange = {
                                prefs.sfxVolume = it
                                sfxVolume = it
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFD700),
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Vibration Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                soundManager.playClick()
                                vibrationManager.vibrateClick()
                                val newState = !vibrationEnabled
                                prefs.vibrationEnabled = newState
                                vibrationEnabled = newState
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_vibration),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        CustomToggle(checked = vibrationEnabled)
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Interstitial Ads Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                soundManager.playClick()
                                vibrationManager.vibrateClick()
                                val newState = !adsEnabled
                                prefs.adsInterstitialEnabled = newState
                                adsEnabled = newState
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_ads_interstitial),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        CustomToggle(checked = adsEnabled)
                    }
                }

                // Language selection Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_language).toAppUppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { changeLanguage("tr") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (lang == "tr") Color(0xFFFFD700) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "TÜRKÇE",
                                color = if (lang == "tr") Color(0xFFFFD700) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { changeLanguage("en") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (lang == "en") Color(0xFFFFD700) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "ENGLISH",
                                color = if (lang == "en") Color(0xFFFFD700) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Removed Google Drive Backup Box
            }
        }
    }
}

@Composable
fun CustomToggle(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) Color.White else Color(0xFF2C2E30)
    val thumbColor = if (checked) Color.Black else Color(0xFF888888)
    val alignment by animateFloatAsState(targetValue = if (checked) 1f else 0f)

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .background(color = trackColor, shape = RoundedCornerShape(14.dp))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = (22.dp * alignment))
                .size(20.dp)
                .background(color = thumbColor, shape = CircleShape)
        )
    }
}


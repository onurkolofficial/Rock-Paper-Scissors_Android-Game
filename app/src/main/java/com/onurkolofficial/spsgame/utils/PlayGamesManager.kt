package com.onurkolofficial.spsgame.utils

import android.app.Activity
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.games.SnapshotsClient
import com.google.gson.Gson
import com.onurkolofficial.spsgame.data.GamePreferences
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.gms.games.leaderboard.LeaderboardScore
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer

data class LeaderboardEntry(
    val rank: String,
    val name: String,
    val score: String,
    val iconUri: String?,
    val playerId: String? = null
)

data class CloudSaveData(
    val statsWins: Int = 0,
    val statsLosses: Int = 0,
    val statsDraws: Int = 0,
    val statsOnlineWins: Int = 0,
    val statsOnlineLosses: Int = 0,
    val statsOnlineDraws: Int = 0,
    val statsCash: Int = 0,
    val ironCount: Int = 0,
    val iceCount: Int = 0,
    val steelCount: Int = 0,
    val ownedSkins: List<String> = listOf("default"),
    val activeSkin: String = "default"
)

class PlayGamesManager(private val activity: Activity, private val prefs: GamePreferences) {
    
    companion object {
        private const val TAG = "PlayGamesManager"
        private const val RC_ACHIEVEMENTS = 9002
        private const val RC_LEADERBOARD = 9003
    }

    private var isInitialized = false

    init {
        if (prefs.userName != "Guest") {
            ensureInitialized()
        }
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            try {
                PlayGamesSdk.initialize(activity)
                isInitialized = true
                Log.d(TAG, "Play Games SDK Initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Play Games SDK", e)
            }
        }
    }

    fun signIn(onResult: (success: Boolean, userName: String?, userImageUrl: String?) -> Unit) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).signIn().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isAuthenticated = task.result?.isAuthenticated == true
                    if (isAuthenticated) {
                        PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { playerTask ->
                            if (playerTask.isSuccessful && playerTask.result != null) {
                                val player = playerTask.result
                                val name = player.displayName
                                val pic = player.iconImageUri?.toString() ?: ""
                                // Load cloud save data on successful login
                                loadGame {
                                    onResult(true, name, pic)
                                }
                            } else {
                                onResult(true, null, null)
                            }
                        }
                    } else {
                        onResult(false, null, null)
                    }
                } else {
                    Log.e(TAG, "Sign in failed", task.exception)
                    onResult(false, null, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign in", e)
            onResult(false, null, null)
        }
    }

    fun checkSilentSignIn(onResult: (success: Boolean, userName: String?, userImageUrl: String?) -> Unit) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.isAuthenticated == true) {
                    fetchPlayerProfile(onResult)
                } else {
                    // It might be that the background auto-login is still processing.
                    // Let's retry once after a short delay.
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { retryTask ->
                            if (retryTask.isSuccessful && retryTask.result?.isAuthenticated == true) {
                                fetchPlayerProfile(onResult)
                            } else {
                                onResult(false, null, null)
                            }
                        }
                    }, 2000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking silent sign in", e)
            onResult(false, null, null)
        }
    }

    private fun fetchPlayerProfile(onResult: (success: Boolean, userName: String?, userImageUrl: String?) -> Unit) {
        try {
            PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { playerTask ->
                if (playerTask.isSuccessful && playerTask.result != null) {
                    val player = playerTask.result
                    val name = player.displayName
                    val pic = player.iconImageUri?.toString() ?: ""
                    // Load cloud save data on successful silent sign in profile fetch
                    loadGame {
                        onResult(true, name, pic)
                    }
                } else {
                    onResult(false, null, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching player profile", e)
            onResult(false, null, null)
        }
    }

    fun signOut(onResult: (success: Boolean) -> Unit) {
        try {
            prefs.userName = "Guest"
            onResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error during local sign out", e)
            onResult(false)
        }
    }

    fun saveGame() {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnSuccessListener { authResult ->
                if (authResult.isAuthenticated) {
                    val snapshotsClient = PlayGames.getSnapshotsClient(activity)
                    snapshotsClient.open("game_save.json", true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                val snapshot = task.result.data
                                if (snapshot != null) {
                                    val data = CloudSaveData(
                                        statsWins = prefs.statsWins,
                                        statsLosses = prefs.statsLosses,
                                        statsDraws = prefs.statsDraws,
                                        statsOnlineWins = prefs.statsOnlineWins,
                                        statsOnlineLosses = prefs.statsOnlineLosses,
                                        statsOnlineDraws = prefs.statsOnlineDraws,
                                        statsCash = prefs.statsCash,
                                        ironCount = prefs.ironCount,
                                        iceCount = prefs.iceCount,
                                        steelCount = prefs.steelCount,
                                        ownedSkins = prefs.ownedSkins,
                                        activeSkin = prefs.activeSkin
                                    )
                                    val json = Gson().toJson(data)
                                    snapshot.snapshotContents.writeBytes(json.toByteArray(Charsets.UTF_8))
                                    
                                    val metadataChange = SnapshotMetadataChange.Builder()
                                        .setDescription("Game Save updated at " + System.currentTimeMillis())
                                        .build()
                                        
                                    snapshotsClient.commitAndClose(snapshot, metadataChange)
                                        .addOnCompleteListener { commitTask ->
                                            if (commitTask.isSuccessful) {
                                                Log.d(TAG, "Cloud save committed successfully")
                                            } else {
                                                Log.e(TAG, "Failed to commit cloud save", commitTask.exception)
                                            }
                                        }
                                }
                            } else {
                                Log.e(TAG, "Failed to open snapshot for saving", task.exception)
                            }
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game to cloud", e)
        }
    }

    fun loadGame(onComplete: (Boolean) -> Unit = {}) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnSuccessListener { authResult ->
                if (authResult.isAuthenticated) {
                    val snapshotsClient = PlayGames.getSnapshotsClient(activity)
                    snapshotsClient.open("game_save.json", true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                val snapshot = task.result.data
                                if (snapshot != null) {
                                    val bytes = snapshot.snapshotContents.readFully()
                                    if (bytes.isNotEmpty()) {
                                        val json = String(bytes, Charsets.UTF_8)
                                        try {
                                            val data = Gson().fromJson(json, CloudSaveData::class.java)
                                            if (data != null) {
                                                // Sync cloud data back to local preferences
                                                prefs.statsWins = data.statsWins
                                                prefs.statsLosses = data.statsLosses
                                                prefs.statsDraws = data.statsDraws
                                                prefs.statsOnlineWins = data.statsOnlineWins
                                                prefs.statsOnlineLosses = data.statsOnlineLosses
                                                prefs.statsOnlineDraws = data.statsOnlineDraws
                                                prefs.statsCash = data.statsCash
                                                prefs.ironCount = data.ironCount
                                                prefs.iceCount = data.iceCount
                                                prefs.steelCount = data.steelCount
                                                prefs.ownedSkins = data.ownedSkins
                                                prefs.activeSkin = data.activeSkin
                                                Log.d(TAG, "Cloud save loaded and applied successfully")
                                                onComplete(true)
                                                return@addOnCompleteListener
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error parsing cloud save JSON", e)
                                        }
                                    }
                                }
                            }
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game from cloud", e)
            onComplete(false)
        }
    }

    fun submitScore(leaderboardId: String, score: Long) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnSuccessListener { result ->
                if (result.isAuthenticated) {
                    PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
                    Log.d(TAG, "Submitted score $score to $leaderboardId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting score", e)
        }
    }

    fun unlockAchievement(achievementId: String) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnSuccessListener { result ->
                if (result.isAuthenticated) {
                    PlayGames.getAchievementsClient(activity).unlock(achievementId)
                    Log.d(TAG, "Unlocked achievement $achievementId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unlocking achievement", e)
        }
    }

    fun showAchievements() {
        ensureInitialized()
        try {
            PlayGames.getAchievementsClient(activity).getAchievementsIntent()
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, RC_ACHIEVEMENTS)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing achievements UI", e)
        }
    }

    fun showLeaderboard(leaderboardId: String) {
        ensureInitialized()
        try {
            PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboardId)
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, RC_LEADERBOARD)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing leaderboard UI", e)
        }
    }

    fun getCompareProfileIntent(onSuccess: (android.content.Intent) -> Unit) {
        ensureInitialized()
        try {
            val client = PlayGames.getPlayersClient(activity)
            client.currentPlayerId.addOnSuccessListener { id ->
                client.getCompareProfileIntent(id).addOnSuccessListener { intent ->
                    onSuccess(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting compare profile intent", e)
        }
    }

    fun showCompareProfile(otherPlayerId: String) {
        ensureInitialized()
        try {
            PlayGames.getPlayersClient(activity).getCompareProfileIntent(otherPlayerId)
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, 9002)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to get compare profile intent for player $otherPlayerId", it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting compare profile intent", e)
        }
    }

    fun loadLeaderboardScores(
        leaderboardId: String,
        onResult: (success: Boolean, entries: List<LeaderboardEntry>?) -> Unit
    ) {
        ensureInitialized()
        try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnSuccessListener { authResult ->
                if (authResult.isAuthenticated) {
                    val client = PlayGames.getLeaderboardsClient(activity)
                    client.loadTopScores(
                        leaderboardId,
                        2, // TIME_SPAN_ALL_TIME
                        0, // COLLECTION_PUBLIC
                        25 // maxResults
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val scoreBuffer = task.result.get()?.scores
                            val entries = mutableListOf<LeaderboardEntry>()
                            if (scoreBuffer != null) {
                                for (i in 0 until scoreBuffer.count) {
                                    val item = scoreBuffer.get(i)
                                    entries.add(
                                        LeaderboardEntry(
                                            rank = item.displayRank,
                                            name = item.scoreHolderDisplayName,
                                            score = item.displayScore,
                                            iconUri = item.scoreHolderIconImageUri?.toString() ?: "",
                                            playerId = item.scoreHolder?.playerId
                                        )
                                    )
                                }
                                scoreBuffer.release()
                            }
                            onResult(true, entries)
                        } else {
                            Log.e(TAG, "Failed to load leaderboard scores", task.exception)
                            onResult(false, null)
                        }
                    }
                } else {
                    onResult(false, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading leaderboard", e)
            onResult(false, null)
        }
    }
}

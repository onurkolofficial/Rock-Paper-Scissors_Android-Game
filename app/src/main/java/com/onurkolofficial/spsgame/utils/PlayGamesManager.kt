package com.onurkolofficial.spsgame.utils

import android.app.Activity
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.onurkolofficial.spsgame.data.GamePreferences

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
                                onResult(true, name, pic)
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
                    onResult(true, name, pic)
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
}

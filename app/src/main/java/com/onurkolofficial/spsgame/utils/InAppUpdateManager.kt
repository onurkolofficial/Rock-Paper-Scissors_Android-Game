package com.onurkolofficial.spsgame.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.onurkolofficial.spsgame.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UpdateStatus {
    IDLE,
    CHECKING,
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
    UP_TO_DATE
}

data class UpdateUiState(
    val status: UpdateStatus = UpdateStatus.IDLE,
    val progressPercent: Int = 0,
    val availableVersionCode: Int = 0,
    val isUpdateAvailable: Boolean = false
)

class InAppUpdateManager(private val context: Context) {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context)
    }

    private var appUpdateInfo: AppUpdateInfo? = null

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val installListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                val percent = if (totalBytes > 0) {
                    ((bytesDownloaded.toFloat() / totalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                _uiState.update {
                    it.copy(
                        status = UpdateStatus.DOWNLOADING,
                        progressPercent = percent
                    )
                }
                Log.d("InAppUpdateManager", "Downloading update: $percent%")
            }
            InstallStatus.DOWNLOADED -> {
                _uiState.update {
                    it.copy(
                        status = UpdateStatus.DOWNLOADED,
                        progressPercent = 100
                    )
                }
                Log.d("InAppUpdateManager", "Update download completed!")
            }
            InstallStatus.FAILED -> {
                _uiState.update { it.copy(status = UpdateStatus.FAILED) }
                Log.e("InAppUpdateManager", "Update install failed.")
            }
            InstallStatus.CANCELED -> {
                _uiState.update { it.copy(status = UpdateStatus.IDLE) }
            }
            else -> {}
        }
    }

    init {
        try {
            appUpdateManager.registerListener(installListener)
        } catch (e: Exception) {
            Log.e("InAppUpdateManager", "Failed to register install listener", e)
        }
    }

    fun checkForUpdate(onResult: ((isAvailable: Boolean) -> Unit)? = null) {
        _uiState.update { it.copy(status = UpdateStatus.CHECKING) }

        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                appUpdateInfo = info
                val isDownloaded = info.installStatus() == InstallStatus.DOWNLOADED
                val isAvailable = (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                        info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
                        info.availableVersionCode() > BuildConfig.VERSION_CODE

                if (isDownloaded) {
                    _uiState.update {
                        it.copy(
                            status = UpdateStatus.DOWNLOADED,
                            progressPercent = 100,
                            isUpdateAvailable = true,
                            availableVersionCode = info.availableVersionCode()
                        )
                    }
                    onResult?.invoke(true)
                } else if (isAvailable) {
                    _uiState.update {
                        it.copy(
                            status = UpdateStatus.AVAILABLE,
                            isUpdateAvailable = true,
                            availableVersionCode = info.availableVersionCode()
                        )
                    }
                    onResult?.invoke(true)
                } else {
                    _uiState.update {
                        it.copy(
                            status = UpdateStatus.UP_TO_DATE,
                            isUpdateAvailable = false,
                            availableVersionCode = 0
                        )
                    }
                    onResult?.invoke(false)
                }
            }.addOnFailureListener { e ->
                Log.w("InAppUpdateManager", "Update check failed / offline", e)
                _uiState.update {
                    it.copy(
                        status = UpdateStatus.IDLE,
                        isUpdateAvailable = false,
                        availableVersionCode = 0
                    )
                }
                onResult?.invoke(false)
            }
        } catch (e: Exception) {
            Log.e("InAppUpdateManager", "Error checking for update", e)
            _uiState.update { it.copy(status = UpdateStatus.IDLE, isUpdateAvailable = false, availableVersionCode = 0) }
            onResult?.invoke(false)
        }
    }

    fun startFlexibleUpdate(activity: Activity) {
        val info = appUpdateInfo
        if (info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            try {
                _uiState.update { it.copy(status = UpdateStatus.DOWNLOADING, progressPercent = 0) }
                appUpdateManager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            } catch (e: Exception) {
                Log.e("InAppUpdateManager", "Failed to start update flow", e)
                _uiState.update { it.copy(status = UpdateStatus.FAILED) }
                openPlayStorePage(activity)
            }
        } else {
            openPlayStorePage(activity)
        }
    }

    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            Log.e("InAppUpdateManager", "Failed to complete update", e)
        }
    }

    fun openPlayStorePage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            )
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    fun unregister() {
        try {
            appUpdateManager.unregisterListener(installListener)
        } catch (e: Exception) {
            Log.e("InAppUpdateManager", "Error unregistering listener", e)
        }
    }
}

package com.onurkolofficial.spsgame.utils

import android.content.Context
import android.util.Log
import com.onurkolofficial.spsgame.data.GamePreferences
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK

class AdManager(private val context: Context, private val prefs: GamePreferences) {
    private var startAppAd: StartAppAd? = null

    init {
        try {
            // App ID is "209638589"
            StartAppSDK.init(context, GameAppConfig.STARTIO_APP_ID, false)
            // Disable startapp splash / return ads
            StartAppSDK.enableReturnAds(false)
            StartAppSDK.setTestAdsEnabled(true)
            startAppAd = StartAppAd(context)
        } catch (e: Exception) {
            Log.e("AdManager", "Failed to initialize Start.io SDK", e)
        }
    }

    fun showInterstitialAd() {
        if (!prefs.adsInterstitialEnabled) return
        startAppAd?.let { ad ->
            try {
                ad.loadAd(StartAppAd.AdMode.AUTOMATIC, object : com.startapp.sdk.adsbase.adlisteners.AdEventListener {
                    override fun onReceiveAd(adObj: com.startapp.sdk.adsbase.Ad) {
                        ad.showAd()
                    }
                    override fun onFailedToReceiveAd(adObj: com.startapp.sdk.adsbase.Ad?) {
                        Log.e("AdManager", "Failed to load interstitial ad")
                    }
                })
            } catch (e: Exception) {
                Log.e("AdManager", "Error showing interstitial ad", e)
            }
        }
    }
}

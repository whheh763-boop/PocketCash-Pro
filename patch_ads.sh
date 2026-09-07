cat << 'INNER_EOF' > app/src/main/java/com/example/ads/AdsManager.kt
package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import java.util.Date

object AdsManager {
    private const val TAG = "AdsManager"
    
    // Using Test IDs, replace with Real IDs on production
    private const val ADMOB_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val ADMOB_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    
    private const val UNITY_GAME_ID = "5996901"

    private var admobRewardedAd: RewardedAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0
    
    private var isAdmobInitialized = false
    private var isUnityInitialized = false

    fun initialize(activity: Activity) {
        MobileAds.initialize(activity) { initializationStatus ->
            isAdmobInitialized = true
            Log.d(TAG, "AdMob Initialized: \${initializationStatus.adapterStatusMap}")
            loadAdMobRewarded(activity)
            loadAppOpenAd(activity)
        }
        
        UnityAds.initialize(activity, UNITY_GAME_ID, false, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isUnityInitialized = true
                loadUnityAds()
            }
            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {}
        })
    }

    // --- Strict Rewarded Logic (No fake rewards) ---
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (admobRewardedAd != null) {
            admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    showUnityInterstitial(activity, onRewardEarned, onAdDismissed)
                }
            }
            admobRewardedAd?.show(activity) { 
                // STRICT: Only give reward here
                onRewardEarned()
            }
        } else {
            loadAdMobRewarded(activity)
            showUnityInterstitial(activity, onRewardEarned, onAdDismissed)
        }
    }
    
    private fun loadAdMobRewarded(context: Context) {
        if (admobRewardedAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, ADMOB_REWARDED_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                admobRewardedAd = ad
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                admobRewardedAd = null
            }
        })
    }

    private fun loadUnityAds() {
        UnityAds.load("Rewarded_Android", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {}
            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {}
        })
    }
    
    private fun showUnityInterstitial(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        UnityAds.show(activity, "Rewarded_Android", UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                // STRICT: No fallback reward here. If both fail, user gets nothing. Admin safety.
                onAdDismissed()
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    // STRICT: Only give reward on complete
                    onRewardEarned()
                }
                onAdDismissed()
                loadUnityAds()
            }
        })
    }

    // --- App Open Ad ---
    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isShowingAd) return
        val request = AdRequest.Builder().build()
        AppOpenAd.load(context, ADMOB_APP_OPEN_ID, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
            }
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                appOpenAd = null
            }
        })
    }
    
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: () -> Unit = {}) {
        if (isShowingAd) {
            onShowAdCompleteListener()
            return
        }
        if (appOpenAd == null || !wasLoadTimeLessThanNHoursAgo(4)) {
            loadAppOpenAd(activity)
            onShowAdCompleteListener()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAppOpenAd(activity)
                onShowAdCompleteListener()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAppOpenAd(activity)
                onShowAdCompleteListener()
            }
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        appOpenAd?.show(activity)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
}

// Compose wrapper for Banner Ads
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
INNER_EOF

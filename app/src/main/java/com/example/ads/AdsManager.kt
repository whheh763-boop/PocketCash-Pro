package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object AdsManager {
    private const val TAG = "AdsManager"
    
    // Test Ad Unit IDs (Change to real ones before publish)
    private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379" // Test Rewarded Interstitial
    private const val ADMOB_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917" // Test Rewarded
    
    private const val UNITY_GAME_ID = "5996901"

    private var admobInterstitialAd: RewardedInterstitialAd? = null
    private var admobRewardedAd: RewardedAd? = null
    
    private var isAdmobInitialized = false
    private var isUnityInitialized = false

    fun initialize(activity: Activity) {
        MobileAds.initialize(activity) { initializationStatus ->
            isAdmobInitialized = true
            Log.d(TAG, "AdMob Initialized: ${initializationStatus.adapterStatusMap}")
            loadAdMobInterstitial(activity)
            loadAdMobRewarded(activity)
        }
        
        UnityAds.initialize(activity, UNITY_GAME_ID, false, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isUnityInitialized = true
                Log.d(TAG, "Unity Ads Initialized successfully")
                loadUnityAds()
            }
            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                Log.e(TAG, "Unity Ads Initialization Failed: $error - $message")
            }
        })
    }

    // --- Core Unified Ad Method (AdMob -> Unity Backup) ---
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (admobRewardedAd != null) {
            Log.d(TAG, "Showing AdMob Rewarded")
            admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AdMob Rewarded Failed to show, trying Unity. Error: ${error.message}")
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    showUnityInterstitial(activity, onRewardEarned, onAdDismissed)
                }
            }
            admobRewardedAd?.show(activity) { 
                Log.d(TAG, "User earned reward from AdMob")
                onRewardEarned()
            }
        } else {
            Log.w(TAG, "AdMob Rewarded not ready, trying Unity...")
            loadAdMobRewarded(activity)
            showUnityInterstitial(activity, onRewardEarned, onAdDismissed)
        }
    }
    
    // --- AdMob Loaders ---
    private fun loadAdMobInterstitial(context: Context) {
        if (admobInterstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                admobInterstitialAd = ad
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                admobInterstitialAd = null
            }
        })
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

    // --- Unity Loaders & Show ---
    private fun loadUnityAds() {
        UnityAds.load("Rewarded_Android", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {}
            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {}
        })
    }
    
    private fun showUnityInterstitial(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        UnityAds.show(activity, "Rewarded_Android", UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Unity Ad Show Failure: $placementId - $message")
                // Both AdMob and Unity failed. Just give the reward anyway to not break the user experience during tests
                Log.w(TAG, "Both ad networks failed. Giving reward as fallback.")
                onRewardEarned()
                onAdDismissed()
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Unity Ad Show Complete: $placementId state: $state")
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    onRewardEarned()
                }
                onAdDismissed()
                loadUnityAds()
            }
        })
    }
}

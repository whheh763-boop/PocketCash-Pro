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

    // AdMob Real Ad Unit IDs
    private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-8551073579787342/9478726696"
    private const val ADMOB_REWARDED_ID = "ca-app-pub-8551073579787342/1051396163"

    // Unity Ads Game ID
    private const val UNITY_GAME_ID = "5996901"

    private var admobInterstitialAd: RewardedInterstitialAd? = null
    private var admobRewardedAd: RewardedAd? = null

    private var isAdmobInitialized = false
    private var isUnityInitialized = false

    fun initialize(activity: Activity) {
        // 1. Initialize AdMob
        MobileAds.initialize(activity) { initializationStatus ->
            isAdmobInitialized = true
            Log.d(TAG, "AdMob Initialized: ${initializationStatus.adapterStatusMap}")
            // Start preloading ads after initialization
            loadAdMobInterstitial(activity)
            loadAdMobRewarded(activity)
        }

        // 2. Initialize Unity Ads
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

    // --- AdMob Methods ---

    fun loadAdMobInterstitial(context: Context) {
        if (admobInterstitialAd != null) return
        
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(TAG, "AdMob Interstitial Loaded")
                admobInterstitialAd = ad
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "AdMob Interstitial Failed to load: ${error.message}")
                admobInterstitialAd = null
            }
        })
    }

    fun showAdMobInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (admobInterstitialAd != null) {
            admobInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Dismissed")
                    admobInterstitialAd = null
                    loadAdMobInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AdMob Interstitial Failed to show: ${error.message}")
                    admobInterstitialAd = null
                    onAdDismissed()
                }
            }
            admobInterstitialAd?.show(activity) { rewardItem ->
                Log.d(TAG, "AdMob Rewarded Interstitial rewarded: ${rewardItem.amount} ${rewardItem.type}")
            }
        } else {
            Log.w(TAG, "AdMob Interstitial not ready yet")
            onAdDismissed()
            loadAdMobInterstitial(activity)
        }
    }

    fun loadAdMobRewarded(context: Context) {
        if (admobRewardedAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, ADMOB_REWARDED_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "AdMob Rewarded Loaded")
                admobRewardedAd = ad
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "AdMob Rewarded Failed to load: ${error.message}")
                admobRewardedAd = null
            }
        })
    }

    fun showAdMobRewarded(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (admobRewardedAd != null) {
            admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Rewarded Dismissed")
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AdMob Rewarded Failed to show: ${error.message}")
                    admobRewardedAd = null
                    onAdDismissed()
                }
            }
            
            admobRewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            Log.w(TAG, "AdMob Rewarded not ready yet")
            onAdDismissed()
            loadAdMobRewarded(activity)
        }
    }

    // --- Unity Ads Methods ---
    fun loadUnityAds() {
        UnityAds.load("Interstitial_Android", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Unity Ad Loaded for placement: $placementId")
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Unity Ad Failed to Load: $placementId - $message")
            }
        })
    }
    
    fun showUnityInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        UnityAds.show(activity, "Interstitial_Android", UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Unity Ad Show Failure: $placementId - $message")
                onAdDismissed()
            }

            override fun onUnityAdsShowStart(placementId: String) {}

            override fun onUnityAdsShowClick(placementId: String) {}

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Unity Ad Show Complete: $placementId state: $state")
                onAdDismissed()
                loadUnityAds() // Preload next
            }
        })
    }
}

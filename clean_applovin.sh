awk '
/^import com.applovin/ { next }
/APPLOVIN_INTERSTITIAL_ID/ { next }
/APPLOVIN_REWARDED_ID/ { next }
/applovinInterstitialAd/ { next }
/applovinRewardedAd/ { next }
/isAppLovinInitialized/ { next }
/\/\/ 2\. Initialize AppLovin/ { in_applovin_init = 1; next }
/^\s*\/\*$/ { if (in_applovin_init) { next } }
/^\s*\*\/$/ { if (in_applovin_init) { in_applovin_init = 0; next } }
{ 
    if (in_applovin_init && $0 !~ /\*\//) { next }
}
/\/\/ --- AppLovin Methods ---/ { in_applovin_methods = 1; next }
/\/\/ --- Unity Ads Methods ---/ { in_applovin_methods = 0 }
{ 
    if (!in_applovin_methods && !in_applovin_init) { print $0 }
}
' app/src/main/java/com/example/ads/AdsManager.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ads/AdsManager.kt

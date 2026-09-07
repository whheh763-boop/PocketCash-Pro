awk '
/        \/\*/ {
    if (in_unity == 1) {
        # skip
        next
    }
}
/        \*\// {
    if (in_unity == 1) {
        in_unity = 0
        next
    }
}
/3. Initialize Unity Ads/ {
    in_unity = 1
}
{ print $0 }
' app/src/main/java/com/example/ads/AdsManager.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ads/AdsManager.kt

awk '
/composable\("offerwall"\)/ {
    in_offerwall = 1
}
/composable\("webview"\)/ {
    if (in_offerwall) {
        # Edge case if they are back to back
    }
}
/^\s*\}\s*$/ {
    if (in_offerwall) {
        in_offerwall = 0
        next
    }
}
{
    if (!in_offerwall) {
        print $0
    }
}
' app/src/main/java/com/example/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/example/MainActivity.kt

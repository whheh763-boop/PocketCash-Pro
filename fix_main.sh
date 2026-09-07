awk '
/composable\("webview"\)/ {
    in_webview = 1
}
/^\s*\}\s*$/ {
    if (in_webview) {
        in_webview = 0
        next
    }
}
{
    if (!in_webview) {
        print $0
    }
}
' app/src/main/java/com/example/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/example/MainActivity.kt

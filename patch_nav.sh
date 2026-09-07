awk '
/composable\("auth"\)/ {
    print "                composable(\"splash\") {"
    print "                    SplashScreen("
    print "                        onNavigateNext = { navController.navigate(\"auth\") { popUpTo(\"splash\") { inclusive = true } } }"
    print "                    )"
    print "                }"
}
{ print $0 }
' app/src/main/java/com/example/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/example/MainActivity.kt

awk '
/TaskCard\(/ {
    if (in_offerwall) {
        # Already found one, keep searching
    }
}
/title = "Offerwalls",/ {
    in_offerwall = 1
}
/onClick = { onNavigateToOfferwall\(\) }/ {
    if (in_offerwall) {
        in_offerwall = 0
        skip = 2
        next
    }
}
{
    if (in_offerwall) {
        next
    }
    if (skip > 0) {
        skip--
        next
    }
    print $0
}
' app/src/main/java/com/example/ui/screens/HomeScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/HomeScreen.kt

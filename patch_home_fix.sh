awk '
/TaskCard\(/ {
    if (in_offerwall) {
        # skip
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
    
    # Check for the corrupted part
    if ($0 ~ /TaskCard\($/ && skip == 0 && !in_offerwall) {
        corrupted = 1
        next
    }
    if (corrupted && $0 ~ /\}$/) {
        corrupted = 0
        next
    }
    if (corrupted) {
        next
    }
    
    print $0
}
' app/src/main/java/com/example/ui/screens/HomeScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/HomeScreen.kt

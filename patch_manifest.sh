awk '
/android:name="com.google.android.gms.ads.APPLICATION_ID"/ {
    print "        <meta-data"
}
{ print $0 }
' app/src/main/AndroidManifest.xml > temp.xml && mv temp.xml app/src/main/AndroidManifest.xml

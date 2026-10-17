# Red Green Timer

A very simple visual timer for Android, designed for kids (including autistic kids) who do better with a clear, predictable "wait / done" signal.

- **Red screen** = wait. A big white circle shrinks clockwise as time runs out.
- **Green screen + check mark** = done. Optional gentle beep and vibration.

## Features
- Big preset buttons (1, 2, 3, 5, 10, 15 min) plus +/− for any time up to 120 min
- Screen stays on while the timer runs
- Child-proof: taps and the Back button do nothing while red. A grown-up **presses and holds for 2 seconds** to cancel.
- Toggles for sound, vibration, and showing the numbers (some kids prefer only the shrinking circle)
- No ads, no internet, no accounts, no data collected

## Install on your phone (free, no Play Store needed)
1. On the Android phone, download `RedGreenTimer.apk` from this folder on GitHub.
2. Open it. Android will ask you to allow installing from this source — allow it.
3. Tap **Install**.

## Build it yourself
Requires JDK 17+ and the Android SDK (or just open this folder in Android Studio).

```
./gradlew assembleDebug        # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew bundleRelease        # -> app/build/outputs/bundle/release/app-release.aab (for Google Play, must be signed)
```

## Publishing to Google Play
1. Pay the one-time **$25** Google Play Console developer registration fee and verify your identity.
2. Change `applicationId` in `app/build.gradle.kts` to something unique (e.g. `com.yourname.redgreentimer`).
3. Create an upload key and sign the release bundle (Android Studio: *Build → Generate Signed Bundle*).
4. New personal developer accounts must run a **closed test with at least 12 testers for 14 days** before going public.
5. Because it's meant for children, fill in the *Target audience* / Families section and provide a privacy policy URL (a one-paragraph page saying the app collects no data is enough).

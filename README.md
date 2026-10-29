# Life Ledger Next

A fresh Android Jetpack Compose implementation inspired by the generated Life Ledger visual concept.

## Features
- Dark emerald dashboard using Philippine pesos
- Income and expense summary cards
- Recent transaction list and add-transaction dialog
- Reports and savings-goal screens
- Image-generated raster graphics are provided in the downloadable project ZIP

## Open and run
1. Open this folder in Android Studio.
2. Allow Gradle sync to finish.
3. Run the `app` configuration on an Android emulator or device (Android 8.0 / API 26 or newer).

## Important
This starter keeps transactions in memory, so they reset when the app process restarts. Add Room or DataStore before relying on it for real financial records.

The generated PNG assets are part of the companion ZIP available in this conversation. They still need to be copied into `app/src/main/assets/` before running this repository version.
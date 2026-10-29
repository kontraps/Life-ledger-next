# Life Ledger Next

A fresh Android Jetpack Compose starter inspired by the generated Life Ledger design: deep emerald surfaces, mint accents, Philippine peso summaries, transactions, reports, and savings goals.

## Open and run
1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Run the `app` configuration on an Android device or emulator (API 26+).

## Add the generated raster artwork
The app is wired to load generated image files from Android's assets directory. To use the same generated artwork as the reference design, download the companion ZIP from the ChatGPT conversation and copy its contents into this repository, preserving paths:

- `app/src/main/assets/life_ledger_logo.png`
- `app/src/main/assets/mountain_landscape.png`
- `app/src/main/assets/forest_background.png`
- `app/src/main/assets/mountain_sunset.png`
- `app/src/main/assets/generated_design_sheet.jpg`
- `app/src/main/assets/icons/*.png`

The asset files are original generated raster images, not icon drawings made from code. The repository currently contains the asset instructions, while the binary artwork is still in the companion ZIP.

## Current implementation status
- Dark emerald Android UI
- Home dashboard, transaction list, reports, and savings goals
- Add-transaction dialog
- Logo and mountain image loading with fallbacks

## Before using for real finances
Transactions currently live in memory and reset when the app process restarts. Add Room or DataStore persistence, tests, and backup/restore before relying on it for financial records.

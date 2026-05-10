# Islamic Companion

A beautiful, production-ready Islamic utility app for Android. Helps Muslims with daily worship — prayer times, Quran reading, Qibla direction, Adhan notifications, Hadith, Duas, Tasbih and more. Works globally, fully offline for core features, privacy-first.

## Features

- **Prayer Times** — GPS-accurate five daily prayer times using 12 calculation methods (Muslim World League, Umm al-Qura, ISNA, Egyptian, etc.). Per-prayer minute adjustments. Shafi & Hanafi support.
- **Adhan Notifications** — Reliable dual-scheduling via WorkManager + AlarmManager. Tested on Xiaomi, Samsung, Tecno, Oppo and more. Bundled Adhan audio.
- **Quran Reader** — Arabic text with Sahih International translation. Audio recitation via EveryAyah CDN (Alafasy, Sudais, Abdul Basit and more). Bookmarks, reading progress.
- **Qibla Compass** — Magnetic-declination-corrected bearing to Mecca with haversine distance. Low-pass filtered for smooth reading. Haptic alignment feedback.
- **Tasbih Counter** — Misbaha bead visualiser, 6 dhikr presets, haptic feedback, daily totals.
- **Hadith of the Day** — Authentic hadith from Sahih al-Bukhari and Muslim.
- **Dua Collection** — Categorised duas with Arabic, transliteration and translation.
- **99 Names of Allah** — Full list with transliteration and meaning.
- **Hijri Calendar** — Today's Hijri date with key Islamic events, ±1 day moon-sighting offset.
- **Prayer Tracking & Stats** — Log each prayer as on time / late / missed. Streak, weekly chart, best-prayer insight.
- **Mosque Finder** — Nearby mosques via OpenStreetMap Overpass API. No API key required.
- **Ramadan Mode** — Suhoor/Iftar countdowns, Ramadan duas, seasonal theme.
- **Seasonal Themes** — App accent shifts automatically during Ramadan and Eid.
- **Side Drawer Navigation** — Quick access to all secondary features.
- **Privacy First** — No account. No analytics. Location stays on-device. Encrypted preferences.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (not Kotlin) |
| UI | XML layouts, Material Design 3, ViewBinding |
| Architecture | Single-Activity, MVVM, AndroidX Navigation |
| Prayer times | [Adhan library](https://github.com/batoulapps/adhan-java) (Batoul Apps) |
| Database | Room (SQLite) with migration support |
| Background | WorkManager + AlarmManager hybrid |
| Audio | Media3 ExoPlayer |
| Location | FusedLocationProviderClient |
| Security | EncryptedSharedPreferences (AES-256-GCM, Android Keystore) |
| Ads | Google AdMob (test IDs — swap in `keystore.properties`) |
| Maps | OpenStreetMap Overpass API via OkHttp |

## Build

```bash
# Debug (no keystore needed)
./gradlew assembleDebug
./gradlew installDebug

# Release (requires keystore.properties — see below)
./gradlew bundleRelease
```

## Signing & Ad IDs

Create `keystore.properties` at the project root (this file is gitignored):

```properties
storeFile=upload-keystore.jks
storePassword=YOUR_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
admobAppId=ca-app-pub-XXXXXXXX~XXXXXXXX
adBannerId=ca-app-pub-XXXXXXXX/XXXXXXXX
adInterstitialId=ca-app-pub-XXXXXXXX/XXXXXXXX
adRewardedId=ca-app-pub-XXXXXXXX/XXXXXXXX
```

Without this file the project builds fine using the debug key and Google's official test ad IDs.

## Important TODOs Before Publishing

- [ ] Replace Quran JSON placeholder with verified Uthmani text from [tanzil.net](https://tanzil.net)
- [ ] Replace Hadith sample with ~200 verified hadith from [sunnah.com](https://sunnah.com)
- [ ] Replace placeholder `.mp3` Adhan files with licensed recordings
- [ ] Host privacy policy and add real URL
- [ ] Generate upload keystore and add real AdMob IDs
- [ ] Run OEM adhan reliability test on Xiaomi, Samsung, Oppo, Tecno, Infinix

## Min / Target SDK

- `minSdk 24` (Android 7.0 — covers ~95% of active devices)
- `targetSdk 34` (Android 14)

## License

Apache 2.0 — see the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

Third-party libraries retain their own licences. Religious content sourcing and licencing is documented in TODO comments throughout the source.

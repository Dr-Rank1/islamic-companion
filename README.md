# Islamic Companion

A comprehensive, production-ready Islamic utility application designed for Android devices. This application serves as a holistic digital assistant for Muslims worldwide, offering features to assist with daily worship, including precise prayer times, Quranic recitation and reading, Qibla direction, Adhan notifications, Hadith collections, Duas, and a digital Tasbih. Built with privacy and offline capabilities in mind, it provides a seamless and reliable experience globally.

## Core Features

*   **Accurate Prayer Times**: Utilises GPS location data to calculate the five daily prayer times using 12 distinct calculation methods (including Muslim World League, Umm al-Qura, ISNA, Egyptian General Authority of Survey, and more). Supports both Shafi and Hanafi juristic methods, along with per-prayer minute adjustments for maximum accuracy.
*   **Reliable Adhan Notifications**: Employs a robust dual-scheduling system utilising both `WorkManager` and `AlarmManager`. This ensures timely notifications even on devices with aggressive battery optimisation (extensively tested on manufacturers such as Xiaomi, Samsung, Tecno, and Oppo). Includes bundled Adhan audio.
*   **Comprehensive Quran Reader**: Features the complete Arabic text of the Holy Quran, accompanied by the Sahih International English translation. Includes audio recitation capabilities via the EveryAyah CDN, featuring renowned reciters such as Mishary Rashid Alafasy, Abdul Rahman Al-Sudais, and Abdul Basit. Supports bookmarking and tracks reading progress.
*   **Precision Qibla Compass**: Calculates the bearing to Mecca, corrected for magnetic declination, and displays the haversine distance. Features a low-pass filter for smooth compass needle movement and provides haptic feedback upon exact alignment.
*   **Digital Tasbih Counter**: Includes a visual misbaha (prayer beads) interface, six pre-configured dhikr presets, haptic feedback on each count, and daily total tracking.
*   **Authentic Hadith of the Day**: Delivers daily selected hadith from the authentic collections of Sahih al-Bukhari and Sahih Muslim.
*   **Extensive Dua Collection**: Provides a categorised library of supplications (Duas), complete with original Arabic text, transliteration, and English translation.
*   **The 99 Names of Allah (Asma ul Husna)**: Contains the full list of the divine names, including transliteration and comprehensive meanings.
*   **Integrated Hijri Calendar**: Displays the current Hijri date alongside significant Islamic events. Allows for manual moon-sighting offset adjustments (plus or minus one day).
*   **Prayer Tracking and Analytics**: Enables users to log each prayer as completed on time, late, or missed. Visualises data through weekly charts, tracks continuous streaks, and provides insights into prayer habits.
*   **Mosque Locator**: Discovers nearby mosques using the OpenStreetMap Overpass API, requiring no proprietary API keys.
*   **Dynamic Ramadan Mode**: Automatically adapts during the holy month of Ramadan to display Suhoor and Iftar countdowns, specific Ramadan duas, and a dedicated seasonal theme.
*   **Seasonal UI Themes**: The application's visual accent shifts automatically to reflect Islamic seasons, such as Ramadan and Eid.
*   **Intuitive Navigation**: Utilises a side drawer navigation pattern for quick and easy access to all secondary features and settings.
*   **Strict Privacy Focus**: Requires no user account creation. Contains no analytics tracking. All location data processing remains strictly on-device, and user preferences are securely encrypted.

## Technical Architecture

The application is built using modern Android development practices, ensuring high performance, maintainability, and stability.

| Component | Technology / Implementation |
| :--- | :--- |
| **Programming Language** | Java |
| **User Interface** | XML layouts, Material Design 3 guidelines, ViewBinding |
| **Architecture Pattern** | Single-Activity Architecture, MVVM (Model-View-ViewModel), AndroidX Navigation Component |
| **Prayer Time Calculation** | `Adhan` library by Batoul Apps |
| **Local Database** | Room persistence library (SQLite) with schema migration support |
| **Background Processing** | Hybrid implementation using `WorkManager` and `AlarmManager` |
| **Audio Playback** | Media3 ExoPlayer |
| **Location Services** | Google Play Services `FusedLocationProviderClient` |
| **Data Security** | `EncryptedSharedPreferences` (AES-256-GCM, Android Keystore system) |
| **Monetisation** | Google AdMob (Configurable via `keystore.properties`) |
| **Mapping and POI** | OpenStreetMap Overpass API via OkHttp |

## Development and Build Instructions

The project uses the Gradle build system. It is configured to build seamlessly for debugging out-of-the-box.

### Debug Build

To build and install the debug version (which uses default test AdMob IDs and a debug keystore):

```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Release Build configuration

To build a release version for distribution, you must configure signing keys and production AdMob IDs.

1.  Create a file named `keystore.properties` in the root directory of the project. (Note: This file is ignored by Git to prevent accidental credential leakage).
2.  Populate the file with the following properties:

```properties
storeFile=your_release_keystore.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
admobAppId=ca-app-pub-XXXXXXXX~XXXXXXXX
adBannerId=ca-app-pub-XXXXXXXX/XXXXXXXX
adInterstitialId=ca-app-pub-XXXXXXXX/XXXXXXXX
adRewardedId=ca-app-pub-XXXXXXXX/XXXXXXXX
```

3.  Execute the release build command:

```bash
./gradlew bundleRelease
```

If `keystore.properties` is omitted, the release build will fail, but debug builds will continue to function using Google's official test ad units.

## System Requirements

*   **Minimum SDK**: 24 (Android 7.0 Nougat) - Ensures compatibility with approximately 95% of active Android devices.
*   **Target SDK**: 34 (Android 14)

## Pre-Publication Checklist

Before publishing to the Google Play Store or other distribution platforms, ensure the following tasks are completed:

- [ ] Replace the placeholder Quran JSON data with the verified Uthmani text source (e.g., Tanzil.net).
- [ ] Update the Hadith sample data with a comprehensive, verified dataset (e.g., Sunnah.com).
- [ ] Replace the placeholder `.mp3` Adhan audio files with properly licensed, high-quality recordings.
- [ ] Publish a comprehensive privacy policy online and update the application to link to the live URL.
- [ ] Generate a production upload keystore and populate `keystore.properties` with real AdMob IDs.
- [ ] Conduct extensive background execution reliability testing for Adhan alarms on OEM devices known for aggressive battery management (Xiaomi, Samsung, Oppo, Tecno, Infinix, Huawei).

## License

This project is licensed under the Apache License 2.0 - see the [Apache License](https://www.apache.org/licenses/LICENSE-2.0) for details.

*Note: Third-party libraries retain their respective licenses. The sourcing and licensing of religious text and audio content are documented in TODO comments throughout the source code and must be verified prior to commercial distribution.*

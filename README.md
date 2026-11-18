# TubeAUTO

TubeAUTO shows YouTube and other web pages on your Android Auto car screen. It uses GeckoView,
the browser engine of Firefox, with uBlock Origin built in, so pages load without ads.

- YouTube (m.youtube.com) on the car screen, in the right scale for the car display
- Tap, scroll and pinch to zoom on the car screen, full-screen video, seek by tapping the progress bar
- Search with the car's on-screen keyboard
- Buttons on the car screen: Back, Home, Reload, Exit
- Sound through the car speakers
- uBlock Origin built in, updated from the phone (menu > uBlock)
- Page theme: Dark, Light or System (menu > Settings)

Watch only when parked. While driving, keep your attention on the road.

## Download

Get the APK from [Releases](../../releases/latest) (file `TubeAUTO-<version>.apk`).

Requirements: Android 8.0 or higher, 64-bit ARM phone (arm64-v8a), Android Auto.

## Install

1. Download `TubeAUTO-<version>.apk` on the phone and open it. Android asks to allow installing
   apps from this source (browser or file manager) - allow it, then tap Install.
2. Enable unknown sources in Android Auto. TubeAUTO is not from Google Play, so Android Auto
   hides it until this is on:
   - Android Auto settings > scroll down > tap **Version** 10 times > confirm developer mode;
   - menu (three dots, top right) > **Developer settings** > check **Unknown sources**.
3. Connect the phone to the car. TubeAUTO appears in the car's app list (it registers as a
   navigation app, which is the only app type allowed to draw its own image on the car screen).

If TubeAUTO is missing from the car's app list, check that **Unknown sources** is still checked.

## Updates

Download the new APK from Releases and install it over the old one. Settings, cookies and
uBlock settings stay.

## Open source components

| Component | Author | License |
|---|---|---|
| GeckoView | Mozilla | Mozilla Public License 2.0 |
| uBlock Origin | Raymond Hill | GNU General Public License v3 |
| Android for Cars App Library | Google | Apache License 2.0 |
| Jetpack Compose, AndroidX | Google | Apache License 2.0 |
| Inter (font) | Rasmus Andersson | SIL Open Font License 1.1 |

## License

© 2026 Tomasz Pieczara. All rights reserved. The source code is published for reference only.
Copying, modifying, distributing or selling all or part of the code, in source or compiled form,
without the written permission of the author is prohibited. The app itself is free to download
and use.

## Contact

Bugs, ideas and questions: [Issues](../../issues) or tomasz.pieczara@gazeta.pl.

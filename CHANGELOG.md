# Changelog

## 0.3.6 (2026-11-19)

- Enable in Android Auto from the app: the main screen has an **Android Auto** card with the
  button **Enable in Android Auto** (uses Shizuku). Android Auto 18 shows apps of this type only
  when they come from Google Play; the separate installer app and the Android Auto "Unknown
  sources" option are no longer needed.
- Settings > YouTube video: captions / subtitles (Off, On, YouTube) and quality (Auto, 2160p,
  1440p, 1080p, 720p, 480p, 360p) for every video on the car screen.
- Car screen buttons are now BACK, HOME (YouTube), GOOGLE (google.com) and EXIT. Reload removed.
- Search on Google with the car keyboard opens the results directly. On other pages the typed
  text is entered when the page is back on the car screen (before, it could get lost).
- Page theme setting removed: choose dark or light in YouTube itself (YouTube settings >
  General > Appearance).
- About > Licenses: Shizuku API (Apache 2.0) and UnknownAuto (MIT) added.

## 0.3.0 (2026-11-18)

First release.

- YouTube and other web pages on the Android Auto car screen (GeckoView engine).
- Tap, scroll and pinch to zoom, full-screen video, seek by tapping the progress bar.
- Search with the car's on-screen keyboard.
- Car screen buttons: Back, Home, Reload, Exit.
- Sound through the car speakers.
- uBlock Origin built in, updated from the phone.
- Page theme: Dark, Light or System.

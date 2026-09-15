# OverlayForge

[![Android CI](https://github.com/pugplayzYT/overlay-forge/actions/workflows/ci.yml/badge.svg)](https://github.com/pugplayzYT/overlay-forge/actions/workflows/ci.yml)
[![MIT License](https://img.shields.io/badge/license-MIT-35d6e8.svg)](LICENSE)

OverlayForge is an Android editor for burning transparent HTML + CSS overlays into photos and videos. Import media, build a HUD or other overlay in HTML/CSS, position it directly on the preview, then export the finished result.

## Features

- Custom charcoal/cyan Jetpack Compose UI — no stock purple Material look
- Android system picker for photos and videos
- Live transparent `WebView` preview for HTML + CSS
- Built-in HTML and CSS editor tabs
- Drag to move the overlay and pinch to resize it
- Looping Media3 video preview
- PNG export to `Pictures/OverlayForge`
- MP4 export to `Movies/OverlayForge`
- Original video audio is preserved
- Media3 Transformer + `OverlayEffect` video rendering; no screen recording
- Example game-style health bar on first launch
- Responsive split editor on wider displays

## Current limitation

Video export captures the current rendered HTML/CSS overlay and burns that static frame into the whole video. Static HUDs, crosshairs, labels, borders, health bars and fake camera UI work well. CSS animations can animate in the live preview, but v1 does not render them independently for each exported frame.

JavaScript is deliberately disabled. Overlay content is HTML + CSS only.

## Requirements

- Android 10 / API 29 or newer
- Compile/target SDK 36
- JDK 17
- Android Gradle Plugin 9.4.0
- Kotlin 2.3.21
- Gradle 9.6.0
- Compose BOM 2026.08.00
- Media3 1.11.0

## Build

The repository includes the Gradle wrapper after the first CI bootstrap. Build a debug APK with:

```bash
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

CI also uploads a compiled debug APK for every successful `main`/`develop` build.

## Releases

Stable releases are tag-driven. Push a semantic version tag such as `v1.0.0` and `.github/workflows/release.yml` builds, verifies and publishes a signed APK to GitHub Releases.

For signing, configure these GitHub Actions secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The signing key itself is intentionally never committed to the repository.

## Branches

- `main` — stable, releasable code
- `develop` — integration branch for upcoming changes
- feature work — branch from `develop`, then merge back through a pull request

## Export design

The `WebView` is captured as an alpha bitmap at the preview aspect ratio. Drag and zoom are stored as normalized transforms so the same placement can be mapped to the original media resolution. Photos are composited directly; videos use Media3 `BitmapOverlay` / `OverlayEffect` with Transformer so the result is a real transcoded MP4.

## License

OverlayForge is released under the [MIT License](LICENSE).

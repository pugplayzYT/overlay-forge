# Contributing

Thanks for helping improve OverlayForge.

## Workflow

1. Branch from `develop`.
2. Keep each change focused.
3. Run `./gradlew :app:lintDebug :app:assembleDebug` before opening a pull request.
4. Open the pull request against `develop` unless it is an urgent release fix.
5. Explain what changed and how it was tested.

`main` is reserved for stable, releasable code. Release tags are created from `main`.

## Style

- Use Kotlin official formatting.
- Keep the custom OverlayForge visual language rather than default Material styling.
- Prefer Android/Media3 APIs over screen-capture workarounds for export features.
- Never commit signing keys, passwords, local SDK paths or generated build output.

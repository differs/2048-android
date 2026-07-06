# 2048 (Android)

A commercial-grade Android reimplementation of the classic **2048** sliding-tile
puzzle, built with **Kotlin + Jetpack Compose**. Gameplay is a faithful clone of
the original, extended with polish and features expected of a shippable app.

## Features

- **Faithful 2048 gameplay** — swipe to move, matching tiles merge, reach 2048 to
  win and keep playing beyond it.
- **Smooth animations** — spring-based tile slide, pop-in for new tiles, and a
  bump on merges. Tiles keep stable identity across moves so animations are correct.
- **Multiple board sizes** — 4×4, 5×5, 6×6, each with its own saved game and best score.
- **Undo** — take back the last moves (bounded history).
- **Light / Dark / System themes.**
- **Sound effects + haptics**, individually toggleable.
- **Persistent state** — game, best scores, settings and stats saved with Jetpack
  DataStore; the game resumes where you left off.
- **Statistics** — games played/won, win rate, highest tile, total moves, average score.
- **Achievements** — unlockable milestones with in-game toast notifications.
- **Monetization seams** — `AdManager` and `BillingManager` interfaces with no-op
  implementations, ready to swap for AdMob / Play Billing without touching game code.

## Architecture

```
core/     Pure game engine (no Android deps) — fully unit-tested
data/     DataStore-backed persistence, settings, stats, achievements
sound/    SoundPool + Vibrator wrapper
ads/      AdManager interface + NoOpAdManager stub
billing/  BillingManager interface + NoOpBillingManager stub
ui/       Compose screens (game, settings, stats, achievements) + GameViewModel
```

The game logic in `core/GameEngine.kt` is deterministic given an RNG and holds no
Android dependencies, so it is covered by JVM unit tests in
`app/src/test/.../GameEngineTest.kt`.

## Build

```bash
./gradlew assembleDebug      # build debug APK
./gradlew test               # run unit tests
./gradlew installDebug       # install on a connected device/emulator
```

Requirements: JDK 17, Android SDK (compileSdk 34), minSdk 26.

## Enabling real monetization

Replace `NoOpAdManager` / `NoOpBillingManager` with implementations backed by the
Google Mobile Ads SDK and Google Play Billing Library respectively; the rest of
the app depends only on the interfaces.

## License

MIT — see [LICENSE](LICENSE).

# PS Launcher — Lightweight Android Game Launcher

**Package:** `com.nayem.pslauncher` · **v1.0.0 v1** · **minSdk 24 / targetSdk 34**
**Goal:** a very light, fast launcher for Android games — with a Game Booster,
physical + on-screen controller support, and a PS4 / PS5 hub.

> **Honest limit — read this first**
> PS4 / PS5 games do **not** run natively on Android. There is no stable
> PS4 emulator on Android, and no PS5 emulator at all. Any app that claims
> to "install PS4 PKG and play on any phone" is fake.
>
> The only real ways to play PS4 / PS5 games from Android:
> 1. **Remote Play** — stream from *your own* PS4 / PS5 on the same Wi-Fi
>    (or via PSN remote). This is what the PS Hub in this app does.
> 2. **PS Plus cloud streaming** — needs a PS Plus Premium subscription.
>
> This launcher does **not** bundle pirated games, BIOS, PKGs or an
> emulator. It launches your installed Android games, boosts the phone
> while you play, and connects you to Remote Play for your own console.

## What v1 does

- **Game Library** — lists installed games on the phone, one-tap launch,
  search, recent-first sorting. No ads, no tracking, no heavy libs.
- **Game Booster (`booster/GameBooster.kt`)** — one tap before play:
  - Do-Not-Disturb on, auto-brightness off + brightness up
  - Kill background processes (non-system), free RAM report
  - Keeps screen on, suggests closing heavy apps
  - Restore everything on exit
- **Controller (`controller/`)**
  - **Physical:** any Android gamepad via `InputManager` — DualShock 4,
    DualSense, Xbox, generic BT/USB. Button + stick mapping in
    `GamepadManager.kt`, tested in the Controller Test screen.
  - **Virtual:** on-screen joystick + ABXY + L/R in
    `VirtualControllerView.kt` — for phones with no pad. Touch → key events
    the game / Remote Play layer can read.
- **PS Hub (`ps/`)** — save your console IP + PSN ID, check Remote Play
  is installed, launch it, or open Play Store. v2 target: Chiaki
  (open-source Remote Play client) integration.

## Lightweight rules

- View system only, **no Compose** — small APK, fast cold start.
- Zero heavy dependencies: only `androidx.appcompat`, `core-ktx`,
  `recyclerview`, `material` (for the switch/button only).
- No internet permission in v1 except to open Play Store / PS links.
  No analytics, no ads SDK.
- `minSdk 24` (Android 7.0) so it runs on "any Android device".

## Build — CLOUD ONLY (standing rule)

Never build locally. Push to GitHub → GitHub Actions builds the APK.

```bash
# from ~/workspace/ps-launcher
git init
git add .
git commit -m "PS Launcher v1 scaffold"
# create repo errordrive/ps-launcher (or your pick), then:
git remote add origin https://github.com/errordrive/ps-launcher.git
git push -u origin main
# Actions → Android CI → download app-debug APK artifact
```

Workflow: `.github/workflows/android.yml` (JDK 17, Gradle 8.9).

## Folder

```
app/src/main/
  AndroidManifest.xml
  java/com/nayem/pslauncher/
    MainActivity.kt          # library + tabs
    ui/GameAdapter.kt        # game list
    booster/GameBooster.kt   # DND, RAM, brightness, restore
    controller/GamepadManager.kt
    controller/VirtualControllerView.kt
    ps/PsHub.kt              # Remote Play hub logic
    ps/PsHubActivity.kt      # PS Hub screen
  res/layout/ res/values/
docs/PS-HUB.md               # Remote Play setup guide
```

## Permissions (why each)

- `QUERY_ALL_PACKAGES` — list installed games only.
- `KILL_BACKGROUND_PROCESSES` + `PACKAGE_USAGE_STATS` — booster.
- `ACCESS_NOTIFICATION_POLICY` — DND toggle (user grants once).
- `BLUETOOTH_CONNECT` — DualShock / DualSense over BT.
- `VIBRATE` — controller haptics.

## Next step for Nayem

Pick one: **(A)** create repo `errordrive/ps-launcher` and I push + cloud-build
the v1 APK, **(B)** keep it local-only for now, **(C)** add Chiaki Remote Play
in v2 before the first APK.

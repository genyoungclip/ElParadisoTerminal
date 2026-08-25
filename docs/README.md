# Developer Documentation

## Building locally

```bash
# 1. Install Android SDK + NDK 26.1
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# 2. Generate a keystore for release signing
bash scripts/generate-keystore.sh

# 3. Build
./gradlew assembleDebug              # → app/build/outputs/apk/debug/*.apk
./gradlew assembleRelease bundleRelease
```

## Publishing checklist

1. `bash scripts/release.sh 1.0.1` → bumps `versionName` / `versionCode`, tags, pushes.
2. GitHub Actions (`.github/workflows/android-release.yml`) builds signed APKs & AAB, uploads to a GitHub Release.
3. `deploy-site.yml` pushes the updated website to `elp.elparadisogonzalo.net`.
4. `play-store.yml` (manual dispatch) promotes the AAB to Google Play.
5. `fdroid.yml` regenerates the F-Droid repo at `elp.elparadisogonzalo.net/fdroid/`.

## Required GitHub Secrets

| Secret | Purpose |
|---|---|
| `KEYSTORE_BASE64` | base64-encoded release keystore |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias inside the keystore |
| `KEY_PASSWORD` | key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Google Play API service account JSON |
| `FDROID_KEYSTORE_BASE64` | F-Droid signing key |
| `FDROID_KEYSTORE_PASSWORD` | F-Droid keystore password |
| `FDROID_KEY_ALIAS` | F-Droid key alias |
| `APT_GPG_PRIVATE` | GPG private key for signing the APT repo |
| `CPANEL_FTP_HOST` / `_USER` / `_PASSWORD` | cPanel FTP for elparadisogonzalo.net |

## Repository layout

```
app/                Android app (Kotlin + JNI)
website/            Static site → elp.elparadisogonzalo.net
scripts/            Bootstrap builder, repo signer, release helper
fastlane/           Play Store / F-Droid metadata
.github/workflows/  CI (android-release, android-debug, deploy-site,
                    bootstrap-build, apt-repo, play-store, fdroid, codeql)
```

## APT repository

`elp.elparadisogonzalo.net/apt/` is a plain Debian repo. Users add it via:

```bash
echo "deb https://elp.elparadisogonzalo.net/apt stable main" \
  | sudo tee /etc/apt/sources.list.d/elp.list
curl -fsSL https://elp.elparadisogonzalo.net/apt/elp.gpg \
  | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/elp.gpg
apt update
```

# El Paradiso Terminal

<p align="center">
  <img src="docs/logo.png" alt="El Paradiso Terminal" width="180"/>
</p>

<p align="center">
  <b>A full-featured Android terminal emulator & package manager</b><br/>
  Termux-compatible • PowerShell-ready • Multi-language runtime<br/>
  <a href="https://elp.elparadisogonzalo.net">elp.elparadisogonzalo.net</a>
</p>

<p align="center">
  <img alt="Build" src="https://github.com/elparadisogonzalo/elp-terminal/actions/workflows/android-release.yml/badge.svg"/>
  <img alt="License" src="https://img.shields.io/badge/license-GPL--3.0-blue"/>
  <img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen"/>
  <img alt="Version" src="https://img.shields.io/badge/version-1.0.0-orange"/>
</p>

---

## ✨ Features

- 🖥️ **Full Linux terminal emulator** — VT100/xterm compatible, based on the proven Termux terminal-emulator core.
- 📦 **APT-style package manager** — install `python`, `nodejs`, `ruby`, `go`, `rust`, `php`, `openjdk`, `clang`, `git`, `nginx`, `postgresql` and 1500+ more.
- 🐚 **Multi-shell support** — `bash`, `zsh`, `fish`, and a bundled **PowerShell 7** engine.
- 🐍 **Every major language pre-wired** — Python 3.12, Node.js 20, Ruby 3.3, Go 1.22, Rust 1.79, Java 21, PHP 8.3, Perl, Lua, R, Julia.
- 🌐 **Remote bootstrap** from `https://elp.elparadisogonzalo.net` — the app pulls its rootfs and package repos from the official mirror.
- 🎨 **Modern UI** — Material 3, extra keys row, sessions, tabs, dark/light themes, custom color schemes.
- 🔐 **Signed reproducible builds** via GitHub Actions.
- 📲 **F-Droid & Play Store ready** — full fastlane metadata included.

---

## 🚀 Quick Start

```bash
git clone https://github.com/elparadisogonzalo/elp-terminal.git
cd elp-terminal
./gradlew assembleRelease
```

The signed APKs land in `app/build/outputs/apk/release/`.

## 📱 Install

- **Direct APK:** https://elp.elparadisogonzalo.net/download/latest
- **F-Droid:** search *El Paradiso Terminal*
- **GitHub Releases:** [latest release](https://github.com/elparadisogonzalo/elp-terminal/releases/latest)

## 🧪 Inside the app

Once installed, on first launch the app pulls its bootstrap tarball from
`https://elp.elparadisogonzalo.net/bootstrap/`, unpacks it under
`$PREFIX = /data/data/net.elparadisogonzalo.terminal/files/usr`, and drops you
into a `bash` prompt. Then:

```bash
pkg update && pkg upgrade
pkg install python nodejs rust powershell git nano
python -m http.server 8080
pwsh
```

## 🏗️ Architecture

```
app/                 → Android app (Kotlin + JNI)
  ├── src/main/java  → Terminal UI, session manager, package client
  ├── src/main/cpp   → PTY / fork / exec native layer (JNI)
  └── assets         → Bootstrap installer, seed scripts
website/             → Static site served at elp.elparadisogonzalo.net
.github/workflows/   → CI (build, sign, release, deploy site, F-Droid)
fastlane/            → Store metadata & screenshots
scripts/             → Bootstrap builder, repo signer, release helper
```

## 📄 License

GPL-3.0-or-later. See [LICENSE](LICENSE).

Portions © Termux contributors (GPL-3.0) — this project reuses the
`terminal-emulator` and `terminal-view` modules with attribution.

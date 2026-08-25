#!/usr/bin/env bash
# Cuts a new release: bumps versionCode/Name, tags git, pushes.
set -euo pipefail
VERSION="${1:?usage: release.sh 1.2.3}"

sed -i -E "s/(versionName *= *\")[^\"]+/\1${VERSION}/" app/build.gradle.kts
CURRENT=$(grep -oE 'versionCode *= *[0-9]+' app/build.gradle.kts | grep -oE '[0-9]+')
NEXT=$((CURRENT + 1))
sed -i -E "s/(versionCode *= *)[0-9]+/\1${NEXT}/" app/build.gradle.kts

git add app/build.gradle.kts
git commit -m "release: v${VERSION} (${NEXT})"
git tag "v${VERSION}"
git push origin main --tags
echo "==> Pushed v${VERSION} — GitHub Actions will build & release."

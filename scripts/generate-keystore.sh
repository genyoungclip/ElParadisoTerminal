#!/usr/bin/env bash
# One-off: generate a release keystore and base64 it for GitHub Secrets.
set -euo pipefail
KEYSTORE=release.keystore
ALIAS=elp-release
STOREPASS="${STOREPASS:-changeme}"
KEYPASS="${KEYPASS:-changeme}"

keytool -genkey -v \
  -keystore "$KEYSTORE" \
  -alias   "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$STOREPASS" -keypass "$KEYPASS" \
  -dname "CN=El Paradiso Gonzalo, O=El Paradiso, L=Madrid, C=ES"

echo
echo "==> Base64 keystore (paste into KEYSTORE_BASE64 secret):"
base64 -w0 "$KEYSTORE"; echo
echo "==> Store password → KEYSTORE_PASSWORD"
echo "==> Key alias      → KEY_ALIAS  ($ALIAS)"
echo "==> Key password   → KEY_PASSWORD"

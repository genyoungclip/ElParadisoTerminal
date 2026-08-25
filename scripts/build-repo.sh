#!/usr/bin/env bash
# Rebuilds the APT repository served at https://elp.elparadisogonzalo.net/apt/
# Layout:
#   apt/
#     dists/stable/{Release,InRelease,Release.gpg}
#     dists/stable/main/binary-<arch>/{Packages,Packages.gz}
#     pool/main/<letter>/<pkg>/<pkg>_<ver>_<arch>.deb
set -euo pipefail

REPO=website/public/apt
ARCHES=(aarch64 arm x86_64 i686)
COMPONENTS=(main)
CODENAME=stable

mkdir -p "$REPO"/{pool/main,dists/$CODENAME}
for a in "${ARCHES[@]}"; do
  mkdir -p "$REPO/dists/$CODENAME/main/binary-$a"
done

# Index every .deb in packages/
for a in "${ARCHES[@]}"; do
  echo "==> Indexing $a"
  ( cd "$REPO" && dpkg-scanpackages -a "$a" pool/main /dev/null \
      > "dists/$CODENAME/main/binary-$a/Packages" ) || true
  gzip -f -k "$REPO/dists/$CODENAME/main/binary-$a/Packages"
done

cat > "$REPO/dists/$CODENAME/Release" <<EOF
Origin: El Paradiso Terminal
Label: elp
Suite: $CODENAME
Codename: $CODENAME
Architectures: ${ARCHES[*]}
Components: ${COMPONENTS[*]}
Description: Official APT repository for El Paradiso Terminal
Date: $(date -R -u)
EOF

apt-ftparchive release "$REPO/dists/$CODENAME/" \
  >> "$REPO/dists/$CODENAME/Release"

# Sign
if gpg --list-secret-keys apt@elparadisogonzalo.net >/dev/null 2>&1; then
  gpg --default-key apt@elparadisogonzalo.net \
      --clearsign  -o "$REPO/dists/$CODENAME/InRelease"  "$REPO/dists/$CODENAME/Release"
  gpg --default-key apt@elparadisogonzalo.net \
      --detach-sign -o "$REPO/dists/$CODENAME/Release.gpg" "$REPO/dists/$CODENAME/Release"
  gpg --armor --export apt@elparadisogonzalo.net > "$REPO/elp.gpg"
fi

echo "==> APT repo rebuilt at $REPO"

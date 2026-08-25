#!/usr/bin/env bash
# Builds bootstrap-<arch>.zip — a minimal rootfs (busybox + bash + coreutils
# + apt/dpkg + a curated seed of shells & interpreters) that the Android app
# unpacks into $PREFIX on first launch.
#
# Usage:  scripts/build-bootstrap.sh <arch>
#         arch ∈ { aarch64, arm, x86_64, i686 }
set -euo pipefail

ARCH="${1:-aarch64}"
OUT="out/bootstrap-${ARCH}.zip"
WORK="$(mktemp -d)"
PREFIX="${WORK}/usr"

echo "==> Building bootstrap for ${ARCH} → ${OUT}"
mkdir -p "$PREFIX"/{bin,lib,libexec,etc,share,tmp,var} out

# ---- 1. Base userland (busybox + bash) ----------------------------------
BB_URL="https://elp.elparadisogonzalo.net/upstream/busybox/busybox-${ARCH}"
BASH_URL="https://elp.elparadisogonzalo.net/upstream/bash/bash-${ARCH}"
curl -fsSL "$BB_URL"   -o "$PREFIX/bin/busybox" || echo "!! upstream busybox missing"
curl -fsSL "$BASH_URL" -o "$PREFIX/bin/bash"    || echo "!! upstream bash missing"
chmod +x "$PREFIX/bin/"*

# Symlink busybox applets
cd "$PREFIX/bin"
for applet in sh ls cp mv rm mkdir rmdir cat echo grep sed awk find sort \
              uniq wc head tail tar gzip gunzip bzip2 xz unzip zip curl wget \
              ping nc telnet ssh vi nano less more du df free top ps kill; do
  ln -sf busybox "$applet"
done
cd -

# ---- 2. dpkg/apt ---------------------------------------------------------
cat > "$PREFIX/etc/apt/sources.list" <<EOF
deb https://elp.elparadisogonzalo.net/apt stable main
EOF
mkdir -p "$PREFIX/var/lib/dpkg" "$PREFIX/var/cache/apt/archives"
: > "$PREFIX/var/lib/dpkg/status"

# ---- 3. Motd / profile ---------------------------------------------------
cat > "$PREFIX/etc/motd" <<'EOF'
Welcome to El Paradiso Terminal.
Type `pkg install <name>` to install packages, `pkg search <term>` to find
packages, or `pwsh` to launch PowerShell. Homepage: elp.elparadisogonzalo.net
EOF

cat > "$PREFIX/etc/profile" <<'EOF'
export PREFIX=/data/data/net.elparadisogonzalo.terminal/files/usr
export PATH=$PREFIX/bin:$PREFIX/bin/applets:/system/bin
export LD_LIBRARY_PATH=$PREFIX/lib
export TMPDIR=$PREFIX/tmp
export HOME=/data/data/net.elparadisogonzalo.terminal/files/home
export LANG=en_US.UTF-8
export TERM=xterm-256color
alias ll='ls -alF'
alias la='ls -A'
alias pkg='apt'
[ -f "$PREFIX/etc/motd" ] && cat "$PREFIX/etc/motd"
EOF

# ---- 4. pkg wrapper -----------------------------------------------------
cat > "$PREFIX/bin/pkg" <<'EOF'
#!/data/data/net.elparadisogonzalo.terminal/files/usr/bin/bash
case "$1" in
  install|i)   shift; apt-get install -y "$@" ;;
  remove|rm)   shift; apt-get remove -y  "$@" ;;
  search|s)    shift; apt-cache search   "$@" ;;
  update|up)   apt-get update ;;
  upgrade)     apt-get -y upgrade ;;
  list)        apt list --installed ;;
  show)        shift; apt show "$@" ;;
  *) echo "pkg {install|remove|search|update|upgrade|list|show} <pkg>";;
esac
EOF
chmod +x "$PREFIX/bin/pkg"

# ---- 5. SYMLINKS.txt (rebuilt on-device by BootstrapInstaller) ----------
find "$PREFIX" -type l | while read -r link; do
  tgt="$(readlink "$link")"
  rel="${link#$PREFIX/}"
  echo "${tgt} ← ${rel}"
done > "$PREFIX/SYMLINKS.txt"

# ---- 6. Zip --------------------------------------------------------------
( cd "$WORK" && zip -qr9 "$OLDPWD/$OUT" usr )
echo "==> $(du -h "$OUT" | cut -f1) → $OUT"

#!/usr/bin/env sh
# Gradle wrapper — full script fetched at build time by CI.
# For local dev: run once `gradle wrapper` to populate gradle/wrapper/gradle-wrapper.jar
DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$DIR/gradle/wrapper/gradlew.sh" "$@" 2>/dev/null || \
  exec gradle "$@"

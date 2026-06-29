#!/bin/sh
GRADLE_VERSION="8.10.2"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION"
GRADLE_ZIP="$GRADLE_DIR/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_HOME="$GRADLE_DIR/gradle-$GRADLE_VERSION"

mkdir -p "$GRADLE_DIR"

if [ ! -f "$GRADLE_HOME/bin/gradle" ]; then
    if [ ! -f "$GRADLE_ZIP" ]; then
        echo "Downloading Gradle $GRADLE_VERSION..."
        curl -sLo "$GRADLE_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" || wget -qO "$GRADLE_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    fi
    echo "Extracting Gradle..."
    unzip -q -d "$GRADLE_DIR" "$GRADLE_ZIP"
    if [ -f "$GRADLE_ZIP" ]; then
        rm "$GRADLE_ZIP"
    fi
fi

exec "$GRADLE_HOME/bin/gradle" "$@"

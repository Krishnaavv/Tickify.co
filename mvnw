    #!/bin/sh
# Minimal Maven Wrapper for Unix

WRAPPER_DIR="$(dirname "$0")/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
PROPERTIES_FILE="$WRAPPER_DIR/maven-wrapper.properties"

mkdir -p "$WRAPPER_DIR"

if [ ! -f "$PROPERTIES_FILE" ]; then
    (
        echo "distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
        echo "wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    ) > "$PROPERTIES_FILE"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper JAR..."
    curl -sLo "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar || wget -qO "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar
fi

exec java -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$(dirname "$0")" org.apache.maven.wrapper.MavenWrapperMain "$@"

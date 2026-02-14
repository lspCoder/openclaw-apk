#!/bin/bash
#
# Gradle wrapper script for OpenClaw APK
#

# Determine the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Determine the Java command
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Build the classpath
CLASSPATH="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

# Check if gradle-wrapper.jar exists, if not download it
if [ ! -f "$CLASSPATH" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p "$(dirname "$CLASSPATH")"
    curl -sL -o "$CLASSPATH" "https://github.com/gradle/gradle/raw/v8.0.0/gradle/wrapper/gradle-wrapper.jar"
fi

# Execute Gradle
exec "$JAVACMD" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"

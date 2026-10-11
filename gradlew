#!/bin/sh
#
# Gradle start up script for UN*X
#
# This script is a modified version of the Gradle wrapper script.
# It attempts to find a suitable JVM and then executes the Gradle wrapper JAR.

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
fi

# Determine the wrapper JAR location.
WRAPPER_JAR="$(cd "$(dirname "$0")" && pwd -P)/gradle/wrapper/gradle-wrapper.jar"

# Check if the wrapper JAR exists.
if [ ! -f "$WRAPPER_JAR" ] ; then
    die "ERROR: Could not find the Gradle wrapper JAR at $WRAPPER_JAR"
fi

# Execute the wrapper JAR.
exec "$JAVACMD" -jar "$WRAPPER_JAR" "$@"

die() {
    echo "$1"
    exit 1
}
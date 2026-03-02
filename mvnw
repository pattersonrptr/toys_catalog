#!/bin/sh
# Maven Wrapper script
# Permite rodar ./mvnw sem ter Maven instalado na máquina

set -e

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

if [ -r "$MAVEN_WRAPPER_PROPERTIES" ]; then
    DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/.*=//')
fi

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    WRAPPER_URL=$(grep "wrapperUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/.*=//')
    curl -fsSL "$WRAPPER_URL" -o "$MAVEN_WRAPPER_JAR" 2>/dev/null || \
    wget -q "$WRAPPER_URL" -O "$MAVEN_WRAPPER_JAR"
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"

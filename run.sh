#!/bin/sh

DEFAULT_JAVA_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"
DEBUG_OPTIONS=""

if [ -n "${JAVA_DEBUG_ENABLE:-}" ] ||  [ -n "${JAVA_DEBUG:-}" ]; then
    debug_port="${JAVA_DEBUG_PORT:-5005}"
    DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${debug_port}"
fi

echo -e "run.sh configuration:"
echo -e "\tDEFAULT_JAVA_OPTIONS: ${DEFAULT_JAVA_OPTIONS:-}"
echo -e "\tJAVA_OPTIONS: ${JAVA_OPTIONS:-}"
echo -e "\tDEBUG_OPTIONS: ${DEBUG_OPTIONS:-}"

java $DEFAULT_JAVA_OPTIONS $JAVA_OPTIONS $DEBUG_OPTIONS -jar /$JAR


#!/bin/bash
# Get the classpath from Maven
CLASSPATH=$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')
# Add target/classes to the classpath
FULL_CLASSPATH="target/classes:$CLASSPATH"
# Run the simulator
java -cp "$FULL_CLASSPATH" org.netuber.core.NetUberSimulator

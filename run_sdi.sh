#!/bin/bash

echo "Starting NetUber Real-World SDI Infrastructure..."

# 1. Compile
mvn clean install -DskipTests

# 2. Get Classpath
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q
CP=$(cat cp.txt):target/netuber-core-1.0-SNAPSHOT.jar
rm cp.txt

# 3. Start Agents for each region (Background)
echo "Launching Local Agents for US-East, US-West, EU-West..."
java -cp $CP org.netuber.agent.LocalAgent US-East > agent_us_east.log 2>&1 &
AGENT_PID1=$!
java -cp $CP org.netuber.agent.LocalAgent US-West > agent_us_west.log 2>&1 &
AGENT_PID2=$!
java -cp $CP org.netuber.agent.LocalAgent EU-West > agent_eu_west.log 2>&1 &
AGENT_PID3=$!

# 4. Give agents a moment to connect
sleep 5

# 5. Start the SDI Controller (NetUber Simulator)
echo "Launching SDI Controller..."
java -cp $CP org.netuber.core.NetUberSimulator

# 6. Cleanup
echo "Cleaning up agents and Docker containers..."
kill $AGENT_PID1 $AGENT_PID2 $AGENT_PID3 2>/dev/null
docker rm -f $(docker ps -a -q --filter "name=VR-") 2>/dev/null
echo "SDI Run Complete."

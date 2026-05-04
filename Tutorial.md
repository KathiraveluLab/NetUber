# NetUber SDI Tutorial: From Simulation to Real Deployment

This tutorial guides you through using NetUber as a real Software-Defined Internet (SDI) controller.

## 1. Concepts
*   **SDI Controller**: The brain (Orchestrator) that calculates optimal placements.
*   **Local Agent**: The distributed component running on each cloud node (e.g., AWS EC2).
*   **M4T Bus**: The messaging layer (ActiveMQ) that connects the brain to the hands.

## 2. Prerequisites
*   **ActiveMQ** running on `localhost:61616`.
*   **Docker** (Optional, for actual routing container support).

## 3. Running the "Real Thing"

### Step A: Start the Local Agents
In a real deployment, you would run one agent per VM. For testing, you can run multiple agents locally with different Node IDs.

```bash
# Terminal 1: Start US-East Agent
java -cp target/netuber-core-1.0-SNAPSHOT.jar org.netuber.agent.LocalAgent US-East

# Terminal 2: Start EU-West Agent
java -cp target/netuber-core-1.0-SNAPSHOT.jar org.netuber.agent.LocalAgent EU-West
```

### Step B: Start the SDI Controller
The controller will detect the topology, send `VR_DEPLOY` commands to the agents, and wait for BGP convergence (Active status).

```bash
# Terminal 3: Start the Controller
./run_simulation.sh
```

## 4. Observing the Control Loop
1.  **Controller** calculates that a VR is needed on `US-East`.
2.  **Controller** publishes a JSON command to `netuber/node/US-East/cmds`.
3.  **Local Agent** receives the command and generates a `bgpd.conf` for Quagga.
4.  **Local Agent** spawns the routing container and reports `ACTIVE` status.
5.  **Controller** receives the status and completes the workflow placement.

## 5. Scaling the Infrastructure
To scale to 100+ nodes, simply deploy the `LocalAgent` JAR to your cloud fleet. The orchestrator will automatically discover them through the messaging bus.

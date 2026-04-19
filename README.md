# NetUber
**Software-Defined Internet**

NetUber is a research framework for simulating and orchestrating latency-sensitive web service workflows using a fleet of shared virtual routers (VRs) deployed on cloud spot instances. The framework acts as an evaluation platform for a Software-Defined Internet, enabling dynamic, bandwidth-aware, cost-optimized inter-cloud routing.

## Features

NetUber provides the following capabilities:

* **DAG-Aware Orchestration**: Workflows are modeled as Directed Acyclic Graphs (DAGs), enabling complex multi-stage pipelines (e.g., Ingestion -> Transcoding -> Storage) with specific data volume constraints.
* **Spot Price Optimization**: The orchestrator dynamically evaluates the spot price of different availability zones and regions to minimize the cost of deploying the overlay network.
* **Bandwidth-Aware Routing**: Evaluates outbound bandwidth requirements across the service graph to select deployment nodes capable of handling the data volume.
* **Spot Preemption Simulation**: Provides a stochastic simulation of cloud spot instance preemption (runtime churn), forcing the orchestrator to dynamically recover and re-place services when underlying VMs are terminated.
* **Middleware Integration**: Fully integrated with the `Messaging4Transport` (M4T) architecture. Uses an AMQP broker (ActiveMQ) for publishing virtual router deployments and service placements, enabling a federated, event-driven orchestration model.

## Architecture

* **Core Models**: `Node`, `Link`, `VirtualRouter`, and `Workflow`.
* **LatencyAwarePlacement**: The engine responsible for calculating the optimal deployment map for a DAG workflow across the available topology, minimizing cost while verifying bandwidth and latency constraints.
* **OverlayManager**: Manages the lifecycle of the shared Virtual Router fleet.
* **ConnectivityProvider**: The M4T facade that authenticates and publishes orchestration events to external messaging queues.

## Getting Started

### Prerequisites
* **Java 11+**
* **Maven 3.x**
* **ActiveMQ** (Optional, but required for full M4T parity. Run locally on `tcp://localhost:61616`)

### Running the Simulation

A convenience script is provided to compile and execute the simulation:

```bash
./run_simulation.sh
```

This will run the `NetUberSimulator`, which initializes a 3-region topology (US-East, US-West, EU-West), defines a video-processing DAG workflow, calculates the optimal bandwidth-and-cost-aware placement, and then simulates random spot instance preemptions.

## Citing NetUber

If you use NetUber or the concept of a Software-Defined Internet in your research, please cite the following papers:

* Kathiravelu, P., Chiesa, M., Marcos, P., Canini, M. and Veiga, L., 2018, May. **Moving bits with a fleet of shared virtual routers.** In 2018 IFIP Networking Conference (IFIP Networking) and Workshops (pp. 1-9). IEEE.
  
* Kathiravelu, P., Van Roy, P., Veiga, L. and Benkhelifa, E., 2020, April. **Latency-sensitive web service workflows: A case for a software-defined internet.** In 2020 Seventh International Conference on Software Defined Systems (SDS) (pp. 115-122). IEEE.

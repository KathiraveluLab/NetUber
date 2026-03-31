package org.netuber.core;

import org.netuber.core.model.*;
import org.netuber.core.networking.ConnectivityProvider;
import org.netuber.core.networking.OverlayManager;
import org.netuber.core.orchestration.LatencyAwarePlacement;
import org.netuber.core.orchestration.Orchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetUberSimulator {
    private static final Logger logger = LoggerFactory.getLogger(NetUberSimulator.class);

    public static void main(String[] args) {
        logger.info("Initializing NetUber Advanced Research Simulation...");

        // 1. Setup Topology with Spot Prices
        Node usEast = new Node("US-East", "Virginia", 100, 256, 1000, 0.5);
        Node usWest = new Node("US-West", "California", 100, 256, 1000, 0.4);
        Node euWest = new Node("EU-West", "Ireland", 100, 256, 1000, 0.6);
        
        List<Node> topology = List.of(usEast, usWest, euWest);
        
        List<Link> links = new ArrayList<>();
        links.add(new Link(usEast, usWest, 70, 500, 10));
        links.add(new Link(usWest, euWest, 150, 200, 50));
        links.add(new Link(usEast, euWest, 80, 400, 20));

        // 2. Define Workflow DAG
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("Ingestion", List.of("Transcoding"));
        graph.put("Transcoding", List.of("Storage"));
        graph.put("Storage", List.of());

        Map<String, Double> volumes = new HashMap<>();
        volumes.put("Ingestion->Transcoding", 500.0);
        volumes.put("Transcoding->Storage", 200.0);

        Workflow videoProcess = new Workflow("Workflow-Video-Encoding", graph, volumes, 150.0);
        
        // 3. Initialize NetUber Framework
        OverlayManager overlayManager = new OverlayManager();
        LatencyAwarePlacement placementEngine = new LatencyAwarePlacement();
        ConnectivityProvider connectivityProvider = new ConnectivityProvider();
        Orchestrator orchestrator = new Orchestrator(overlayManager, placementEngine, connectivityProvider);

        // 4. Run Simulation
        logger.info("Executing Paper-Parity Orchestration...");
        orchestrator.orchestrateWorkflow(videoProcess, topology, links);
        
        // 5. Simulate Churn
        orchestrator.handleRuntimeChurn();

        logger.info("Simulation Complete.");
    }
}

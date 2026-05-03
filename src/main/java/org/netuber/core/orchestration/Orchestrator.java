package org.netuber.core.orchestration;

import org.netuber.core.model.*;
import org.netuber.core.networking.ConnectivityProvider;
import org.netuber.core.networking.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class Orchestrator {
    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
    private OverlayManager overlayManager;
    private LatencyAwarePlacement placementEngine;

    private ConnectivityProvider connectivityProvider;
    private Workflow currentWorkflow;
    private Map<String, Node> currentPlacement;
    private List<Node> currentTopology;
    private List<Link> currentLinks;

    public Orchestrator(OverlayManager overlayManager, LatencyAwarePlacement placementEngine, ConnectivityProvider connectivityProvider) {
        this.overlayManager = overlayManager;
        this.placementEngine = placementEngine;
        this.connectivityProvider = connectivityProvider;
    }

    public void orchestrateWorkflow(Workflow workflow, List<Node> topology, List<Link> links) {
        this.currentWorkflow = workflow;
        this.currentTopology = topology;
        this.currentLinks = links;
        
        logger.info("Starting Paper-Parity Orchestration for {}", workflow.getId());
        
        // 1. Manage Overlay: Ensure active VRs are available
        for (Node node : topology) {
            if (overlayManager.getActiveVRs(node).isEmpty()) {
                // Lease duration: 1 hour, Preemption prob: 5% (simulating multi-region spot market)
                VirtualRouter vr = overlayManager.deployVR(node, 100.0, 3600000, 0.05); 
                connectivityProvider.publishVRDeployment(node, vr);
            }
        }

        // 2. Calculate Advanced Placement (Bandwidth & Cost Aware)
        this.currentPlacement = placementEngine.calculatePlacement(workflow, topology, links);
        connectivityProvider.publishServicePlacement(workflow, currentPlacement);
        
        // 3. Verify DAG Latency Constraints
        double estimatedLatency = placementEngine.estimateEndToEndLatency(workflow, currentPlacement, links);
        if (estimatedLatency <= workflow.getMaxLatency()) {
            logger.info("NetUber Success! Estimated DAG Latency: {}ms", estimatedLatency);
        } else {
            logger.warn("Latency constraint violated for {}! Estimated: {}ms, Max: {}ms", 
                    workflow.getId(), estimatedLatency, workflow.getMaxLatency());
        }
    }

    public void handleRuntimeChurn() {
        logger.info("Detecting spot preemption in the fleet...");
        overlayManager.updateSpotPrices(currentTopology);
        overlayManager.simulateSpotPreemption();
        
        if (currentWorkflow == null || currentPlacement == null) return;

        boolean recoveryNeeded = false;
        for (Map.Entry<String, Node> entry : currentPlacement.entrySet()) {
            if (overlayManager.getActiveVRs(entry.getValue()).isEmpty()) {
                logger.warn("CRITICAL: Service {} lost its hosting VR on node {}!", entry.getKey(), entry.getValue().getId());
                recoveryNeeded = true;
            }
        }

        if (recoveryNeeded) {
            logger.info("Initiating research-parity recovery for workflow: {}", currentWorkflow.getId());
            orchestrateWorkflow(currentWorkflow, currentTopology, currentLinks);
        } else {
            logger.info("No active services affected by preemption. Shared fleet remains stable.");
        }
    }
}

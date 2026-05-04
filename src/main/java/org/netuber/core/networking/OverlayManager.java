package org.netuber.core.networking;

import org.netuber.core.model.Node;
import org.netuber.core.model.VirtualRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OverlayManager {
    private static final Logger logger = LoggerFactory.getLogger(OverlayManager.class);
    private Map<String, List<VirtualRouter>> nodeVRMap = new HashMap<>();
    private int vrCounter = 0;

    public VirtualRouter deployVR(Node node, double capacity, long leaseDuration, double preemptionProb) {
        String vrId = "VR-" + (++vrCounter);
        VirtualRouter vr = new VirtualRouter(vrId, node, capacity, leaseDuration, preemptionProb);
        nodeVRMap.computeIfAbsent(node.getId(), k -> new ArrayList<>()).add(vr);
        logger.info("Deployed {} on node {} (Capacity: {}, Price: {})", vrId, node.getId(), capacity, node.getSpotPrice());
        return vr;
    }

    public void simulateSpotPreemption() {
        logger.info("Simulating spot preemption events based on price spikes...");
        Random random = new Random();
        for (List<VirtualRouter> vrs : nodeVRMap.values()) {
            for (VirtualRouter vr : vrs) {
                Node node = vr.getHostNode();
                // If price spikes significantly (> 2.0), preemption probability increases
                double effectiveProb = node.getSpotPrice() > 2.0 ? vr.getPreemptionProbability() * 5 : vr.getPreemptionProbability();
                
                if (vr.isActive() && random.nextDouble() < effectiveProb) {
                    vr.setActive(false);
                    logger.warn("CRITICAL: VR {} preempted from node {} due to cloud price spikes (Current Price: {})!", 
                            vr.getId(), node.getId(), node.getSpotPrice());
                }
            }
        }
    }

    public void updateSpotPrices(List<Node> nodes) {
        logger.info("Updating cloud spot prices across regions...");
        Random random = new Random();
        for (Node node : nodes) {
            // Price fluctuates by +/- 20%
            double change = 0.8 + (1.2 - 0.8) * random.nextDouble();
            node.setSpotPrice(node.getSpotPrice() * change);
            logger.info("Node {}: New Spot Price: {}", node.getId(), String.format("%.2f", node.getSpotPrice()));
        }
    }

    public void updateVRStatus(String vrId, boolean isActive) {
        for (List<VirtualRouter> vrs : nodeVRMap.values()) {
            for (VirtualRouter vr : vrs) {
                if (vr.getId().equals(vrId)) {
                    vr.setActive(isActive);
                    return;
                }
            }
        }
    }

    public List<VirtualRouter> getAllActiveVRs() {
        List<VirtualRouter> active = new ArrayList<>();
        for (List<VirtualRouter> vrs : nodeVRMap.values()) {
            for (VirtualRouter vr : vrs) {
                if (vr.isActive()) active.add(vr);
            }
        }
        return active;
    }

    public List<VirtualRouter> getActiveVRs(Node node) {
        List<VirtualRouter> active = new ArrayList<>();
        List<VirtualRouter> vrs = nodeVRMap.get(node.getId());
        if (vrs != null) {
            for (VirtualRouter vr : vrs) {
                if (vr.isActive()) active.add(vr);
            }
        }
        return active;
    }
}

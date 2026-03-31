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
        logger.info("Simulating spot preemption events...");
        Random random = new Random();
        for (List<VirtualRouter> vrs : nodeVRMap.values()) {
            for (VirtualRouter vr : vrs) {
                if (vr.isActive() && random.nextDouble() < vr.getPreemptionProbability()) {
                    vr.setActive(false);
                    logger.warn("CRITICAL: VR {} preempted from node {} due to cloud price spikes!", 
                            vr.getId(), vr.getHostNode().getId());
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

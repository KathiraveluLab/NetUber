package org.netuber.core.orchestration;

import org.netuber.core.model.Link;
import org.netuber.core.model.Node;
import org.netuber.core.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LatencyAwarePlacement {
    private static final Logger logger = LoggerFactory.getLogger(LatencyAwarePlacement.class);

    public Map<String, Node> calculatePlacement(Workflow workflow, List<Node> availableNodes, List<Link> links) {
        logger.info("Calculating advanced research-parity placement for workflow: {}", workflow.getId());
        Map<String, Node> placement = new HashMap<>();
        
        // Priority: Satisfy Bandwidth -> Minimize Cost -> (Verify Latency later)
        for (String service : workflow.getServiceGraph().keySet()) {
            Node bestNode = null;
            double minCost = Double.MAX_VALUE;
            double requiredBW = calculateRequiredOutboundBW(service, workflow);

            for (Node node : availableNodes) {
                if (node.getBandwidthCapacity() >= requiredBW) {
                    if (node.getSpotPrice() < minCost) {
                        minCost = node.getSpotPrice();
                        bestNode = node;
                    }
                }
            }

            if (bestNode != null) {
                placement.put(service, bestNode);
                logger.info("Placed service {} on node {} (BW Req: {}, Spot Price: {})", 
                        service, bestNode.getId(), requiredBW, bestNode.getSpotPrice());
            }
        }
        
        return placement;
    }
    
    private double calculateRequiredOutboundBW(String service, Workflow workflow) {
        double total = 0;
        List<String> neighbors = workflow.getServiceGraph().get(service);
        if (neighbors != null) {
            for (String next : neighbors) {
                String edgeId = service + "->" + next;
                total += workflow.getDataVolume().getOrDefault(edgeId, 0.0);
            }
        }
        return total;
    }
    
    public double estimateEndToEndLatency(Workflow workflow, Map<String, Node> placement, List<Link> links) {
        // Simple longest path estimation for the DAG
        return findLongestPathLatency(workflow, placement, links);
    }

    private double findLongestPathLatency(Workflow workflow, Map<String, Node> placement, List<Link> links) {
        Map<String, Double> memo = new HashMap<>();
        double totalMax = 0;
        
        for (String service : workflow.getServiceGraph().keySet()) {
            totalMax = Math.max(totalMax, calculateServicePathLatency(service, workflow, placement, links, memo));
        }
        return totalMax;
    }

    private double calculateServicePathLatency(String service, Workflow workflow, Map<String, Node> placement, 
                                             List<Link> links, Map<String, Double> memo) {
        if (memo.containsKey(service)) return memo.get(service);
        
        double maxChildLatency = 0;
        List<String> neighbors = workflow.getServiceGraph().get(service);
        Node srcNode = placement.get(service);
        
        if (neighbors != null && srcNode != null) {
            for (String neighbor : neighbors) {
                Node dstNode = placement.get(neighbor);
                if (dstNode == null) continue;
                
                double edgeLatency = srcNode.getId().equals(dstNode.getId()) ? 0 : findLatency(srcNode, dstNode, links);
                double childPathLatency = edgeLatency + calculateServicePathLatency(neighbor, workflow, placement, links, memo);
                maxChildLatency = Math.max(maxChildLatency, childPathLatency);
            }
        }
        
        memo.put(service, maxChildLatency);
        return maxChildLatency;
    }

    private double findLatency(Node src, Node dst, List<Link> links) {
        for (Link link : links) {
            // Check both directions as virtual routers form a bidirectional overlay
            if ((link.getSource().equals(src) && link.getDestination().equals(dst)) ||
                (link.getSource().equals(dst) && link.getDestination().equals(src))) {
                return link.getLatency();
            }
        }
        return 100; // Default penalty
    }
}

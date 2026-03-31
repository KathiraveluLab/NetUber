package org.netuber.core.networking;

import org.netuber.core.model.Node;
import org.netuber.core.model.VirtualRouter;
import org.netuber.core.model.Workflow;
import org.opendaylight.messaging4transport.M4TFactory;
import org.opendaylight.messaging4transport.Messaging4TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class ConnectivityProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityProvider.class);
    private Messaging4TransportService m4t;

    public ConnectivityProvider() {
        try {
            this.m4t = M4TFactory.getService();
            logger.info("Successfully initialized Messaging4Transport Service.");
        } catch (Exception e) {
            logger.error("Failed to initialize M4T Service. Messaging will be mocked.", e);
        }
    }

    public void publishVRDeployment(Node node, VirtualRouter vr) {
        String topic = "netuber/vr/deploy/" + node.getId();
        String payload = String.format("{\"vrId\": \"%s\", \"capacity\": %.1f}", 
                vr.getId(), vr.getCapacity());
        
        logger.info("[M4T] Publishing VR deployment to {}: {}", topic, payload);
        if (m4t != null) {
            m4t.publish(topic, payload);
        }
    }

    public void publishServicePlacement(Workflow workflow, Map<String, Node> placement) {
        String topic = "netuber/service/placement/" + workflow.getId();
        String placementJson = placement.entrySet().stream()
                .map(e -> String.format("\"%s\": \"%s\"", e.getKey(), e.getValue().getId()))
                .collect(Collectors.joining(", ", "{", "}"));
        
        logger.info("[M4T] Publishing placement to {}: {}", topic, placementJson);
        if (m4t != null) {
            m4t.publish(topic, placementJson);
        }
    }
}

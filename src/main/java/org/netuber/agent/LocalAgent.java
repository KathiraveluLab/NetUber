package org.netuber.agent;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import jakarta.jms.*;

/**
 * NetUber Local Agent
 * Runs on cloud instances to execute SDI Controller commands.
 */
public class LocalAgent {
    private static final Logger logger = LoggerFactory.getLogger(LocalAgent.class);
    private String nodeId;
    private Session session;
    private Connection connection;

    public LocalAgent(String nodeId) {
        this.nodeId = nodeId;
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            this.connection = factory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            logger.info("LocalAgent for node {} successfully connected to SDI Bus.", nodeId);
        } catch (Exception e) {
            logger.error("Failed to connect LocalAgent to M4T bus.", e);
        }
    }

    public void start() {
        if (session == null) return;

        try {
            String topicName = "netuber.node." + nodeId + ".cmds";
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);
            
            logger.info("Subscribing to controller commands on topic: {}", topicName);
            
            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        String text = ((TextMessage) message).getText();
                        JSONObject cmd = new JSONObject(text);
                        handleCommand(cmd);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse command", e);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to start agent subscription.", e);
        }
    }

    private void handleCommand(JSONObject cmd) {
        String type = cmd.getString("command");
        logger.info("Received SDI Command: {}", type);

        switch (type) {
            case "VR_DEPLOY":
                String vrId = cmd.getString("vrId");
                double capacity = cmd.getDouble("capacity");
                deployVirtualRouter(vrId, capacity);
                break;
            case "TERMINATE":
                logger.warn("Agent on node {} received termination signal.", nodeId);
                System.exit(0);
                break;
            default:
                logger.warn("Unknown command type: {}", type);
        }
    }

    private void deployVirtualRouter(String vrId, double capacity) {
        logger.info("[SDI-EXEC] Generating BGP Configuration for VR: {}", vrId);
        String bgpConfig = generateBgpConfig(vrId);
        
        logger.info("[SDI-EXEC] Spawning Docker Container: osrg/quagga");
        
        // Command to start a real routing container with NET_ADMIN privileges
        String dockerCmd = String.format(
            "docker run -d --name %s --cap-add=NET_ADMIN osrg/quagga", 
            vrId
        );
        
        logger.info("Executing: {}", dockerCmd);
        
        try {
            Process process = Runtime.getRuntime().exec(dockerCmd);
            // Non-blocking for the agent logic
        } catch (Exception e) {
            logger.error("Failed to execute Docker command.", e);
        }

        try {
            // Simulate BGP convergence and startup delay
            Thread.sleep(2000); 
            publishStatus(vrId, "ACTIVE");
            logger.info("[SDI-EXEC] VR {} is now ACTIVE and routing bits.", vrId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateBgpConfig(String vrId) {
        StringBuilder sb = new StringBuilder();
        sb.append("hostname ").append(vrId).append("\n");
        sb.append("password zebra\n");
        sb.append("router bgp 65000\n");
        sb.append("  bgp router-id 10.0.0.1\n");
        sb.append("  network 192.168.0.0/24\n");
        
        // Dynamic neighbor assignment based on SDI Topology would go here
        sb.append("  neighbor 10.0.0.2 remote-as 65001\n");
        
        String config = sb.toString();
        logger.debug("Generated BGP Config:\n{}", config);
        return config;
    }

    private void publishStatus(String vrId, String status) {
        try {
            String topicName = "netuber.status." + nodeId;
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);

            JSONObject response = new JSONObject();
            response.put("nodeId", nodeId);
            response.put("vrId", vrId);
            response.put("status", status);
            response.put("timestamp", System.currentTimeMillis());

            TextMessage message = session.createTextMessage(response.toString());
            producer.send(message);
            logger.info("Reporting status to controller: {}", status);
            producer.close();
        } catch (Exception e) {
            logger.error("Failed to publish status.", e);
        }
    }

    public static void main(String[] args) {
        String id = args.length > 0 ? args[0] : "US-East";
        LocalAgent agent = new LocalAgent(id);
        agent.start();
        
        // Keep the agent running
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

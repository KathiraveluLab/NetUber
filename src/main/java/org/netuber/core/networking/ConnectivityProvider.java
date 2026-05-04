package org.netuber.core.networking;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.netuber.core.model.Node;
import org.netuber.core.model.VirtualRouter;
import org.netuber.core.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import jakarta.jms.*;
import java.util.Map;
import java.util.function.Consumer;

public class ConnectivityProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityProvider.class);
    private Session session;
    private Connection connection;

    public ConnectivityProvider() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            this.connection = factory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            logger.info("Successfully connected to ActiveMQ Broker.");
        } catch (Exception e) {
            logger.error("Failed to connect to ActiveMQ. Messaging will be mocked.", e);
        }
    }

    public void publishVRDeployment(Node node, VirtualRouter vr) {
        String topicName = "netuber.node." + node.getId() + ".cmds";
        try {
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            
            JSONObject cmd = new JSONObject();
            cmd.put("command", "VR_DEPLOY");
            cmd.put("vrId", vr.getId());
            cmd.put("capacity", vr.getCapacity());
            cmd.put("timestamp", System.currentTimeMillis());
            
            TextMessage message = session.createTextMessage(cmd.toString());
            producer.send(message);
            logger.info("[SDI-CONTROL] Sent VR_DEPLOY to {}: {}", topicName, cmd);
            producer.close();
        } catch (Exception e) {
            logger.error("Failed to publish VR deployment.", e);
        }
    }

    public void subscribeToStatus(Consumer<String> statusHandler) {
        try {
            Topic topic = session.createTopic("netuber.status.>");
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        statusHandler.accept(((TextMessage) message).getText());
                    }
                } catch (JMSException e) {
                    logger.error("Error receiving status message", e);
                }
            });
            logger.info("[SDI-CONTROL] Subscribed to telemetry on netuber.status.>");
        } catch (Exception e) {
            logger.error("Failed to subscribe to status.", e);
        }
    }

    public void publishServicePlacement(Workflow workflow, Map<String, Node> placement) {
        String topicName = "netuber.service.placement." + workflow.getId();
        try {
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            
            JSONObject payload = new JSONObject();
            placement.forEach((service, node) -> payload.put(service, node.getId()));
            
            TextMessage message = session.createTextMessage(payload.toString());
            producer.send(message);
            logger.info("[SDI-CONTROL] Sent placement to {}: {}", topicName, payload);
            producer.close();
        } catch (Exception e) {
            logger.error("Failed to publish placement.", e);
        }
    }
}

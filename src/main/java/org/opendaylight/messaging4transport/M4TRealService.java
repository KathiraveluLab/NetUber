package org.opendaylight.messaging4transport;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Real implementation of Messaging4TransportService using ActiveMQ/JMS.
 */
public class M4TRealService implements Messaging4TransportService {
    private Connection connection;
    private Session session;

    public M4TRealService() {
        try {
            // Real ActiveMQ Connection for AMQP/M4T bridging
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            System.err.println("[M4T Error] Failed to initialize real AMQP environment: " + e.getMessage());
            System.err.println("[M4T Error] Ensure ActiveMQ is running at tcp://localhost:61616 for research parity.");
        }
    }

    @Override
    public void publish(String topic, String message) {
        if (session == null) {
            System.err.println("[M4T Error] Cannot publish to " + topic + " - Service not initialized.");
            return;
        }
        
        try {
            Destination destination = session.createTopic(topic);
            MessageProducer producer = session.createProducer(destination);
            TextMessage textMessage = session.createTextMessage(message);
            
            producer.send(textMessage);
            System.out.println("[M4T MOM] REAL PUBLISH SUCCESS to " + topic);
            
            producer.close();
        } catch (JMSException e) {
            System.err.println("[M4T Error] Failed to publish message to topic " + topic + ": " + e.getMessage());
        }
    }
}

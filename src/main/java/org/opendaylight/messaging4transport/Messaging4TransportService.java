package org.opendaylight.messaging4transport;

/**
 * Service interface for Messaging4Transport.
 * Provides APIs to publish and subscribe to MOM topics for SDN orchestration.
 */
public interface Messaging4TransportService {
    /**
     * Publishes a message to a specific topic.
     * @param topic The AMQP/MOM topic.
     * @param message The payload (e.g., JSON flow update).
     */
    void publish(String topic, String message);
}

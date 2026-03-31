package org.opendaylight.messaging4transport;

/**
 * Factory for accessing the Messaging4Transport service.
 */
public class M4TFactory {
    private static Messaging4TransportService instance;

    public static Messaging4TransportService getService() {
        if (instance == null) {
            // In a real OSGi environment, this would be injected.
            // For the Évora standalone framework, we transition from mock to a real AMQP/JMS provider.
            instance = new M4TRealService();
        }
        return instance;
    }

    public static void setService(Messaging4TransportService service) {
        instance = service;
    }
}

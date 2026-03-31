package org.netuber.core.model;

public class Link {
    private Node source;
    private Node destination;
    private double latency;
    private double bandwidth;
    private double cost;

    public Link(Node source, Node destination, double latency, double bandwidth, double cost) {
        this.source = source;
        this.destination = destination;
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.cost = cost;
    }

    public Node getSource() { return source; }
    public Node getDestination() { return destination; }
    public double getLatency() { return latency; }
    public double getBandwidth() { return bandwidth; }
    public double getCost() { return cost; }

    @Override
    public String toString() {
        return "Link{" + source.getId() + "->" + destination.getId() + ", lat=" + latency + "ms}";
    }
}

package org.netuber.core.model;

public class Node {
    private String id;
    private String location;
    private double cpuCapacity;
    private double ramCapacity;
    private double bandwidthCapacity;
    private double spotPrice;

    public Node(String id, String location, double cpuCapacity, double ramCapacity, double bandwidthCapacity, double spotPrice) {
        this.id = id;
        this.location = location;
        this.cpuCapacity = cpuCapacity;
        this.ramCapacity = ramCapacity;
        this.bandwidthCapacity = bandwidthCapacity;
        this.spotPrice = spotPrice;
    }

    public String getId() { return id; }
    public String getLocation() { return location; }
    public double getCpuCapacity() { return cpuCapacity; }
    public double getRamCapacity() { return ramCapacity; }
    public double getBandwidthCapacity() { return bandwidthCapacity; }
    public double getSpotPrice() { return spotPrice; }

    @Override
    public String toString() {
        return "Node{" + "id='" + id + '\'' + ", location='" + location + '\'' + '}';
    }
}

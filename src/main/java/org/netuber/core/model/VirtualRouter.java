package org.netuber.core.model;

public class VirtualRouter {
    private String id;
    private Node hostNode;
    private double capacity;
    private boolean isActive;
    private long leaseEndTime;
    private double preemptionProbability;

    public VirtualRouter(String id, Node hostNode, double capacity, long leaseDuration, double preemptionProbability) {
        this.id = id;
        this.hostNode = hostNode;
        this.capacity = capacity;
        this.isActive = true;
        this.leaseEndTime = System.currentTimeMillis() + leaseDuration;
        this.preemptionProbability = preemptionProbability;
    }

    public String getId() { return id; }
    public Node getHostNode() { return hostNode; }
    public double getCapacity() { return capacity; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public long getLeaseEndTime() { return leaseEndTime; }
    public double getPreemptionProbability() { return preemptionProbability; }

    @Override
    public String toString() {
        return "VR{" + "id='" + id + '\'' + ", host=" + hostNode.getId() + '}';
    }
}

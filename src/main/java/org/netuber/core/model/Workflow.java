package org.netuber.core.model;

import java.util.List;
import java.util.Map;

public class Workflow {
    private String id;
    private Map<String, List<String>> serviceGraph; // Adjacency list for DAG
    private Map<String, Double> dataVolume; // Data volume between services (e.g. "S1->S2" -> 500MB)
    private double maxLatency;

    public Workflow(String id, Map<String, List<String>> serviceGraph, Map<String, Double> dataVolume, double maxLatency) {
        this.id = id;
        this.serviceGraph = serviceGraph;
        this.dataVolume = dataVolume;
        this.maxLatency = maxLatency;
    }

    public String getId() { return id; }
    public Map<String, List<String>> getServiceGraph() { return serviceGraph; }
    public Map<String, Double> getDataVolume() { return dataVolume; }
    public double getMaxLatency() { return maxLatency; }

    @Override
    public String toString() {
        return "Workflow{" + "id='" + id + '\'' + ", graph=" + serviceGraph.keySet() + ", max=" + maxLatency + "ms}";
    }
}

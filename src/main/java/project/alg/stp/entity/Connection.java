package project.alg.stp.entity;

import java.util.List;

public class Connection {

    private final int cost;
    private final List<String> connectedPorts;

    public Connection(int cost, List<String> connectedPorts){
        this.cost = cost;
        this.connectedPorts = connectedPorts;
    }

    public int getCost() {
        return cost;
    }

    public List<String> getConnectedPorts() {
        return connectedPorts;
    }

}

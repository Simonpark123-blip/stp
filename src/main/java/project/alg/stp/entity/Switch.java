package project.alg.stp.entity;

import java.util.ArrayList;
import java.util.List;

public class Switch {

    private List<Port> ports = new ArrayList<>();

    private final long priority;
    private final String macAddress;
    private final String name;

    public Switch(long priority, char switchName, String macAddress){
        int i = 0;
        while(ports.size() < 4){
            ports.add(new Port(switchName + "/" + (i+1)));
            i++;
        }
        this.name = String.valueOf(switchName);
        this.priority = priority;
        this.macAddress = macAddress;
    }

    public String getMacAddress(){
        return this.macAddress;
    }

    public long getPriority(){
        return this.priority;
    }

    public String getName(){
        return this.name;
    }

    public List<Port> getPorts(){
        return this.ports;
    }

    public void setPorts(List<Port> ports){
        this.ports = ports;
    }

}

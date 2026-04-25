package project.alg.stp.entity;

public class Port {

    private final String portName;
    private boolean isDesignated = false;
    private boolean isRoot = false;
    private boolean isBlocked = true;
    private boolean isUsed = false;

    public Port(String portName){
        this.portName = portName;
    }

    public String getPortName(){
        return this.portName;
    }

    public boolean isDesignated(){
        return this.isDesignated;
    }

    public boolean isRoot(){
        return this.isRoot;
    }

    public boolean isBlocked(){
        return this.isBlocked;
    }

    public void setIsDesignated(boolean isDesignated){
        this.isDesignated = isDesignated;
    }

    public void setIsRoot(boolean isRoot){
        this.isRoot = isRoot;
    }

    public void setIsBlocked(boolean isBlocked){
        this.isBlocked = isBlocked;
    }

    public void setIsUsed(boolean isUsed){
        this.isUsed = isUsed;
    }

    public boolean isUsed(){
        return this.isUsed;
    }

}

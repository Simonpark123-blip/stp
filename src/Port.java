public class Port {

    private String portName;
    private boolean isDesignated = false;
    private boolean isRoot = false;
    private boolean isBlocked = true;

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

}

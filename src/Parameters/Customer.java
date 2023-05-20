package Parameters;

public class Customer {
    private final int number;
    private final Position position;
    private final double demand;
    private final double readyTime;
    private final double dueTime;
    private final double serviceTime;
    private final boolean startDepot;

    public Customer(int number, Position position, double demand, double readyTime, double dueTime, double serviceTime, boolean startDepot) {
        this.number = number;
        this.position = position;
        this.demand = demand;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
        this.serviceTime = serviceTime;
        this.startDepot = startDepot;
    }

    public boolean isStartDepot() {return startDepot;}
    public int getNumber() {return number;}
    public Position getPosition() {return position;}
    public double getDemand() {return demand;}
    public double getReadyTime() {return readyTime;}
    public double getDueTime() {return dueTime;}
    public double getServiceTime() {return serviceTime;}


}

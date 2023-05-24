package Parameters;

import java.util.*;

public class ProblemInstance {
    private final String name;
    private final List<Customer> customers;
    //Vehicle Capacity is the same for each vehicle. Otherwise, a Vehicle class should be used and actions would have to be taken to eliminate symmetry of vehicles from the solutions.
     private final int vehicleCapacity;
    private final Td_info tdi;
    private final double[][] distances;

    public ProblemInstance(String name, List<Customer> customers, double[][] distances, int vehicleCapacity, Td_info tdi) {
        this.name = name;
        this.customers = customers;
        this.vehicleCapacity = vehicleCapacity;
        this.tdi = tdi;
        this.distances = distances;
    }

    public String getName() {return name;}
    public List<Customer> getCustomers() {
        return customers;
    }
    public int getVehicleCapacity() {
        return vehicleCapacity;
    }
    public Td_info getTdi() {return tdi;}
    public double[][] getDistances() {return distances;}


}

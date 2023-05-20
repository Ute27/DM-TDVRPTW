package Parameters.Solution;

import Parameters.*;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class TripSegment {
    private Customer previousCustomer;
    private final Customer customer;
    private final double eTime;
    private final double lTime;
    private double previousDistance;
    private double previousTravelTime;
    private double arrivalTime;
    private double departureTime;
    private final double serviceTime;
    private final Td_info tdi;
    private final List<Double> tdi_bpoints;
    private final int[][] tdi_arcCategories;
    private final double[][] tdi_travelspeedsPB;


    TripSegment(Customer customer1, Customer customer2, Td_info tdi) {
        this.previousCustomer = customer1;
        this.customer = customer2;
        this.eTime = customer2.getReadyTime();
        this.lTime = customer2.getDueTime();
        this.previousDistance = calculateDistance(customer1,customer2);
        this.tdi = tdi;
        this.tdi_bpoints = tdi.getBpoints();
        this.tdi_arcCategories = tdi.getArcCategories();
        this.tdi_travelspeedsPB = tdi.getTravelSpeedsPB();
        this.serviceTime = this.customer.getServiceTime();
        this.previousTravelTime = Integer.MAX_VALUE;
        this.arrivalTime = Integer.MAX_VALUE;
        this.departureTime = Integer.MAX_VALUE;

    }

    TripSegment(Customer customer, double departureTime, Td_info tdi) {
        //This must be the starting depot, otherwise this route is not feasible
        this.customer = customer;
        this.previousCustomer = customer;
        this.eTime = customer.getReadyTime();
        this.lTime = customer.getDueTime();
        this.previousDistance = 0;
        this.previousTravelTime = 0;
        this.tdi = tdi;
        this.tdi_bpoints = tdi.getBpoints();
        this.tdi_arcCategories = tdi.getArcCategories();
        this.tdi_travelspeedsPB = tdi.getTravelSpeedsPB();
        this.arrivalTime = departureTime;
        this.serviceTime = customer.getServiceTime(); //should be zero
        this.departureTime = departureTime;
    }

    public Customer getCustomer() {return customer;}
    public double geteTime() {return eTime;}
    public double getlTime() {return lTime;}
    public double getPreviousDistance() {return previousDistance;}
    public double getPreviousTravelTime() {return previousTravelTime;}
    public double getArrivalTime() {return arrivalTime;}
    public double getDepartureTime() {return departureTime;}
    public double getServiceTime() {return serviceTime;}

    public void setDepartureTimeAtBegin(double departureTimeAtBegin) {
        this.previousTravelTime = doIchoua(previousCustomer,customer,departureTimeAtBegin);
        this.arrivalTime = departureTimeAtBegin + previousTravelTime;
        double waitTime = 0;
        if(this.eTime>arrivalTime) {
            waitTime = this.eTime - arrivalTime;
        }
        if(this.lTime<arrivalTime) {
            waitTime = Integer.MAX_VALUE;
        }
        this.departureTime = arrivalTime + this.serviceTime + waitTime;
    }

    public void setArrivalTimeAtEnd(double arrivalTimeAtEnd) {
        this.previousTravelTime = doIchouaBackwards(previousCustomer,customer,arrivalTimeAtEnd);
        this.arrivalTime = arrivalTimeAtEnd;
        double waitTime = 0;
        if(this.eTime>arrivalTime) {
            waitTime = this.eTime - arrivalTime;
        }
        if(this.lTime<arrivalTime) {
            waitTime = Integer.MAX_VALUE;
        }
        this.departureTime = arrivalTime + this.serviceTime + waitTime;
    }

    double calculateDistance(Customer customer1, Customer customer2) {
        Position p1 = customer1.getPosition();
        Position p2 = customer2.getPosition();
        return sqrt(pow(p1.getX() - p2.getX(), 2) + pow(abs(p1.getY() - p2.getY()),2));
    }

    // Literal implementation of the Ichoua algorithm from customer 1 to this customer at a given startTime
    // Returns travelTime
    double doIchoua(Customer customer1, Customer customer2, double startTime) {
        int category = tdi_arcCategories[customer1.getNumber()][customer2.getNumber()];
        double t = startTime;
        int l = findCurrentBP(t); //index BP
        double d = calculateDistance(customer1,customer2);
        double t_new;
        try {
            t_new = t + d/tdi_travelspeedsPB[l][category];
        } catch (ArrayIndexOutOfBoundsException e) {
            // l is out of bounds thus the travelSpeed is zero, making the new arrivalTime infinity
            t_new = Double.MAX_VALUE;
        }
        if(l==tdi_bpoints.size()-1) return(t_new-startTime);
        while(t_new>tdi_bpoints.get(l+1)) {
            double distanceTraveled = (l==-1)?0:tdi_travelspeedsPB[l][category] * (tdi_bpoints.get(l+1)-t);
            d = d - distanceTraveled;
            t = tdi_bpoints.get(l+1);
            t_new = t + (d/tdi_travelspeedsPB[l+1][category]);
            l++;
            if(l==tdi_bpoints.size()-1) break;
        }
        return(t_new-startTime);
    }

    public double doIchouaBackwards(Customer customer1, Customer customer2, double arrivalTime) {
        int category = tdi_arcCategories[customer1.getNumber()][customer2.getNumber()];
        double t = arrivalTime;
        int l = findCurrentBP(t); //index BP
        double d = calculateDistance(customer1,customer2);
        double t_new;
        try {
            t_new = t - d/tdi_travelspeedsPB[l][category];
        } catch (ArrayIndexOutOfBoundsException e) {
            // l is out of bounds thus the travelSpeed is zero, making the new arrivalTime infinity
            t_new = Double.MAX_VALUE;
            if (l==-1) return Double.MAX_VALUE;
        }
        if(l == 0) return(arrivalTime-t_new);
        while(t_new<tdi_bpoints.get(l-1)) {
            double distanceTraveled = (l==-1)?0:tdi_travelspeedsPB[l][category] * (tdi_bpoints.get(l+1)-t);
            d = d - distanceTraveled;
            t = tdi_bpoints.get(l-1);
            t_new = t - (d/tdi_travelspeedsPB[l-1][category]);
            l--;
            if(l==0) break;
        }
        return(arrivalTime-t_new);
    }

    // This method returns the index of the breakpoint that defines the speed at a given departureTime
    int findCurrentBP(double departureTime) {
        int indexBP = -1;
        for(Double bpoint : tdi.getBpoints()) {
            if(departureTime<bpoint){
                return indexBP;
            }
            indexBP++;
        }
        return indexBP;
    }

    public void updateWithNewPreviousSegment(TripSegment previous) {
        this.previousCustomer = previous.getCustomer();
        this.previousDistance = calculateDistance(previousCustomer,customer);
        this.previousTravelTime = doIchoua(previousCustomer,customer,previous.getDepartureTime());
        this.arrivalTime = previous.getDepartureTime()+previousTravelTime;
        double waitTime = 0;
        if(this.eTime>this.arrivalTime) {
            waitTime = this.eTime-this.arrivalTime;
        }
        this.departureTime = arrivalTime + serviceTime + waitTime;
    }

    public void updateWithNewPreviousDepartureTime(TripSegment previous) {
        this.previousTravelTime = doIchoua(previous.getCustomer(),customer,previous.getDepartureTime());
        this.arrivalTime = previous.getDepartureTime()+previousTravelTime;
        double waitTime = 0;
        if(this.eTime>arrivalTime) {
            waitTime = this.eTime - arrivalTime;
        }
        this.departureTime = arrivalTime + this.serviceTime + waitTime;
    }

    public void updateWithNewEndDepartureTime(double departureTime) {
        this.departureTime = departureTime;
        this.arrivalTime = departureTime-serviceTime;
        this.previousTravelTime = doIchouaBackwards(previousCustomer, customer, this.arrivalTime);
    }

    boolean isFeasibleTW() {
        //It is allowed to arrive early, but then one needs to wait
        return arrivalTime <= lTime;
    }


}

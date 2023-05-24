package Parameters.Solution;
import Parameters.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SingleTrip {

    private final int nodes;
    private final double duration;
    private final ProblemInstance problemInstance;
    private final double loads;
    public final boolean feasibleTW;
    private final boolean feasibleC;
    private final boolean feasibleO;

    //The list underneath contains a tripSegment for each place where the vehicle stops.
    private final List<TripSegment> segments = new ArrayList<>();

    public SingleTrip(List<Customer> customers, Double departureTime, ProblemInstance problem) {

        //Calculate the total load
        double loads = 0;
        for(Customer customer: customers) {
            loads = loads + customer.getDemand();
        }
        this.loads = loads;
        Td_info tdi = problem.getTdi();
        this.problemInstance = problem;

        //Make a segment per customer
        int index = 0;
        for(Customer customer: customers) {

            TripSegment thisSegment;
            if(index==0) {
                thisSegment = new TripSegment(customer, departureTime, tdi);
            } else {
                TripSegment previous = segments.get(index-1);
                thisSegment = new TripSegment(previous.getCustomer(),customer, tdi);
                thisSegment.setDepartureTimeAtBegin(previous.getDepartureTime());
            }
            index++;
            segments.add(thisSegment);
        }


        nodes = segments.size();
        if(loads>problem.getVehicleCapacity()) {
            duration = 999999999;
        } else {
            duration = segments.get(segments.size()-1).getDepartureTime() - segments.get(0).getArrivalTime();
        }
        feasibleTW = isFeasibleTW();
        feasibleC = isFeasibleC();
        feasibleO = isFeasibleO();
    }

    public boolean getFeasibility() {
        return feasibleC && feasibleO && feasibleTW;
    }

    public double getDuration() {return duration;}
    public List<TripSegment> getSegments() {return segments;}




    double calculateTotalDistance() {
        double distance = 0.0;
        for (TripSegment segment: segments) {
            distance += segment.getPreviousDistance();
        }
        return distance;
    }

    double calculateTravelTime() {
        // This line only works if you already have a feasible order
        // double travelTime = segments.get(segments.size()-1).getArrivalTime() - segments.get(0).getDepartureTime();
        // Slower but more general method:
        double travelTime = 0;
        for(TripSegment segment: segments) {
            travelTime += segment.getDepartureTime() - segment.getArrivalTime() + segment.getPreviousTravelTime();
        }
        return travelTime;
    }

    boolean isFeasibleC() {
        return this.loads <= problemInstance.getVehicleCapacity();
    }

    //Checks if the order of vehicles is feasible; do the time windows not overlap?
    boolean isFeasibleO() {
        double firstTime = 0;
        for (TripSegment ts: segments) {
            double secondTime = ts.getArrivalTime() - ts.getPreviousTravelTime();
            if(firstTime>secondTime) return false;
            firstTime = secondTime;
        }
        return true;
    }

    // Returns if this trip is feasible considering the time windows
    boolean isFeasibleTW() {

        // Late time window checks: do you arrive before lTime?
        for (TripSegment ts: segments) {
            if(!ts.isFeasibleTW()) return false;
        }

        // Early time window checks: do you wait when you arrive too early?
        for (TripSegment ts: segments) {
            if(ts.geteTime() > ts.getArrivalTime()) {
                if (ts.getDepartureTime()<ts.geteTime() + ts.getServiceTime()) return false;
            }
        }
        return true;
    }

    void print() {
        System.out.println("SingleTrip Details: ... ");
        System.out.println("Duration: " + duration);
        System.out.println("Distance: " + calculateTotalDistance());
        for(TripSegment ts:segments) {
            System.out.print(ts.getCustomer().getNumber() + ", ");
        }
        System.out.println();

    }
    void printAddToSB(StringBuilder sb) throws IOException {
        sb.append("SingleTrip Details: ... " + "\n");
        sb.append("Duration: ").append(duration).append("\n");
        sb.append("Distance: ").append(calculateTotalDistance()).append("\n");
        for(TripSegment ts:segments) {
            sb.append(ts.getCustomer().getNumber());
            if(ts!=segments.get(segments.size()-1)) {
                sb.append(", ");
            } else {
                sb.append("\n");
            }
        }

        sb.append("nodes: ").append(nodes).append("\n");
        int i=0;
        sb.append("Index \t Cus Num \t E_time \t L_time \t Dist(prv) \t T_time(prv) \t ArrTime \t DepTime \t ServiceTime \n");
        for(TripSegment ts:segments) {
            i++;
            printTripSegment(ts,sb,i);
        }

    }

    void printTripSegment(TripSegment ts,StringBuilder sb, int index) throws IOException {
        //System.out.println("Customer number: " + ts.customer.number + " ArrivalTime: " + ts.arrivalTime + " DepartureTime: " + ts.departureTime);
        sb.append(index).append(" \t ").append(ts.getCustomer().getNumber()).append(" \t ").append(ts.geteTime()).append(" \t ").append(ts.getlTime()).append(" \t ").append(ts.getPreviousDistance()).append(" \t ").append(ts.getPreviousTravelTime()).append(" \t ").append(ts.getArrivalTime()).append(" \t ").append(ts.getDepartureTime()).append(" \t ").append(ts.getServiceTime()).append(" \n");
    }

    public void deleteThisSegment(TripSegment segmentToDelete) {

        boolean found=false;
        TripSegment previous = segments.get(0);
        int indexDeleted = segments.indexOf(segmentToDelete);
        segments.remove(segmentToDelete);
        for(TripSegment segment: segments) {
            if(segments.indexOf(segment)==indexDeleted) {
                segment.updateWithNewPreviousSegment(previous);
                found=true;

            }
            if(found) {
                segment.updateWithNewPreviousDepartureTime(previous);
            }
            previous = segment;

            //You can calculate this more efficiently, but then you would have to keep track of the added cost up until
            //a given moment, which would take a lot of memory.
            calculateTravelTime();
            calculateTotalDistance();

        }
    }

    public void changeToDepartLatest(double departureTimeAtEnd) {
        double newDepartureTime = departureTimeAtEnd;
        for(int index = segments.size()-1; index>=0; index--) {
            segments.get(index).updateWithNewEndDepartureTime(newDepartureTime);
            newDepartureTime = segments.get(index).getArrivalTime()-segments.get(index).getPreviousTravelTime();
        }
    }
}

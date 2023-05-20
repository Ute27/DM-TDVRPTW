package Parameters.Solution;
import Parameters.*;
import Parameters.Concatenation.ConcatenatedSegment;
import Parameters.Concatenation.ConcatenatedTrip;

import java.io.IOException;
import java.util.*;

public class Solution {

    //Customer 0 is the start depot and customer nbNodes is the end depot
    private final List<SingleTrip> routes;
    private double totalCost;
    private boolean feasibility;
    public ProblemInstance problem;
    private final Td_info tdi;
    private final double infeasibleRegret = 999999999;

    public Solution(Solution sol) {
        this.routes = sol.routes;
        this.totalCost = sol.totalCost;
        this.feasibility = sol.feasibility;
        this.problem = sol.problem;
        this.tdi = problem.getTdi();
    }

    public boolean getFeasibility() {return feasibility;}

    public Solution(List<List<Customer>> customers, ProblemInstance problem) {
        this.tdi = problem.getTdi();
        this.routes = new ArrayList<>();
        this.problem = problem;
        for(List<Customer> route: customers) {
            double dt = route.get(0).getReadyTime();
            if(route.size()>6) {
                dt = findOptimalDT(route);
            }
            SingleTrip st = new SingleTrip(route,dt,problem);
            this.routes.add(st);
        }
        calculateCost();
        feasibility = calculateFeasibility();
    }

    public List<SingleTrip> getRoutes() {
        return routes;
    }
    public double getTotalCost() {
        return totalCost;
    }
    public ProblemInstance getProblem() {
        return problem;
    }

    public void print() {
        System.out.println("------------------------------------SOLUTION DETAILS-----------------------------------");
        System.out.println("Number of routes: " + routes.size());
        System.out.println("Total Cost: " + totalCost);
        System.out.println("FEASIBLE?: " + feasibility);
        for (SingleTrip singleTrip: routes) {
                singleTrip.print();

        }
    }

    public void printAddToSB(StringBuilder sb) throws IOException {
        sb.append("------------------------------------SOLUTION DETAILS-----------------------------------\n");
        sb.append("Number of routes: ").append(routes.size()).append("\n");
        sb.append("Total Cost: ").append(totalCost).append("\n");
        sb.append("FEASIBLE?: ").append(feasibility).append("\n");
        for (SingleTrip singleTrip: routes) {
                singleTrip.printAddToSB(sb);

        }
    }

    void calculateCost() {
        //The cost in this case is the totalTravelTime
        totalCost = 0.0;
        for (SingleTrip singleTrip: routes) {
                totalCost += singleTrip.calculateTravelTime();
        }
    }

    public void updateFeasibility() {
        feasibility = calculateFeasibility();
    }

    boolean calculateFeasibility() {

        // 1. Check if all customers are visited exactly once; always true, no need to check
        // 2. Check if all early and late times are considered in the segments
        // 3. Check if there are no capacity faults

        for (SingleTrip trip: routes) {
                if (!trip.isFeasibleTW()) return false;
                if (!trip.isFeasibleC()) return false;
                if (!trip.isFeasibleO()) return false;
        }
        return true;

    }

    //____________________________________________________________________________________________________________

    //____________________________________________________________________________________________________________

    // EVERYTHING TO DO WITH HEURISTICS


    /**
     *
     * First come the destroy methods
     *
     */

    public void destroyCustomer(Customer customer) {
        SingleTrip changedRoute = routes.get(0);
        TripSegment changedSegment = changedRoute.getSegments().get(0);

        for(SingleTrip trip: routes) {
                for(TripSegment segment: trip.getSegments()) {
                    if(segment.getCustomer()==customer) {
                        changedSegment = segment;
                        changedRoute = trip;
                    }
                }

        }
        changedRoute.deleteThisSegment(changedSegment);
        if(changedRoute.getSegments().size()<3) {
            routes.remove(changedRoute);
        }
        calculateCost();
    }

    /**
     *
     * different Repair Regret methods
     *
     */

    public void repairRegretRANDOPT(Customer customerToInsert, long seedNumber) {
        Random rnd = new Random(seedNumber);
        int k_index = rnd.nextInt(3); //index equals 0,1 or 2.
        int k = k_index+1;
        double[][] mic_regretValues = new double[routes.size()][5];

        //First calculate the mic[][] for every route which holds the segment index and the insertion cost
        int route_index = 0;
        for(SingleTrip route: routes) {
            double[][] mic = calculateMIC_Random(route, customerToInsert, problem, seedNumber);
            calculateRegretValue(k, mic_regretValues, route_index, mic);
            route_index++;
        }

        //mic_regretValues is sorted based on the regret Values
        insertCustomerBasedOnMICValuesButOptimal(customerToInsert, mic_regretValues);

    }

    public void repairRegretRANDRAND(Customer customerToInsert, long seedNumber) {

        Random rnd = new Random(seedNumber);
        int k_index = rnd.nextInt(3); //index equals 0,1 or 2.
        int k = k_index+1;
        //col1 = route index
        //col2 = regret value
        //col3 = segment index for minimal insertionCost
        //col4 = duration of trip when inserted
        //col5 = random departureTime of trip when inserted
        double[][] mic_regretValues = new double[routes.size()][5];

        // Calculate the minimum insertion cost of the customer to every route it can be assigned to
        int route_index = 0;
        for(SingleTrip route: routes) {
            double[][] mic = calculateMIC_Random(route, customerToInsert, problem, seedNumber);
            calculateRegretValue(k, mic_regretValues, route_index, mic);
            route_index++;
        }

        //mic_regretValues is sorted based on the regret Values
        insertCustomerBasedOnMICValues(customerToInsert, mic_regretValues);

    }

    public void repairRegretOPTOPT(Customer customerToInsert, long seedNumber) {

        Random rnd = new Random(seedNumber);
        int k_index = rnd.nextInt(3); //index equals 0,1 or 2.
        int k = k_index+1;
        //col1 = route index
        //col2 = regret value
        //col3 = segment index for minimal insertionCost
        //col4 = duration of trip when inserted
        //col5 = optimal departureTime of trip when inserted
        double[][] mic_regretValues = new double[routes.size()][5];

        // Calculate the minimum insertion cost of the customer to every route it can be assigned to
        int route_index = 0;
        for(SingleTrip route: routes) {
            double[][] mic = calculateMIC_Optimal(route, customerToInsert, problem);
            calculateRegretValue(k, mic_regretValues, route_index, mic);
            route_index++;
        }

        //mic_regretValues is sorted based on the regret Values
        //komt op hetzelfde neer als basedonmicvaluesbutoptimal, maar die optimale tijdstippen zijn toch al eens berekend dus moet niet opnieuw gedaan worden
        insertCustomerBasedOnMICValues(customerToInsert, mic_regretValues);

    }

    public void repairRegretEARLYOPT(Customer customerToInsert, long seedNumber) {

        Random rnd = new Random(seedNumber);
        int k_index = rnd.nextInt(3); //index equals 0,1 or 2.
        int k = k_index+1;
        //col1 = route index
        //col2 = regret value
        //col3 = segment index for minimal insertionCost
        //col4 = duration of trip when inserted
        //col5 = optimal departureTime of trip when inserted
        double[][] mic_regretValues = new double[routes.size()][5];

        // Calculate the minimum insertion cost of the customer to every route it can be assigned to
        int route_index = 0;
        for(SingleTrip route: routes) {
            double[][] mic = calculateMIC_Early(route, customerToInsert, problem);
            calculateRegretValue(k, mic_regretValues, route_index, mic);
            route_index++;
        }

        //mic_regretValues is sorted based on the regret Values
        insertCustomerBasedOnMICValuesButOptimal(customerToInsert, mic_regretValues);

    }

    public void repairRegretEARLYEARLY(Customer customerToInsert, long seedNumber) {

        Random rnd = new Random(seedNumber);
        int k_index = rnd.nextInt(3); //index equals 0,1 or 2.
        int k = k_index+1;
        //col1 = route index
        //col2 = regret value
        //col3 = segment index for minimal insertionCost
        //col4 = duration of trip when inserted
        //col5 = optimal departureTime of trip when inserted
        double[][] mic_regretValues = new double[routes.size()][5];

        // Calculate the minimum insertion cost of the customer to every route it can be assigned to
        int route_index = 0;
        for(SingleTrip route: routes) {
            double[][] mic = calculateMIC_Early(route, customerToInsert, problem);
            calculateRegretValue(k, mic_regretValues, route_index, mic);
            route_index++;
        }

        //mic_regretValues is sorted based on the regret Values
        insertCustomerBasedOnMICValues(customerToInsert, mic_regretValues);

    }




    /**
     *
     * calculations for MIC and MIC Regret Values
     *
     */

    public double[][] calculateMIC_Early(SingleTrip route, Customer customerToInsert, ProblemInstance problem) {
        //returns a double matrix of which the first column is the minimal insertionCost
        //the second column is the segment index for minimal insertionCost
        //the third column is the departure time

        double[][] result = new double[route.getSegments().size()][3];
        Arrays.stream(result).forEach(a -> Arrays.fill(a, infeasibleRegret));

        List<Customer> customers = new ArrayList<>();
        for(TripSegment segment: route.getSegments()) {
            customers.add(segment.getCustomer());
        }

        //For each insertionPlace, calculate bestDepartureTime and calculate the cost of it
        double insertionCost = infeasibleRegret;
        for(int index_insertionPlace = 1; index_insertionPlace<customers.size()-1; index_insertionPlace++) {
            List<Customer> customerCopy = new ArrayList<>(customers);
            customerCopy.add(index_insertionPlace, customerToInsert);
            double optimalDT = earliestDT(customerCopy);
            simulateTrip(problem, result, insertionCost, index_insertionPlace, customerCopy, optimalDT);

        }
        Arrays.sort(result, Comparator.comparingDouble(row -> row[0]));

        return result;
    }

    public double[][] calculateMIC_Random(SingleTrip route, Customer customer, ProblemInstance problem, long seedNumber) {
        //returns a double matrix of which the first column is the segment index
        //the second column is insertionCost

        double[][] result = new double[route.getSegments().size()][3];
        Arrays.stream(result).forEach(a -> Arrays.fill(a, infeasibleRegret));

        List<Customer> customers = new ArrayList<>();
        for(TripSegment segment: route.getSegments()) {
            customers.add(segment.getCustomer());
        }

        //For each insertionPlace, calculate bestDepartureTime and calculate the cost of it
        double insertionCost = infeasibleRegret;
        for(int index_insertionPlace = 1; index_insertionPlace<customers.size(); index_insertionPlace++) {
            List<Customer> customerCopy = new ArrayList<>(customers);
            customerCopy.add(index_insertionPlace, customer);
            double earlyDT = earliestDT(customerCopy);
            double lateDT = latestDT(customerCopy);
            Random r = new Random(seedNumber);
            double randomDT = earlyDT + (lateDT - earlyDT) * r.nextDouble() ;
            simulateTrip(problem, result, insertionCost, index_insertionPlace, customerCopy, randomDT);

        }
        Arrays.sort(result, Comparator.comparingDouble(row -> row[0]));

        return result;
    }

    public double[][] calculateMIC_Optimal(SingleTrip route, Customer customerToInsert, ProblemInstance problem) {
        //returns a double matrix of which the first column is the minimal insertionCost
        //the second column is the segment index for minimal insertionCost
        //the third column is the departure time

        double[][] result = new double[route.getSegments().size()][3];
        Arrays.stream(result).forEach(a -> Arrays.fill(a, infeasibleRegret));

        List<Customer> customers = new ArrayList<>();
        for(TripSegment segment: route.getSegments()) {
            customers.add(segment.getCustomer());
        }

        //For each insertionPlace, calculate bestDepartureTime and calculate the cost of it
        double insertionCost = infeasibleRegret;
        for(int index_insertionPlace = 1; index_insertionPlace<customers.size()-1; index_insertionPlace++) {
            List<Customer> customerCopy = new ArrayList<>(customers);
            customerCopy.add(index_insertionPlace, customerToInsert);
            double optimalDT = findOptimalDT(customerCopy);
            simulateTrip(problem, result, insertionCost, index_insertionPlace, customerCopy, optimalDT);

        }
        Arrays.sort(result, Comparator.comparingDouble(row -> row[0]));

        return result;
    }

    public double earliestDT(List<Customer> customerList) {
        TripSegment ts = new TripSegment(customerList.get(0),customerList.get(1),tdi);
        ts.setArrivalTimeAtEnd(customerList.get(1).getReadyTime());
        return ts.getArrivalTime()-ts.getPreviousTravelTime();
    }
    public double latestDT(List<Customer> customerList) {
        TripSegment ts = new TripSegment(customerList.get(0),customerList.get(1),tdi);
        ts.setArrivalTimeAtEnd(customerList.get(1).getDueTime());
        return ts.getArrivalTime()-ts.getPreviousTravelTime();
    }

    private void simulateTrip(ProblemInstance problem, double[][] result, double insertionCost, int index_insertionPlace, List<Customer> customerCopy, double randomDT) {
        SingleTrip st = new SingleTrip(customerCopy,randomDT,problem);
        if(st.getFeasibility() && st.getDuration()<insertionCost) {
            result[index_insertionPlace][0] = st.getDuration();
            result[index_insertionPlace][1] = index_insertionPlace-1;
            result[index_insertionPlace][2] = randomDT;
        } else {
            result[index_insertionPlace][0] = infeasibleRegret;
            result[index_insertionPlace][1] = index_insertionPlace-1;
        }
    }


    /**
     *
     * Insert customer methods
     *
     */

    private void insertCustomerBasedOnMICValues(Customer customerToInsert, double[][] mic_regretValues) {
        Arrays.sort(mic_regretValues, Comparator.comparingDouble(row -> row[1]));
        if ((int) mic_regretValues[mic_regretValues.length-1][1] <= infeasibleRegret) {
            //The customer can not be inserted in any route and will thus have a route for itself
            List<Customer> customers = new ArrayList<>();
            customers.add(problem.getCustomers().get(0));
            customers.add(customerToInsert);
            customers.add(problem.getCustomers().get(problem.getCustomers().size()-1));

            double dt = customers.get(0).getReadyTime();
            SingleTrip st = new SingleTrip(customers, dt, problem);

            routes.add(st);
            totalCost = totalCost + st.getDuration();
        }
        else {
            int routeIndexToInsert = (int) mic_regretValues[mic_regretValues.length-1][0];
            int segmentIndexToInsert = (int) mic_regretValues[mic_regretValues.length-1][2];
            double startTimeToPick = mic_regretValues[mic_regretValues.length-1][4];

            TripSegment segmentAfterWhichToInsert = routes.get(routeIndexToInsert).getSegments().get(segmentIndexToInsert);
            totalCost = totalCost - routes.get(routeIndexToInsert).getDuration();
            List<TripSegment> segments = routes.get(routeIndexToInsert).getSegments();
            List<Customer> customers = new ArrayList<>();
            for(TripSegment segment: segments) {
                customers.add(segment.getCustomer());
                if(segment == segmentAfterWhichToInsert) {
                    customers.add(customerToInsert);
                }
            }
            SingleTrip st = new SingleTrip(customers, startTimeToPick, problem);

            routes.remove(routeIndexToInsert);
            routes.add(routeIndexToInsert,st);
            totalCost = totalCost + st.getDuration();

        }
        calculateCost();
        updateFeasibility();
    }

    private void insertCustomerBasedOnMICValuesButOptimal(Customer customerToInsert, double[][] mic_regretValues) {
        //from low to high regret value
        Arrays.sort(mic_regretValues, Comparator.comparingDouble(row -> row[1]));
        if ((int) mic_regretValues[mic_regretValues.length-1][1] <= infeasibleRegret) {
            //The customer can not be inserted in any route and will thus have a route for itself
            List<Customer> customers = new ArrayList<>();
            customers.add(problem.getCustomers().get(0));
            customers.add(customerToInsert);
            customers.add(problem.getCustomers().get(problem.getCustomers().size()-1));

            double dt = customers.get(0).getReadyTime();
            SingleTrip st = new SingleTrip(customers, dt, problem);

            routes.add(st);
            totalCost = totalCost + st.getDuration();
        }
        else {
            int routeIndexToInsert = (int) mic_regretValues[mic_regretValues.length-1][0];
            int segmentIndexToInsert = (int) mic_regretValues[mic_regretValues.length-1][2];

            TripSegment segmentAfterWhichToInsert = routes.get(routeIndexToInsert).getSegments().get(segmentIndexToInsert);
            totalCost = totalCost - routes.get(routeIndexToInsert).getDuration();
            List<TripSegment> segments = routes.get(routeIndexToInsert).getSegments();
            List<Customer> customers = new ArrayList<>();
            for(TripSegment segment: segments) {
                customers.add(segment.getCustomer());
                if(segment == segmentAfterWhichToInsert) {
                    customers.add(customerToInsert);
                }
            }

            double optimalDT = findOptimalDT(customers);
            SingleTrip st = new SingleTrip(customers, optimalDT, problem);

            routes.remove(routeIndexToInsert);
            routes.add(routeIndexToInsert,st);
            totalCost = totalCost + st.getDuration();

        }
        calculateCost();
        updateFeasibility();
    }

    private void calculateRegretValue(int k, double[][] mic_regretValues, int route_index, double[][] mic) {
        double regretValue = 0;
        if (mic[0][0]>=infeasibleRegret) {
            regretValue = infeasibleRegret;
        }
        else {
            for(int i=0; i<k; i++) {
                regretValue += mic[i][0]-mic[0][0];
            }
        }

        mic_regretValues[route_index][0] = route_index;
        mic_regretValues[route_index][1] = regretValue;
        mic_regretValues[route_index][2] = mic[0][1];
        mic_regretValues[route_index][3] = mic[0][0];
        mic_regretValues[route_index][4] = mic[0][2];
    }


    public boolean visitsCustomer(Customer customer) {
        List<Integer> visitedCustomers = new ArrayList<>();
        for(SingleTrip trip: routes) {
            for(TripSegment segment: trip.getSegments()) {
                visitedCustomers.add(segment.getCustomer().getNumber());
            }
        }
        return visitedCustomers.contains(customer.getNumber());
    }


    /**
     *
     * Everything to do with the concatenation and the finding of the optimal DT
     *
     */

    public double findOptimalDT(List<Customer> customerList) {

        //Define the first concatenatedSegment
        ConcatenatedTrip prev;
        List<Customer> veryFirstPart = new ArrayList<>();
        veryFirstPart.add(customerList.get(0));
        veryFirstPart.add(customerList.get(1));


        double[][] previousConcatenatedBPTT = getBpointsAndTheirTTForTwoCustomers(customerList.get(0), customerList.get(1));
        ConcatenatedSegment cs1 = new ConcatenatedSegment(customerList.get(0), customerList.get(1), previousConcatenatedBPTT);
        List<ConcatenatedSegment> css = new ArrayList<>();
        css.add(cs1);
        prev = new ConcatenatedTrip(css,previousConcatenatedBPTT);



        // Check starting from there

        int current_index = 1;
        List<Customer> theListUpUntilThisIndex = new ArrayList<>();
        for(int i=0; i<=current_index;i++) {
            theListUpUntilThisIndex.add(customerList.get(i));
        }


        while(current_index<customerList.size()-1) {
            theListUpUntilThisIndex.add(customerList.get(current_index+1));

            List<Customer> firstPartOfTheList = new ArrayList<>();
            for(int i=0; i<=current_index+1;i++){
                firstPartOfTheList.add(customerList.get(i));
            }


            double[][] thisBPTT = getBpointsAndTheirTTForTwoCustomers(customerList.get(current_index),customerList.get(current_index+1));
            ConcatenatedSegment this_css = new ConcatenatedSegment(customerList.get(current_index), customerList.get(current_index+1), thisBPTT);
            List<ConcatenatedSegment> css2 = new ArrayList<>();
            css2.add(this_css);
            ConcatenatedTrip this_trip = new ConcatenatedTrip(css2,thisBPTT);

            prev = concatenate(prev, this_trip);

            current_index++;

        }

        return prev.getIdealDepTime();

    }

    public double[][] getBpointsAndTheirTTForTwoCustomers(Customer c1, Customer c2) {
        List<Double> bpoints = tdi.getBpoints();
        List<Double> thisNodesBreakpoints = new ArrayList<>(bpoints);

        double[][] bpointsTravelTime = new double[thisNodesBreakpoints.size()*2+2][2];
        int bpi = 0;

        for(double breakPoint: thisNodesBreakpoints) {
            TripSegment ts = new TripSegment(c1,c2,tdi);
            ts.setDepartureTimeAtBegin(breakPoint);
            bpointsTravelTime[bpi][0] = breakPoint;
            bpointsTravelTime[bpi][1] = ts.getPreviousTravelTime();
            bpi++;
        }

        //Add all travelTime breakpoints; if a vehicle departs at a TT bp it will arrive at a TravelSpeed breakpoint
        for(double breakPoint: thisNodesBreakpoints) {
            TripSegment ts = new TripSegment(c1,c2,tdi);
            ts.setArrivalTimeAtEnd(breakPoint);
            double reverseBreakPoint = breakPoint - ts.getPreviousTravelTime();
            bpointsTravelTime[bpi][0] = reverseBreakPoint;
            bpointsTravelTime[bpi][1] = ts.getPreviousTravelTime();
            bpi++;
        }

        //Add early time window breakpoint
        TripSegment ts = new TripSegment(c1,c2,tdi);
        ts.setArrivalTimeAtEnd(c2.getReadyTime());
        double reverseBreakPoint = c2.getReadyTime() - ts.getPreviousTravelTime();
        bpointsTravelTime[bpi][0] = reverseBreakPoint;
        bpointsTravelTime[bpi][1] = ts.getPreviousTravelTime();
        bpi++;

        //Add late time window breakpoint
        TripSegment tsLate = new TripSegment(c1,c2,tdi);
        ts.setArrivalTimeAtEnd(c2.getDueTime());
        double reverseBreakPointLate = c2.getDueTime() - tsLate.getPreviousTravelTime();
        bpointsTravelTime[bpi][0] = reverseBreakPointLate;
        bpointsTravelTime[bpi][1] = tsLate.getPreviousTravelTime();

        //Breakpoints are sorted chronologically
        Arrays.sort(bpointsTravelTime, Comparator.comparingDouble(row -> row[0]));

        //Sort it out, get only the breakpoints within TW
        bpointsTravelTime = sortItOutBasedOnTW(bpointsTravelTime,c2.getReadyTime(),c2.getDueTime());

        return bpointsTravelTime;

    }

    public double[][] sortItOutBasedOnTW(double[][] bpointsTT, double eTime, double lTime) {
        int current_index = 0;
        List<Integer> indexesToDelete = new ArrayList<>();

        while(current_index < bpointsTT.length) {

            //Delete all the bpoints that leave before time zero
            if(bpointsTT[current_index][0]<0) {
                indexesToDelete.add(current_index);
            }
            //Change all the bpoints that arrive before eTime; they need to wait
            else if(bpointsTT[current_index][0]+bpointsTT[current_index][0]<eTime) {
                bpointsTT[current_index][1] += eTime-bpointsTT[current_index][1];
            }
            //Change all the bpoints that arrive after lTime; they are no longer an option; get max travelTime
            else if(bpointsTT[current_index][0]+bpointsTT[current_index][0]>lTime) {
                indexesToDelete.add(current_index);
            }


            current_index++;
        }

        //Delete indexesToDelete
        int rowIndex = 0;
        double[][] result = new double[bpointsTT.length-indexesToDelete.size()][2];
        int fillIndex = 0;
        for(double[] row: bpointsTT) {
            if(!indexesToDelete.contains(rowIndex)) {
                result[fillIndex][0] = row[0];
                result[fillIndex][1] = row[1];
                fillIndex++;
            }
            rowIndex++;
        }

        return result;

    }

    ConcatenatedTrip concatenate(ConcatenatedTrip cta, ConcatenatedTrip ctb) {

        double[][] breakpointset = new double[cta.getNoBpoints()+ctb.getNoBpoints()][2];
        double[][] bpointsCTA = cta.getBpointsTravelTime();
        double[][] bpointsCTB = ctb.getBpointsTravelTime();
        List<ConcatenatedSegment> segmentListCTA = cta.getSegmentList();
        List<ConcatenatedSegment> segmentListCTB = ctb.getSegmentList();
        Customer lastCustCTA = segmentListCTA.get(segmentListCTA.size()-1).getTo();
        Customer firstCustCTB = ctb.getSegmentList().get(0).getFrom();
        List<Customer> customersCTA = new ArrayList<>();
        customersCTA.add(segmentListCTA.get(0).getFrom());
        for(ConcatenatedSegment cs: segmentListCTA) {
            customersCTA.add(cs.getTo());
        }
        List<Customer> customersCTB = new ArrayList<>();
        customersCTB.add(segmentListCTB.get(0).getFrom());
        for(ConcatenatedSegment cs: segmentListCTB) {
            customersCTB.add(cs.getTo());
        }

        int i = 0;
        for(double[] bpointTravelTime: bpointsCTA) {
            double bpoint = bpointTravelTime[0];

            //Extend forward to determine the arrivaltime at the beginning of ctb
            double arrivalAtBeginCtb;

            SingleTrip st = new SingleTrip(customersCTA,bpoint,problem);
            List<TripSegment> segments = st.getSegments();
            double departureAtEndCta = segments.get(segments.size()-1).getDepartureTime();

            TripSegment ts = new TripSegment(lastCustCTA,firstCustCTB,tdi);
            ts.setDepartureTimeAtBegin(departureAtEndCta);
            arrivalAtBeginCtb = ts.getArrivalTime();

            // Determine the associated ready time at the end of ctb
            double departureFromBeginCtb = ts.getDepartureTime();
            SingleTrip st2 = new SingleTrip(customersCTB,departureFromBeginCtb,problem);
            List<TripSegment> segmentsb = st2.getSegments();
            double readyTimeEndCtb = segmentsb.get(segmentsb.size()-1).getDepartureTime();
            double departureFromBeginCta = segments.get(0).getArrivalTime()-segments.get(0).getPreviousTravelTime();
            double duration = readyTimeEndCtb-departureFromBeginCta;


            breakpointset[i][0] = bpoint;
            breakpointset[i][1] = duration;
            i++;

        }

        for(double[] bpointTravelTime: bpointsCTB) {
            //Find the associated startTime at the beginning of ctb
            double bpoint = bpointTravelTime[0];
            double startTimeAtBeginningOfCtb = bpoint - segmentListCTB.get(0).getFrom().getServiceTime();

            //Extend backwards to find the departureTime at the ending of cta
            TripSegment ts_new = new TripSegment(lastCustCTA,firstCustCTB,tdi);
            ts_new.setArrivalTimeAtEnd(startTimeAtBeginningOfCtb);
            double departureTimeEndOfCta = bpoint-ts_new.getPreviousTravelTime();
            //ts_new.setDepartureTime(departureTimeEndOfCta);


            //Determine the associated startTime at the beginning of cta
            SingleTrip sta = new SingleTrip(customersCTA,0.0,problem);
            sta.changeToDepartLatest(departureTimeEndOfCta);
            List<TripSegment> segmentsCTA = sta.getSegments();
            double startTime = segmentsCTA.get(0).getArrivalTime();


            //Calculate duration
            SingleTrip stb = new SingleTrip(customersCTB,bpoint,problem);
            List<TripSegment> segmentsCTB = stb.getSegments();
            double departureTimeEndOfCtb = segmentsCTB.get(segmentsCTB.size()-1).getDepartureTime();
            double duration = departureTimeEndOfCtb - startTime;


            //Insert into breakpointset
            breakpointset[i][0] = bpoint;
            breakpointset[i][1] = duration;
            i++;

        }

        List<ConcatenatedSegment> allSegments = new ArrayList<>(segmentListCTA);
        ConcatenatedSegment concSegToAdd = new ConcatenatedSegment(lastCustCTA,firstCustCTB,tdi.getTravelSpeedsPB());
        allSegments.add(concSegToAdd);
        allSegments.addAll(segmentListCTB);

        return new ConcatenatedTrip(allSegments,breakpointset);
    }


}

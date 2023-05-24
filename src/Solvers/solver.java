package Solvers;

import Parameters.Concatenation.ConcatenatedTrip;
import Parameters.Customer;
import Parameters.ProblemInstance;
import Parameters.Solution.SingleTrip;
import Parameters.Solution.Solution;
import Parameters.Solution.TripSegment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class solver {

    ProblemInstance problem;
    String solverType;
    int maxIterationsWithoutImprovement = 300000;

    //When one hour has passed, the program will shut down automatically
    int maxTotalCalculateTime = 3600;
    int seedNumber;
    Random rnd;

    private final Map<List<Customer>, ConcatenatedTrip> concatenatedAlready = new HashMap<>();


    public Solution solve(ProblemInstance problem, int seedNumber, String solverTypeGiven) throws IOException, IOException {

        this.seedNumber = seedNumber;
        this.rnd = new Random(seedNumber);
        this.solverType = solverTypeGiven;
        this.problem = problem;
        String instanceName = this.problem.getName();

        StringBuilder sb = new StringBuilder();
        sb.append("Instance name: ").append(instanceName).append("\n");

        long startTime = System.nanoTime()/1000000000;
        Solution solution = FirstSolution();
        long firstNeededTime = System.nanoTime()/1000000000 - startTime;
        sb.append("Current best cost: ").append(solution.getTotalCost()).append("\n");
        sb.append("Time when found: ").append(firstNeededTime).append("\n");

        int numberOfIterationsWithoutImprovement = 0;

        while(numberOfIterationsWithoutImprovement<maxIterationsWithoutImprovement && (System.nanoTime()/1000000000 - startTime)<maxTotalCalculateTime) {
            long currentTime = System.nanoTime()/1000000000;
            Solution copy = new Solution(solution);

            //Switch case for different solver types
            Solution newPossibility = doLNS(copy);
            newPossibility.updateFeasibility();
            if(newPossibility.getTotalCost() < solution.getTotalCost() && newPossibility.getFeasibility()) {
                solution = newPossibility;
                System.out.println("A new best cost is found: " + solution.getTotalCost());
                sb.append("Current best cost: ").append(solution.getTotalCost()).append("\n");
                sb.append("Number of iterations in between: ").append(numberOfIterationsWithoutImprovement).append("\n");
                long timeNeeded = System.nanoTime()/1000000000 - startTime;
                sb.append("Time when found: ").append(timeNeeded).append("\n");
                numberOfIterationsWithoutImprovement = 0;
            } else {
                numberOfIterationsWithoutImprovement++;
                System.out.print(".");
                if(numberOfIterationsWithoutImprovement%10000000==0) {
                    System.out.println("We're at " + numberOfIterationsWithoutImprovement + " iterations");
                    long timeNeeded = System.nanoTime()/1000000000 - startTime;
                    System.out.println("We're at " + timeNeeded + " seconds of math");
                }
            }

        }

        System.out.println("----------------------SOL after LNS --------------------");
        solution.print();

        sb.append("Final best cost: ").append(solution.getTotalCost()).append("\n");
        sb.append("Number of iterations in between: ").append(numberOfIterationsWithoutImprovement).append("\n");
        long timeNeeded = System.nanoTime()/1000000000 - startTime;
        sb.append("Time when found: ").append(timeNeeded).append("\n");

        sb.append("Solution: \n");
        solution.printAddToSB(sb);

        //Writing away the results
        File file = new File(instanceName + "-"+ solverType + seedNumber +".txt");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        return solution;
    }

    Solution FirstSolution() {

        // IF a class vehicle were to be implemented for different capacities/parameters per vehicle, then there should
        // be added a way to get rid of the symmetry problems here.
        List<Customer> customers = problem.getCustomers();
        List<List<Customer>> customersToCalculateWith = new ArrayList<>();
        for (Customer customer:customers) {
            if(customer.getNumber()!=0 && customer.getNumber()!=customers.size()-1) {
                List<Customer> customerList = new ArrayList<>();
                customerList.add(customers.get(0));
                customerList.add(customer);
                customerList.add(customers.get(customers.size()-1));
                customersToCalculateWith.add(customerList);
            }

        }

        return new Solution(customersToCalculateWith, problem, seedNumber);
    }

    Solution doLNS(Solution newPossibility) {

        //First step: destroy
        destroyRandom(newPossibility);
        destroyALoner(newPossibility);
        destroyHighestCost(newPossibility);

        //Second step: repair
        for(Customer customer: problem.getCustomers()) {
            if(!newPossibility.visitsCustomer(customer)) {
                switch(solverType) {
                    case "OptimalOptimal":
                        newPossibility.repairRegretOPTOPT(customer);
                        break;
                    case "RandomOptimal":
                        newPossibility.repairRegretRANDOPT(customer);
                        break;
                    case "EarlyOptimal":
                        newPossibility.repairRegretEARLYOPT(customer);
                        break;
                    case "EarlyEarly":
                        newPossibility.repairRegretEARLYEARLY(customer);
                    default:
                        //Default case = RandomRandom case for now
                        newPossibility.repairRegretRANDRAND(customer);
                        break;
                }
            }
        }

        newPossibility.updateFeasibility();

        return newPossibility;
    }


    public void destroyRandom(Solution newPossibility) {
        List<Customer> customers = problem.getCustomers();
        int random = rnd.nextInt(customers.size()-2) % (customers.size()-2) +1;
        Customer deletedCustomer = customers.get(random);
        newPossibility.destroyCustomer(deletedCustomer);
    }

    public void destroyALoner(Solution newPossibility) {
        for(SingleTrip st: newPossibility.getRoutes()) {
            if(st.getSegments().size()<=3) {
                newPossibility.destroyCustomer(st.getSegments().get(1).getCustomer());
                return;
            }
        }
    }

    public void destroyHighestCost(Solution newPossibility) {
        List<Customer> customers = problem.getCustomers();
        double highestCost = 0;
        Customer toDestroy = customers.get(1);

        for(SingleTrip trip: newPossibility.getRoutes()) {
            for(TripSegment segment: trip.getSegments()) {
                if(segment.getPreviousTravelTime()>highestCost && segment.getCustomer().getNumber()!=customers.size()-1 && segment.getCustomer().getNumber()!=0) {
                    highestCost = segment.getPreviousTravelTime();
                    toDestroy = segment.getCustomer();
                }
            }

        }

        newPossibility.destroyCustomer(toDestroy);
    }
}

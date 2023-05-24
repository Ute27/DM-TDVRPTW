
import Parameters.Solution.Solution;
import Readers.*;
import Parameters.*;
import Solvers.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        //Standard values that will be picked if there are no sufficient cmdline arguments
        String filepath = "jsonresources/DM-TDVRPTW/C101_25.json";
        String repairRegretType = "EarlyOptimal";
        int seedNumber = 3; //0,1,2,3 of 4

        // greater than 0
        if (args.length > 2) {
            filepath = args[0];
            repairRegretType = args[1];
            seedNumber = Integer.parseInt(args[2]);
        }
        else
            System.out.println("No sufficient amount of command line arguments.");


        SingleReader sr = new SingleReader();
        ProblemInstance problem = sr.readThisInstance(filepath);

        //After reading the following classes are filled: Customer, Depot, Position, ProblemInstance, Td_info

        long startTime = System.nanoTime()/1000000000;
        solver solver = new solver();
        Solution solution = solver.solve(problem,seedNumber,repairRegretType);
        long timeNeeded = System.nanoTime()/1000000000 - startTime;
        System.out.print("Program runtime for this experiment: " + timeNeeded);

    }
}

/*package Solvers;

import Parameters.ProblemInstance;
import Parameters.Solution.Solution;

public class PanSolver {

    ProblemInstance problem;

    // Tabu Search Parameters
    double beta_min = 3.16;
    double beta_max = 24766;
    double beta_rate = 4.06;
    double mu = 20;
    double psi_p = 101;
    double psi_cni = 59;

    // ALNS Parameters
    double alpha_min = 0.41;
    double alpha_max = 0.63;
    double alpha_rate = 0.08;
    double psi_phase = 90;
    double psi_ms = 27;
    double gamma = 0.54;
    double lambda_1 = 23;
    double lambda_2 = 11;
    double omega = 86;

    // TD Parameters
    double gamma_WT = 0.5;
    double gamma_TW = 0.85;


    public Solution solve(ProblemInstance problem) {

        this.problem = problem;
        Solution solution = FirstSolution();
        Solution bestSolution = solution;
        bestSolution.print();
        int n = problem.getCustomers().size();
        int C_cni = 0;
        double epsillon_TS;

        while(!terminationCriteriaAreMet()) {
            int d = 0;//selectRemovalOperator();
            int r = 0;//selectRepairOperator();
            double alpha = Math.min(alpha_max,alpha_min+alpha_rate*C_cni) * n;
            solution = RemoveAndRepair(solution,r,d,alpha);
            epsillon_TS = Math.min(C_cni+psi_cni,2*psi_cni);
            solution = TabuSearch(solution,epsillon_TS);

            if(solution.getTotalCost()<bestSolution.getTotalCost()) {
                C_cni = 0;
                bestSolution = solution;
            } else {
                C_cni++;
            }

            //updateScoresAndProbabilityRemovalRepairOperators();

            if(C_cni%psi_ms==0) {
                solution = bestSolution;
            }
        }

        return bestSolution;
    }

    public boolean terminationCriteriaAreMet() {
        //odo
        return false;
    }

    public Solution FirstSolution() {
        //odo
    }

    public Solution TabuSearch(Solution solution, double epsilon_TS) {
        int C_cni = 0;
        Solution bestSolution = solution;
        //Deze lijn is niet uit de thesis gehaald, eigen inbreng
        double beta = 0;

        while(C_cni<epsilon_TS) {
            //Select and apply the best non-tabu move to S based on Equation 11, to obtain S'
            //Solution newSolution = doBestNonTabuMove();
            Solution newSolution = solution;
            if(newSolution.getFeasibility()) {
                if(newSolution.getTotalCost()<bestSolution.getTotalCost()) {
                    bestSolution = newSolution;
                    C_cni = 0;
                } else {
                    C_cni++;
                }
                beta = Math.max(beta_min,beta/beta_rate);
            } else {
                C_cni++;
                beta = Math.min(beta_max,beta_rate*beta);
            }
            //updateTabuList();
            //reset beta after every psi_p iterations
            solution = newSolution;
        }
        return bestSolution;
    }

}*/
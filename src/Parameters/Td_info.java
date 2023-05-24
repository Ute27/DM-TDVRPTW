package Parameters;

import java.util.*;

public class Td_info {
    private final int nbPeriods;
    private final int nbCategories;
    //Edge matrix will have nbNodes as dimension sizes
    private final int nbNodes;
    private final List<Double> bpoints;
    //first column = speed zone
    //second column = category
    private final double[][] travelSpeedsPB;
    private final int[][] arcCategories;

    public Td_info(int nbPeriods, int nbCategories, int nbNodes, List<Double> bpoints, double[][] travelSpeedsPB, int[][] arcCategories) {
        this.nbPeriods = nbPeriods;
        this.nbCategories = nbCategories;
        this.nbNodes = nbNodes;
        this.bpoints = bpoints;
        this.travelSpeedsPB = travelSpeedsPB;
        this.arcCategories = arcCategories;
    }

    public int getNbPeriods() {
        return nbPeriods;
    }

    public int getNbCategories() {
        return nbCategories;
    }

    public int getNbNodes() {
        return nbNodes;
    }

    public List<Double> getBpoints() {
        return bpoints;
    }

    public double[][] getTravelSpeedsPB() {
        return travelSpeedsPB;
    }

    public int[][] getArcCategories() {
        return arcCategories;
    }
}
